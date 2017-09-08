package com.zu.sweetalbum.module;

/**
 * Created by zu on 17-7-4.
 */

public interface Function<K, T> {
    K apply(T t);
}
