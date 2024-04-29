package com.example.wgn_igloo.notifications

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import retrofit2.Retrofit
import retrofit2.create
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

private const val TAG = "RequestViewModel"
class RequestViewModel: ViewModel() {

    var state by mutableStateOf(RequestState())
        private set

    private val api: FcmAPI = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()

    fun onReceiverTokenChange(newToken: String) {
        state = state.copy(
            receiverToken = newToken
        )
        Log.d(TAG, "Receiver: $newToken")
    }

    fun onSenderTokenChange(newToken: String) {
        state = state.copy(
            senderToken = newToken
        )
        Log.d(TAG, "Sender: $newToken")
    }

    fun onItemNameChange(item: String) {
        state = state.copy(
            itemName = item
        )
        Log.d(TAG, "Item Name: $item")
    }

    fun sendRequest(){
        viewModelScope.launch {
            val requestDto = SendRequestDto(
                to = state.receiverToken,
                notification = NotificationBody(
                    title = "New Message",
                    body = state.itemName
                )
            )

            try {
                api.sendRequest(requestDto)
                state = state.copy(
                    itemName = ""
                )

            } catch(e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}