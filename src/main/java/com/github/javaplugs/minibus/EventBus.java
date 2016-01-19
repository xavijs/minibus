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

/**
 * Generic event bus interface.
 * I assume that there can be several possible implementations with different approach to
 * event/threads handling.
 */
public interface EventBus<E extends Event> {

    /**
     * Subscribe consumer to the event bus using weak link.
     *
     * Subscribing same object twice should not affect how many times subscriber will
     * be called per one event.
     *
     * @param subscriber The object to subscribe to the event bus.
     */
    void subscribe(EventHandler<E> subscriber);

    /**
     * Removes the specified consumer from the event bus subscription list.
     * Once removed, the specified object will no longer receive events posted to the
     * event bus.
     *
     * @param subscriber The object previous subscribed to the event bus.
     */
    void unsubscribe(EventHandler<E> subscriber);

    /**
     * Sends a message on the bus which will be propagated to the appropriate
     * subscribers related to event type.
     *
     * There is no specification given as to how the messages will be delivered,
     * in terms of synchronous or asynchronous.
     *
     * @param event Event to publish
     */
    void publish(E event);

    /**
     * Indicates whether the bus has pending events to publish. Since message/event
     * delivery can be asynchronous (on other threads), the method can be used to
     * start or stop certain actions based on all the events having been published.
     * I.e. perhaps before an application closes, etc.
     *
     * @return True if events are still being delivered.
     */
    boolean hasPendingEvents();

}
