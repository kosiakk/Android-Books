package com.kosenkov.androidbooks.books

import org.json.JSONObject

class GoogleBooksHttp : GoogleBooks {

    // https://developers.google.com/books/docs/v1/reference/volumes
    private fun JSONObject.toVolume() =
            GoogleBooks.Volume(
                    getString("kind"),
                    getString("id"),
                    getJSONObject("volumeInfo").getString("title"),
                    getJSONObject("volumeInfo").getString("subtitle"),
                    getJSONObject("volumeInfo").getJSONArray("authors").join(", "),
                    getJSONObject("imageLinks").getString("thumbnail")
            )

    /**
     * Performs blocking HTTP request to Google API and parses resulting JSON
     */
    override fun syncSearch(query: String, startIndex: Int): GoogleBooks.Volumes {
        TODO("https://developers.google.com/books/docs/v1/reference/volumes/list")
    }
}