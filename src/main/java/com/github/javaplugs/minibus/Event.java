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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * MUTABLE Event (or message) object that passed from publisher to consumer.
 * Be aware of possible message values mutability, do not try to edit this values in consumer.
 * Each event may contain:
 * 1. key-value(object) map
 * 2. objects set
 * Key-value map is just a map where values are generic object.
 * Objects set allows you associate only one object (value) of particular class with one event.
 *
 * Keep in mind that Event is not serializable because it should be used only in one active instance.
 */
public class Event {

    private final String type;

    private final Map<String, Object> properties = new HashMap<>(8);

    private final Map<Class<?>, Object> objects = new HashMap<>(8);

    private boolean locked;

    /**
     * Create message with appropriate type.
     *
     * @param type Message type, event bus will use type while choosing appropriate consumers.
     */
    public Event(String type) {
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Create event with provided properties.
     *
     * @param type Message type, event bus will use type while choosing appropriate consumers.'
     * @param properties Event properties
     */
    public Event(String type, Map<String, Object> properties) {
        this.type = Objects.requireNonNull(type);;
        this.properties.putAll(Objects.requireNonNull(properties));
    }

    /**
     * Return message type.
     * Event bus will use type while choosing appropriate consumers.
     */
    public String getType() {
        return type;
    }

    protected void lock() {
        this.locked = true;
    }

    private void isLocked() {
        if (locked) {
            throw new UnsupportedOperationException("You can not call any setters on "
                + getClass().getSimpleName() + " instance after event was published.");
        }
    }

    /**
     * Return true if event has any associated objects (objects set is not empty).
     * Object values are stored in separate collection that key-value properties.
     */
    public boolean hasObjects() {
        return objects.isEmpty();
    }

    /**
     * Return all objects associated with current event.
     * Object values are stored in separate collection that key-value properties.
     */
    public Collection<Object> getObjects() {
        return objects.values();
    }

    /**
     * Associate some object with event, using another collection than for key-value properties.
     * This is simplified map where key is object class, thus you will be able to store only one
     * instance of particular class.
     * For example if you will try to add sequentially instance1, instance2 and instance3 of SomeClass
     * to event - only last added instance3 will be saved.
     *
     * @param obj Object to store.
     * @throws UnsupportedOperationException If this method will be called during event processing
     */
    public void setObject(Object obj) throws UnsupportedOperationException {
        isLocked();
        objects.put(obj.getClass(), obj);
    }

    /**
     * Return value associated with provided class.
     * Object values are stored in separate collection that key-value properties.
     */
    public <T> Optional<T> getObj(Class<T> cls) {
        return Optional.ofNullable(getObject(cls));
    }

    /**
     * Return object value associated with provided class or null.
     * Object values are stored in separate collection that key-value properties.
     */
    public <T> T getObject(Class<T> cls) {
        Object obj = objects.get(cls.getClass());
        if (obj == null) {
            return null;
        }
        return cls.cast(obj);
    }

    /**
     * Return true if event has any properties (key-value maps is not empty).
     */
    public boolean hasProperties() {
        return properties.isEmpty();
    }

    /**
     * Return all keys associated with key-value properties of current event.
     */
    public Collection<String> getKeys() {
        return properties.keySet();
    }

    /**
     * Associate property value with event.
     *
     * @param key Property key
     * @param value Property value
     * @throws UnsupportedOperationException If this method will be called during event processing
     */
    public void setValue(String key, Object value) throws UnsupportedOperationException {
        isLocked();
        properties.put(key, value);
    }

    /**
     * Return message property value casted to class if any.
     *
     * @param key Property key in map
     * @param cls Expected value class
     * @return Result will be empty if value does not exist or wrong class to cast
     */
    public <T> Optional<T> getVal(String key, Class<T> cls) {
        return Optional.ofNullable(getValue(key, cls));
    }

    /**
     * Return message property value casted to class if any.
     *
     * @param key Property key in map
     * @param cls Expected value class
     * @return Will return matched value or null (if value does not exist or wrong class)
     */
    public <T> T getValue(String key, Class<T> cls) {
        Object val = properties.get(key);
        if (val == null) {
            return null;
        }

        if (cls.isInstance(val)) {
            return cls.cast(val);
        }
        return null;
    }

    /**
     * Return message property value casted to class or default value.
     *
     * @param key Property key in map
     * @param cls Expected value class
     * @return Will return matched value or null (if value does not exist or wrong class)
     *
     */
    public <T> T getValueOr(String key, Class<T> cls, T defaultValue) {
        T val = getValue(key, cls);
        return val == null ? defaultValue : val;
    }

    /**
     * Return string representation of value.
     * Will call toString() on object, thus no matter what value type is.
     *
     * @param key Property key
     * @return String or null if not value
     */
    public String getString(String key) {
        Object val = properties.get(key);

        return val == null ? null : val.toString();
    }

    /**
     * Return string representation of value or default String.
     * Will call toString() on object, thus no matter what value type is.
     *
     * @param key Property key
     * @return String or default value
     */
    public String getStringOr(String key, String defaultValue) {
        String str = getString(key);
        return str == null ? defaultValue : str;
    }
}
