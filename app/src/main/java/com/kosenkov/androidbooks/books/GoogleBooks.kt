package com.kosenkov.androidbooks.books

/**
 * Created by Alexander Kosenkov on 11.04.2017.
 */

interface GoogleBooks {

    data class Volumes(val totalItems: Int, val items: Sequence<Volume>)

    data class Volume(val id: String,
                      val title: String,
                      val subtitle: String?,
                      val authors: String,
                      val thumbnailImageLinks: String?)

    data class VolumeDetails(val volume: Volume, val mainCategory: String)

    /**
     * Performs blocking low-level operation and returns one page of search results
     */
    fun search(query: String, startIndex: Int = 0): Volumes

    fun details(volumeId: String): VolumeDetails
}