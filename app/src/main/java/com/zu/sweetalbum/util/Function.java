package com.zu.sweetalbum.util;

/**
 * Created by zu on 17-7-4.
 */

public interface Function<K, T> {
    K apply(T t);
}
