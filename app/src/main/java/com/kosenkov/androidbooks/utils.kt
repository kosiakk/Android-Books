package com.kosenkov.androidbooks

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.asSequence(): Sequence<JSONObject> {
    var position = 0
    return generateSequence {
        if (position < length()) {
            getJSONObject(position++)
        } else {
            null
        }
    }
}