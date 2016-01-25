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
 * Typical builder for Event type.
 */
public class EventBuilder {

    private final Event event;

    private EventBuilder(Event event) {
        this.event = event;
    }

    /**
     * Create new builder for event with specific type.
     */
    public static EventBuilder create(String eventType) {
        return new EventBuilder(new Event(eventType));
    }

    /**
     * Put property into event properties.
     */
    public EventBuilder val(String key, Object value) {
        event.setValue(key, value);
        return this;
    }

    /**
     * Set object value.
     * Used separate structure than for properties.
     */
    public EventBuilder set(Object objectValue) {
        event.setObject(objectValue);
        return this;
    }

    public Event build() {
        return this.event;
    }
}
