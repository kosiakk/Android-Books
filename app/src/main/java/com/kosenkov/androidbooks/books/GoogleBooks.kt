package com.kosenkov.androidbooks.books

import org.json.JSONObject
import java.io.Serializable

/**
 * A low-level mirror of Google Books API
 * All calls are blocking
 */
interface GoogleBooks {

    // Actually, this is a generic container for paged search results, as seen in other Google APIs
    data class Volumes(val totalItems: Int, val items: List<Volume>)

    /**
     * Minimal subset of search results for each Volume
     */
    data class Volume(val id: String,
                      val title: String,
                      val subtitle: String?,
                      val authors: String,
                      val thumbnailImageLinks: String?) : Serializable

    /**
     * Detailed information, which enriches the Volume data
     */
    data class VolumeDetails(val volume: Volume,
                             val details:JSONObject
            // Many other fields might be added here on demand.
            // Alternatively, this class might be converted to purely dynamic data source like a Map
    ) : Serializable

    /**
     * Performs blocking low-level operation and returns one page of search results.
     * Several calls to this function are required to get the complete list.
     */
    fun search(query: String, startIndex: Int = 0): Volumes

    /**
     * Retrieve all the details about a specific volume
     */
    fun details(volumeId: String): VolumeDetails

    /**
     * Expected length of Volumes.items
     */
    val pageSize: Int
}