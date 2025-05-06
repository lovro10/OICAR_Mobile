package org.oicar.models

import com.google.gson.Gson

data class ImageDocument(
    val name: String,
    val base64Content: String,
    val imageTypeId: Int
) {
    override fun toString(): String {
        return Gson().toJson(ImageDocument(name, base64Content, imageTypeId))
    }
}