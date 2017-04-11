package com.kosenkov.androidbooks.books

/**
 * Created by Alexander Kosenkov on 11.04.2017.
 */

interface GoogleBooks {

    data class Volumes(val startIndex: Int, val totalItems: Int, val items: List<Volume>)

    class Volume(val kind: String,
                 val id: String,
                 val title: String,
                 val subtitle: String,
                 val authors: String,
                 val thumbnailImageLinks: String
    ) {

        inner class Details(val mainCategory: String)

    }

    /**
     * Performs blocking operation and returns paged search results
     */
    fun syncSearch(query: String, startIndex: Int = 0): Volumes

}