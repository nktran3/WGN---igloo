package com.example.wgn_igloo.inbox

import com.google.firebase.Timestamp

data class Notifications(
    val title: String = "",
    val message: String ="",
    val timeCreated: Timestamp = Timestamp.now()
)
