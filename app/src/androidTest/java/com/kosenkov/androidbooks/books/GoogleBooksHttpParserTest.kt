package com.kosenkov.androidbooks.books

import android.support.test.runner.AndroidJUnit4
import com.kosenkov.androidbooks.collections.asSequence
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Kosenkov on 18.04.2017.
 */
@RunWith(AndroidJUnit4::class)

class GoogleBooksHttpParserTest {

    @Test
    fun testParser() {

        //language=JSON
        val json = """
        {
         "totalItems": 894,
         "items": [
          {
           "kind": "books#volume",
           "id": "btIQAAAAYAAJ",
           "volumeInfo": {
            "title": "Alice's Adventures in Wonderland",
            "authors": [
             "Lewis Carroll"
            ],
            "imageLinks": {
             "thumbnail": "http://books.google.com/books/content?id=btIQAAAAYAAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
            }
           }
          },
          {
           "kind": "books#volume",
           "id": "ubKYBfz_tZ0C",
           "volumeInfo": {
            "title": "Handlesen",
            "subtitle": "the easy way",
            "authors": [
             "Alice Funk"
            ],
            "imageLinks": {
             "thumbnail": "http://books.google.com/books/content?id=ubKYBfz_tZ0C&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
            }
           }
          },
          {
           "kind": "books#volume",
           "id": "4S9lqLKg2A0C",
           "volumeInfo": {
            "title": "Alice Munro",
            "authors": [
             "Coral Ann Howells"
            ],
            "imageLinks": {
             "thumbnail": "http://books.google.com/books/content?id=4S9lqLKg2A0C&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
            }
           }
          }
         ]
        }
        """

        val data = JSONObject(json).toVolumes()
        val books = data.items

        assertEquals(3, books.size)
        assertEquals("Alice's Adventures in Wonderland", books[0].title)
        assertEquals("the easy way", books[1].subtitle)
        assertEquals("Coral Ann Howells", books[2].authors)
        assertEquals("http://books.google.com/books/content?id=4S9lqLKg2A0C&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api", books[2].thumbnailImageLinks)

    }

    @Test
    fun testJsonTypedParser() {
        //language=JSON
        val data = JSONArray("""[ 1.0, 1.1, "zero" ]""")

        val numbers = data.asSequence<Double>().toList().toDoubleArray()
        assertArrayEquals(doubleArrayOf(1.0, 1.1), numbers, 0.01)

        val text = data.asSequence<String>().toList().toTypedArray()
        assertArrayEquals(arrayOf("1.0", "1.1", "zero"), text)
    }


}