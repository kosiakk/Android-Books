package com.kosenkov.androidbooks

import android.os.AsyncTask
import org.json.JSONArray
import org.json.JSONObject

/**
 * Allows working with JSONArray like with a Kotlin sequence (use map, filter, etc.)
 *
 * @param generator takes JSON Array index and return value of desired type
 */
inline fun <T : Any> JSONArray.asSequence(crossinline generator: JSONArray.(Int) -> T): Sequence<T> {
    var position = 0
    return generateSequence {
        if (position < length()) {
            generator(position++)
        } else {
            null
        }
    }
}

/**
 * Allows working with JSONArray like with a Kotlin sequence (use map, filter, etc.)
 * Unfortunately, JSON Objects have no common type, therefore Any is used
 *
 * @use anArray.asSequence<String>().map { ... }
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> JSONArray.asSequence(): Sequence<T> = when (T::class) {
    Int::class -> asSequence(JSONArray::getInt) as Sequence<T>
    Boolean::class -> asSequence(JSONArray::getBoolean) as Sequence<T>
    String::class -> asSequence(JSONArray::getString) as Sequence<T>
    JSONObject::class -> asSequence(JSONArray::getJSONObject) as Sequence<T>
    JSONArray::class -> asSequence(JSONArray::getJSONArray) as Sequence<T>
    else -> asSequence(JSONArray::get).filterIsInstance(T::class.java)
}


/**
 * Quick and dirty background task implementation
 * Anko probably does it better
 */
inline fun <P, T> backgroundTask(crossinline postExecute: (T) -> Unit, crossinline inBackground: (P) -> T): AsyncTask<P, Void, T> {
    val task: AsyncTask<P, Void, T> = object : AsyncTask<P, Void, T>() {

        override fun doInBackground(vararg params: P): T = inBackground(params[0])

        override fun onPostExecute(result: T) = postExecute(result)
    }
    return task
}
