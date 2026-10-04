package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "user") val user: String,
    @Json(name = "pass") val pass: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "token") val token: String? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class AddHistoryRequest(
    @Json(name = "token") val token: String? = null,
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "positionMs") val positionMs: Long = 0L
)

@JsonClass(generateAdapter = true)
data class HistoryItemRemote(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "positionMs") val positionMs: Long? = 0L
)

data class UserSession(
    val username: String = "",
    val token: String? = null,
    val isLoggedIn: Boolean = false,
    val isOfflineMode: Boolean = true
)
