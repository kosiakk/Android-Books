package com.kosenkov.androidbooks.books

import com.kosenkov.androidbooks.collections.asSequence
import org.json.JSONObject

/**
 * Created by Kosenkov on 17.05.2017.
 */

// https://developers.google.com/books/docs/v1/reference/volumes
fun JSONObject.toVolumes() =
        GoogleBooks.Volumes(
                getInt("totalItems"),
                optJSONArray("items")?.asSequence<JSONObject>()?.map { it.toVolume() } ?: emptySequence()
        )

fun JSONObject.toVolume(): GoogleBooks.Volume {
    val info = getJSONObject("volumeInfo")
    return GoogleBooks.Volume(
            getString("id"),
            info.getString("title"),
            info.optString("subtitle"),
            info.optJSONArray("authors")?.asSequence<String>()?.joinToString() ?: "",
            info.optJSONObject("imageLinks")?.getString("thumbnail")
    )
}
