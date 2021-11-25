package com.developers.noteappktorserver.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developers.noteappktorserver.entities.MyResponse
import com.developers.noteappktorserver.entities.Note
import com.developers.noteappktorserver.entities.User
import com.developers.noteappktorserver.helpers.Event
import com.developers.noteappktorserver.qualifiers.MainThread
import com.developers.noteappktorserver.repositories.AuthenticationRepository
import com.developers.noteappktorserver.repositories.HomeRepository
import com.developers.noteappktorserver.repositories.NoteRepository
import com.developers.shopapp.helpers.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel@Inject constructor(
    @ApplicationContext val context: Context,
    private val repository: HomeRepository,
    private val noteRepository: NoteRepository,
    private val authRepository: AuthenticationRepository,
    @MainThread
    private val dispatcher: CoroutineDispatcher

) : ViewModel() {

    private val _notesStatus =
        MutableStateFlow<Event<Resource<MyResponse<List<Note>>>>>(Event(Resource.Init()))
    val notesStatus: MutableStateFlow<Event<Resource<MyResponse<List<Note>>>>> = _notesStatus

    private val _deleteNoteStatus =
        MutableStateFlow<Event<Resource<MyResponse<String>>>>(Event(Resource.Init()))
    val deleteNoteStatus: MutableStateFlow<Event<Resource<MyResponse<String>>>> = _deleteNoteStatus
    private val _userInfoStatus =
        MutableStateFlow<Event<Resource<MyResponse<User>>>>(Event(Resource.Init()))
    val userInfoStatus: MutableStateFlow<Event<Resource<MyResponse<User>>>> = _userInfoStatus

    init {
        getNotes()
        myProfile()
    }

    private fun myProfile(){
        viewModelScope.launch {
            userInfoStatus.emit(Event(Resource.Loading()))

            val result=authRepository.getProfile();
            userInfoStatus.emit(Event(result))

        }
    }

    fun getNotes(){
       viewModelScope.launch(dispatcher) {
           _notesStatus.emit(Event(Resource.Loading()))
           val result=repository.getNotes()
           _notesStatus.emit(Event(result))
       }
    }

    fun searchNote(toString: String) {

    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch(dispatcher) {
            _deleteNoteStatus.emit(Event(Resource.Loading()))
            val result= noteRepository.deleteNote(noteId)
            _deleteNoteStatus.emit(Event(result))
        }
    }

}