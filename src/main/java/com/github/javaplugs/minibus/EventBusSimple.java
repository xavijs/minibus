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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple event bus with no background threads.
 * All consumers will be called directly during event publishing.
 * You can use it in a cases where event publishing is rare
 * or if there is requirement to use as less threads as possible.
 */
public class EventBusSimple<E extends EventBusEvent> implements EventBus<E> {

    private static final Logger logger = LoggerFactory.getLogger(EventBusSimple.class);

    private final ReferenceQueue gcQueue = new ReferenceQueue();

    private final AtomicInteger processing = new AtomicInteger();

    private final Set<WeakHandler<EventBusHandler<E>>> handlers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void subscribe(EventBusHandler<? extends E> subscriber) {
        handlers.add(new WeakHandler(subscriber, gcQueue));
    }

    @Override
    public void unsubscribe(EventBusHandler<? extends E> subscriber) {
        handlers.remove(new WeakHandler(subscriber, gcQueue));
    }

    @Override
    public void publish(E event) {
        if (event == null) {
            return;
        }
        processing.incrementAndGet();
        try {
            processEvent(event);
        } finally {
            processing.decrementAndGet();
        }
    }

    @Override
    public boolean hasPendingEvents() {
        return processing.get() > 0;
    }

    private void processEvent(E event) {
        WeakHandler wh;
        while ((wh = (WeakHandler)gcQueue.poll()) != null) {
            handlers.remove(wh);
        }
        if (event != null) {
            notifySubscribers(event);
        }
    }

    private void notifySubscribers(E event) {
        for (WeakHandler<EventBusHandler<E>> wh : handlers) {
            EventBusHandler<E> eh = wh.get();
            if (eh == null) {
                continue;
            }

            try {
                if (eh.getTypeClass() == null) {
                    if (eh.canHandle(event.getClass())) {
                        eh.handle(event);
                    }
                } else if (eh.getTypeClass().equals(event.getClass())) {
                    eh.handle(event);
                }
            } catch (Throwable th) {
                logger.error("Event processing fail " + event.getClass().getSimpleName()
                    + ". " + th.getMessage(), th);
            }
        }
    }
}
