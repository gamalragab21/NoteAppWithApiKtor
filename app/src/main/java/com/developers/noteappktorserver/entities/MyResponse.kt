package com.developers.noteappktorserver.entities

import com.google.gson.annotations.SerializedName


data class MyResponse<T>(
    @SerializedName("success")
    val success:Boolean=false,
    @SerializedName("message")
    val message:String,
    @SerializedName("data")
    val data:T?=null
)