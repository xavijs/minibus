/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vladislav Zablotsky
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package com.github.javaplugs.minibus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Async event bus that will run each event/handler call in separate thread.
 * By default using CachedThreadPool to run handlers.
 */
public class EventBusAsync<E extends EventBusEvent> implements EventBus<E> {

    private static final Logger logger = LoggerFactory.getLogger(EventBusAsync.class);

    private final Thread eventQueueThread;

    private final Queue<E> eventsQueue = new ConcurrentLinkedQueue<>();

    private final ReferenceQueue gcQueue = new ReferenceQueue();

    private final Map<Class, Set<WeakHandler<EventBusHandler<E>>>> handlersCls = new ConcurrentHashMap<>();

    private final Set<WeakHandler<EventBusHandler<E>>> handlers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final ExecutorService handlersExecutor;

    /**
     * Create new EventBus instance with default presets.
     */
    public EventBusAsync() {
        this(Executors.newCachedThreadPool());
    }

    /**
     * Create instance with customer ExecutorService for event handlers.
     *
     * @param handlersExecutor Will be used to run event handler processing for each event
     */
    public EventBusAsync(ExecutorService handlersExecutor) {
        this.handlersExecutor = handlersExecutor;
        eventQueueThread = new Thread(this::eventsQueue, "EventQueue handlers thread");
        eventQueueThread.setDaemon(true);
        eventQueueThread.start();
    }

    @Override
    public void subscribe(EventBusHandler<? extends E> subscriber) {
        Class<? extends E> cls = subscriber.getTypeClass();
        if (cls == null) {
            handlers.add(new WeakHandler(subscriber, gcQueue));
        } else {
            synchronized (this) {
                Set<WeakHandler<EventBusHandler<E>>> hs = handlersCls.get(cls);
                if (hs == null) {
                    hs = Collections.newSetFromMap(new ConcurrentHashMap<>());
                    handlersCls.put(cls, hs);
                }
                hs.add(new WeakHandler(subscriber, gcQueue));
            }
        }
    }

    @Override
    public void unsubscribe(EventBusHandler<? extends E> subscriber) {
        Class cls = subscriber.getTypeClass();
        if (cls == null) {
            handlers.remove(new WeakHandler(subscriber, gcQueue));
        } else {
            Set<WeakHandler<EventBusHandler<E>>> set = handlersCls.get(cls);
            if (set != null) {
                set.remove(new WeakHandler(subscriber, gcQueue));
            }
        }
    }

    @Override
    public void publish(E event) {
        if (event == null) {
            return;
        }
        eventsQueue.add(event);
    }

    @Override
    public boolean hasPendingEvents() {
        return !eventsQueue.isEmpty();
    }

    private void eventsQueue() {
        while (true) {
            WeakHandler wh;
            while ((wh = (WeakHandler)gcQueue.poll()) != null) {
                Class cls = wh.getHandlerTypeClass();
                if (cls == null) {
                    handlers.remove(wh);
                } else {
                    Set<WeakHandler<EventBusHandler<E>>> set = handlersCls.get(cls);
                    if (set != null) {
                        set.remove(wh);
                    }
                }
            }

            E event = eventsQueue.poll();
            if (event != null) {
                notifySubscribers(event);
            }
        }
    }

    private void notifySubscribers(E event) {
        try {
            Set<WeakHandler<EventBusHandler<E>>> hcls = handlersCls.get(event.getClass());
            if (hcls != null) {
                for (WeakHandler<EventBusHandler<E>> wh : hcls) {
                    EventBusHandler<E> eh = wh.get();
                    if (eh != null) {
                        handlersExecutor.submit(() -> runHandlerWrapper(eh, event));
                    }
                }
            }

            for (WeakHandler<EventBusHandler<E>> wh : handlers) {
                EventBusHandler<E> eh = wh.get();
                if (eh.canHandle(event.getClass())) {
                    handlersExecutor.submit(() -> runHandlerWrapper(eh, event));
                }
            }
        } catch (Throwable th) {
            logger.error("Event processing fail " + event.getClass().getSimpleName()
                + ". " + th.getMessage(), th);
        }
    }

    private void runHandlerWrapper(EventBusHandler<E> handler, E event) {
        try {
            runHandler(handler, event);
        } catch (Throwable th) {
            logger.error("Handler " + handler.getClass().getSimpleName()
                + " fail on event " + event.getClass().getSimpleName()
                + ". " + th.getMessage(), th);
        }
    }

    /**
     * You can override this method to add some hooks to events processing.
     */
    protected void runHandler(EventBusHandler<E> eh, E event) {
        eh.handleEvent(event);
    }
}
