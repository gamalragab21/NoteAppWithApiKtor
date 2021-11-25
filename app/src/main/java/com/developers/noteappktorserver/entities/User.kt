package com.developers.noteappktorserver.entities




data class User(
    val id:Int?=-1,
    val username:String,
    val email:String,
    val image:String?,
    val password:String
)

