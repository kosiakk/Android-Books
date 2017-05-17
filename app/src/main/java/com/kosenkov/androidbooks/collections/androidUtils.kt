package com.kosenkov.androidbooks.collections

import android.util.LruCache


// copied from Kotlin MutableMap.getOrPut because LruCache is not a Map
inline fun <K, V> LruCache<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    val value = get(key)
    return if (value == null) {
        val answer = defaultValue()
        put(key, answer)
        answer
    } else {
        value
    }
}