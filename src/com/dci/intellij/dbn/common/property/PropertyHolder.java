package com.dci.intellij.dbn.common.property;

public interface PropertyHolder<T extends Property> {
    boolean set(T status, boolean value);

    boolean is(T status);



    default boolean isNot(T status) {
        return !is(status);
    };

    static <T extends Property> PropertyHolder<T> create(Class<T> type) {
        return new PropertyHolderImpl<T>() {
            @Override
            protected T[] properties() {
                return type.getEnumConstants();
            }
        };
    }
}
