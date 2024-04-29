package com.example.wgn_igloo.notifications

data class SendRequestDto(
    val to: String?,
    val notification: NotificationBody
)

data class NotificationBody(
    val title: String,
    val body: String
)