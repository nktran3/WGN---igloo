package com.example.wgn_igloo.notifications

import retrofit2.http.Body
import retrofit2.http.POST

interface FcmAPI {

    @POST("/send")
    suspend fun sendRequest(
        @Body body: SendRequestDto
    )

    @POST("/broadcast")
    suspend fun broadcast(
        @Body body: SendRequestDto
    )
}