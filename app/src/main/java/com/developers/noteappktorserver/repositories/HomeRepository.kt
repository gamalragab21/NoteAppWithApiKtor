package com.developers.noteappktorserver.repositories

import com.developers.noteappktorserver.data.network.ApiNoteService
import com.developers.noteappktorserver.entities.MyResponse
import com.developers.noteappktorserver.entities.Note
import com.developers.noteappktorserver.qualifiers.IOThread
import com.developers.shopapp.helpers.Resource
import com.developers.noteappktorserver.helpers.safeCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiShopService: ApiNoteService,
    @IOThread
    private val dispatcher: CoroutineDispatcher
) {



    suspend fun getNotes() :Resource<MyResponse<List<Note>>> = withContext(dispatcher){
        safeCall {
            val result= apiShopService.getMyNotes()
            Resource.Success(result)
        }
    }

}