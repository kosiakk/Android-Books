package com.kosenkov.androidbooks

import android.support.v7.widget.RecyclerView
import android.util.Log
import com.kosenkov.androidbooks.books.GoogleBooks
import com.kosenkov.androidbooks.books.GoogleBooksCache
import com.kosenkov.androidbooks.books.GoogleBooksHttp
import com.kosenkov.androidbooks.collections.LazyPagedList
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Global singleton in order to share the cache
 */
val booksApi = GoogleBooksCache(GoogleBooksHttp())

/**
 * Loads Google Books search results in the background thread.
 *
 * The first page is loaded synchronously in the constructor.
 * Other pages are loaded in the background in demand.
 */
class LazyBooksList(val searchQuery: String,
                    val callback: RecyclerView.Adapter<*>)
    : LazyPagedList<GoogleBooks.Volume>(booksApi.pageSize) {

    private val totalItems: Int

    init {
        val firstPage = booksApi.search(searchQuery)
        totalItems = firstPage.totalItems
        setPageData(0, firstPage.items.toList())
    }

    override val size: Int
        get() = totalItems

    override fun enqueueFetch(pageIndex: Int) {
        Log.v("LazyList", "enqueueFetch(pageIndex=$pageIndex)")
        doAsync {
            val startIndex = pageIndex * pageSize

            // blocking call
            val result = booksApi.search(searchQuery, startIndex)

            setPageData(pageIndex, result.items.toList())

            uiThread {
                // Only the original thread that created a view hierarchy can touch its views.
                callback.notifyItemRangeChanged(startIndex, pageSize)
            }
        }
    }
}