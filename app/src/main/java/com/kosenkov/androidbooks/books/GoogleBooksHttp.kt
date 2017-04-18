package com.kosenkov.androidbooks.books

import android.net.Uri
import com.kosenkov.androidbooks.asSequence
import org.json.JSONObject
import java.net.URL
import kotlin.text.Charsets.UTF_8

class GoogleBooksHttp : GoogleBooks {

    // Any sophisticated hacker will extract this key from APK or sniff anyways.
    private val api = "AIzaSyAyiYb_NGVu6MIim1TmQDRG0VRrnO0C550"

    // https://developers.google.com/books/docs/v1/reference/volumes
    private fun JSONObject.toVolumes(startIndex: Int) =
            GoogleBooks.Volumes(
                    startIndex,
                    getInt("totalItems"),
                    getJSONArray("items").asSequence<JSONObject>().map { it.toVolume() }
            )


    private fun JSONObject.toVolume(): GoogleBooks.Volume {
        val info = getJSONObject("volumeInfo")
        return GoogleBooks.Volume(
                getString("kind"),
                getString("id"),
                info.getString("title"),
                info.optString("subtitle"),
                info.getJSONArray("authors").asSequence<String>().joinToString(),
                info.optJSONObject("imageLinks")?.getString("thumbnail")
        )
    }

    /**
     * Performs blocking HTTP request to Google API and parses resulting JSON
     */
    @Throws(Exception::class)
    override fun search(query: String, startIndex: Int): GoogleBooks.Volumes {
        require(startIndex >= 0)

        // doc: https://developers.google.com/books/docs/v1/reference/volumes/list
        val uri = Uri.Builder()
                .scheme("https")
                .authority("www.googleapis.com")
                .appendEncodedPath("books/v1/volumes")
                .appendQueryParameter("q", query)
                .appendQueryParameter("key", api)
                // .appendQueryParameter("maxResults", 40)
                .appendQueryParameter("orderBy", "relevance")
                .appendQueryParameter("projection", "lite") // might just require important fields
//                .appendQueryParameter("fields", "totalItems,items(id,kind,volumeInfo(title,subtitle,authors,imageLinks(thumbnail)))")
                .appendQueryParameter("startIndex", startIndex.toString())
                .build()


        val json: String = URL(uri.toString()).openStream().use {
            it.bufferedReader(charset = UTF_8).readText()
        } // will throw exception otherwise

        return parseVolumes(json, startIndex)
    }

    fun parseVolumes(json: String, startIndex: Int) = JSONObject(json).toVolumes(startIndex)

    override fun details(volumeId: String): GoogleBooks.Volume.Details {
        TODO("https://books.google.com/ebooks?id=$volumeId")
    }
}

