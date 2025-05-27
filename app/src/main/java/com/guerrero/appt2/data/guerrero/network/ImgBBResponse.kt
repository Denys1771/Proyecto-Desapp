package com.guerrero.appt2.data.guerrero.network

data class ImgBBResponse(
    val data: ImgBBData,
    val success: Boolean,
    val status: Int
)

data class ImgBBData(
    val url: String,
    val delete_url: String
)