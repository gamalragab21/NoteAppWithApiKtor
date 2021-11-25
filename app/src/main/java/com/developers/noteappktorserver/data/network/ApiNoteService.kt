package com.developers.noteappktorserver.data.network

import com.developers.noteappktorserver.entities.MyResponse
import com.developers.noteappktorserver.entities.Note
import com.developers.noteappktorserver.entities.User
import retrofit2.http.*


interface ApiNoteService {

    @POST("users/login")
    suspend fun loginUser( @Body userinfo: HashMap<String, String>): MyResponse<String>


    @POST("users/register")
    suspend fun register( @Body userinfo: User): MyResponse<String>

    @GET("notes")
    suspend fun getMyNotes(): MyResponse<List<Note>>

    @POST("notes/create")
    suspend fun createNote( @Body note: Note): MyResponse<String>

    @DELETE("notes/delete")
    suspend fun deleteNote( @Query("id") noteId: Int): MyResponse<String>

    @PUT("notes/update")
    suspend fun updateNote(@Body note: Note): MyResponse<Note>

    @GET("users/me")
    suspend fun getMe():  MyResponse<User>


}