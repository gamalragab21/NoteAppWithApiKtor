package com.developers.noteappktorserver.entities

data class UserInfoDB(
    val email:String="",
    val password:String="",
    val token:String=""
){

    override fun toString(): String {
        return "email:${email},password:${password},token ${token}"
    }
}