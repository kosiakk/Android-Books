package com.kosenkov.androidbooks.books

import android.util.LruCache
import com.kosenkov.androidbooks.collections.getOrPut

/**
 * A simple caching layer around GoogleBooks API.
 * It remembers several last search results and doesn't call
 * HTTP backend if the answer is ready.
 *
 * A trivial example a useful common interface.
 */
class GoogleBooksCache(private val delegate: GoogleBooks) : GoogleBooks {

    // GoogleBooks Interface methods are documented as blocking, so everything is possible here!
    // disk cache, db lookup, Memcached, etc...
    private val cache1 = LruCache<String, GoogleBooks.Volumes>(10)
    private val cache2 = LruCache<String, GoogleBooks.VolumeDetails>(10)

    /**
     * Caches the first page of results to minimize GUI latency
     */
    override fun search(query: String, startIndex: Int) =
            if (startIndex == 0) {
                cache1.getOrPut(query) {
                    delegate.search(query, startIndex)
                }
            } else {
                // subsequent pages will be loaded directly
                delegate.search(query, startIndex)
            }

    /**
     * Individual book details will be cached for faster re-visit and lower burden on API
     */
    override fun details(volumeId: String) =
            cache2.getOrPut(volumeId) {
                delegate.details(volumeId)
            }

    override val pageSize = delegate.pageSize
}