package com.kosenkov.androidbooks.books

import android.util.LruCache
import com.kosenkov.androidbooks.collections.getOrPut

// A dummy example to justify a common interface
class GoogleBooksCache(private val delegate: GoogleBooks) : GoogleBooks {

    override fun search(query: String, startIndex: Int) =
            if (startIndex == 0) {
                // try to cache the first page of results to minimize GUI latency
                cached("first page of $query") {
                    delegate.search(query, startIndex)
                }
            } else {
                // subsequent pages will be loaded directly
                delegate.search(query, startIndex)
            }

    override fun details(volumeId: String) = cached("details of $volumeId") {
        // individual book details will be cached for faster re-visit and lower burden on API
        delegate.details(volumeId)
    }


    // Interface methods are documented as blocking, so everything is possible here!
    // disk cache, db lookup, memcached, etc...
    private val storage = LruCache<String, Any>(10)

    private inline fun <reified V> cached(key: String, generator: () -> V): V {
        @Suppress("UNCHECKED_CAST")
        val unchecked = storage as LruCache<String, V>

        return unchecked.getOrPut(key, generator)
    }

}