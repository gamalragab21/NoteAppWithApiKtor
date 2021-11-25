package com.developers.noteappktorserver.repositories

import com.developers.noteappktorserver.data.network.ApiNoteService
import com.developers.noteappktorserver.entities.MyResponse
import com.developers.noteappktorserver.entities.Note
import com.developers.noteappktorserver.helpers.safeCall
import com.developers.noteappktorserver.qualifiers.IOThread
import com.developers.shopapp.helpers.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val apiShopService: ApiNoteService,
    @IOThread
    private val dispatcher: CoroutineDispatcher,
    private var simpleDateFormat : SimpleDateFormat,
    private var date: Date
)  {

    suspend fun getTimeAndData():String = withContext(dispatcher) {
            simpleDateFormat.format(date)
    }

    suspend fun setColorIndicator(color:String):String = withContext(dispatcher) {
        color
    }

    suspend fun insertNote(note: Note): Resource<MyResponse<String>> = withContext(dispatcher) {
      safeCall {
          val result=apiShopService.createNote(note)

          Resource.Success(result)
      }
    }

    suspend fun deleteNote(noteId: Int): Resource<MyResponse<String>> = withContext(dispatcher) {
        safeCall {
            val result=apiShopService.deleteNote(noteId)
            Resource.Success(result)
        }
    }

   suspend fun updateNote(note: Note): Resource<MyResponse<Note>> = withContext(dispatcher){
       safeCall {
           val result=apiShopService.updateNote(note)
           Resource.Success(result)
       }
    }


}