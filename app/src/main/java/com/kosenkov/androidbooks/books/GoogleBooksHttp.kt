package com.kosenkov.androidbooks.books

import android.net.Uri
import com.kosenkov.androidbooks.asSequence
import org.json.JSONObject
import java.net.URL
import kotlin.text.Charsets.UTF_8

class GoogleBooksHttp : GoogleBooks {

    private fun httpAPI() = Uri.Builder()
            .scheme("https")
            .authority("www.googleapis.com")
            .appendEncodedPath("books/v1/volumes")
            .appendQueryParameter("key", "AIzaSyAyiYb_NGVu6MIim1TmQDRG0VRrnO0C550")

    val PageSize = 40

    /**
     * Performs blocking HTTP request to Google API and parses resulting JSON
     */
    @Throws(Exception::class)
    override fun search(query: String, startIndex: Int): GoogleBooks.Volumes {
        require(startIndex >= 0)

        // doc: https://developers.google.com/books/docs/v1/reference/volumes/list
        val uri = httpAPI()
                .appendQueryParameter("q", query)
                .appendQueryParameter("maxResults", PageSize.toString())
                .appendQueryParameter("orderBy", "relevance")
                .appendQueryParameter("fields", "totalItems,items(id,kind,volumeInfo(title,subtitle,authors,imageLinks(thumbnail)))")
                .appendQueryParameter("startIndex", startIndex.toString())
                .build()

        val json: String = uri.readString() // will throw exception otherwise

        return parseVolumes(JSONObject(json), startIndex, query)
    }

    override fun details(volumeId: String): GoogleBooks.VolumeDetails {

        // doc: https://developers.google.com/books/docs/v1/reference/volumes/get
        val uri = httpAPI()
                .appendPath(volumeId)
                .appendQueryParameter("projection", "lite")
                .build()

        val json = uri.readString().toJsonObject()

        return GoogleBooks.VolumeDetails(json.toVolume(), ("main category"))
    }

    // https://developers.google.com/books/docs/v1/reference/volumes
    private fun JSONObject.toVolumes(startIndex: Int, searchQuery: String) =
            GoogleBooks.Volumes(
                    getInt("totalItems"),
                    getJSONArray("items").asSequence<JSONObject>().map { it.toVolume() }
            )

    private fun JSONObject.toVolume(): GoogleBooks.Volume {
        val info = getJSONObject("volumeInfo")
        return GoogleBooks.Volume(
                getString("id"),
                info.getString("title"),
                info.optString("subtitle"),
                info.optJSONArray("authors")?.asSequence<String>()?.joinToString() ?: "",
                info.optJSONObject("imageLinks")?.getString("thumbnail")
        )
    }

    // will throw exception on failures
    private fun Uri.readString(): String {

        val json: String = URL(toString()).openStream().use {
            it.bufferedReader(charset = UTF_8).readText()
        }

        // should we handle network problems?
        // 1. retry after timeout might be too late for GUI
        // 2. how to handle 5xx errors? Quota Exceeded errors?

        return json
    }

    fun parseVolumes(jsonObject: JSONObject, startIndex: Int, searchQuery: String) = jsonObject.toVolumes(startIndex, searchQuery)

    private inline fun String.toJsonObject() = JSONObject(this)


}


