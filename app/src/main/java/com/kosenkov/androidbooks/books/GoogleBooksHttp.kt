package com.kosenkov.androidbooks.books

import android.net.Uri
import android.util.Log
import org.json.JSONObject
import java.net.URL
import kotlin.text.Charsets.UTF_8

class GoogleBooksHttp : GoogleBooks {

    private fun httpAPI() = Uri.Builder()
            .scheme("https")
            .authority("www.googleapis.com")
            .appendEncodedPath("books/v1/volumes")
            .appendQueryParameter("key", "AIzaSyAyiYb_NGVu6MIim1TmQDRG0VRrnO0C550")

    /**
     * Fixed number of volumes fetched per HTTP-request at once
     */
    override val pageSize = 32

    /**
     * Performs blocking HTTP request to Google API and parses resulting JSON
     */
    @Throws(Exception::class)
    override fun search(query: String, startIndex: Int): GoogleBooks.Volumes {
        Log.v("GoogleBooksHttp", "$query (start from $startIndex)")

        require(startIndex >= 0)

        // doc: https://developers.google.com/books/docs/v1/reference/volumes/list
        val uri = httpAPI()
                .appendQueryParameter("q", query)
                .appendQueryParameter("maxResults", pageSize.toString())
                .appendQueryParameter("orderBy", "relevance")
                .appendQueryParameter("fields", "totalItems,items(id,kind,volumeInfo(title,subtitle,authors,imageLinks(thumbnail)))")
                .appendQueryParameter("startIndex", startIndex.toString())
                .build()

        return uri.readJson().toVolumes()
    }

    override fun details(volumeId: String): GoogleBooks.VolumeDetails {
        Log.v("GoogleBooksHttp", volumeId)

        // doc: https://developers.google.com/books/docs/v1/reference/volumes/get
        val uri = httpAPI()
                .appendPath(volumeId)
                .appendQueryParameter("projection", "lite")
                .build()

        val json = uri.readJson()

        return GoogleBooks.VolumeDetails(json.toVolume(), ("main category"))
    }

    private fun Uri.readJson() = JSONObject(readString())

    /*
    The method will throw exception on failures, because retry after timeout might be too late for GUI.
    How to handle 5xx errors? Quota Exceeded errors?
     */
    private fun Uri.readString(): String =
            URL(toString()).openStream().use {
                it.bufferedReader(charset = UTF_8).readText()
            }
}


