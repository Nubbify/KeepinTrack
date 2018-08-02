package com.nubbify.keepintrack.utils;

public class ValueContainer<T> {
    private T value;

    public ValueContainer(T v) {
        this.value = v;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
