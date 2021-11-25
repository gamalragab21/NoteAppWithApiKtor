package com.developers.noteappktorserver.ui.viewmodels

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developers.noteappktorserver.R
import com.developers.noteappktorserver.entities.MyResponse
import com.developers.noteappktorserver.entities.Note
import com.developers.noteappktorserver.helpers.Event
import com.developers.noteappktorserver.qualifiers.MainThread
import com.developers.noteappktorserver.repositories.NoteRepository
import com.developers.noteappktorserver.utils.Constants.MAX_SUBTITLE_LENGTH
import com.developers.noteappktorserver.utils.Constants.MAX_TITLENOTE_LENGTH
import com.developers.noteappktorserver.utils.Constants.MIN_SUBTITLE_LENGTH
import com.developers.noteappktorserver.utils.Constants.MIN_TITLENOTE_LENGTH
import com.developers.shopapp.helpers.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val repository : NoteRepository,
    private val context : Context,
    @MainThread
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _createNoteStatus = MutableStateFlow<Event<Resource<MyResponse<String>>>>(Event(Resource.Init()))
    val createNoteStatus : MutableStateFlow<Event<Resource<MyResponse<String>>>> = _createNoteStatus

    private val _deleteNoteStatus =
        MutableStateFlow<Event<Resource<MyResponse<String>>>>(Event(Resource.Init()))
    val deleteNoteStatus: MutableStateFlow<Event<Resource<MyResponse<String>>>> = _deleteNoteStatus

    private val _updateNoteStatus =
        MutableStateFlow<Event<Resource<MyResponse<Note>>>>(Event(Resource.Init()))
    val updateNoteStatus: MutableStateFlow<Event<Resource<MyResponse<Note>>>> = _updateNoteStatus


    private val _timeStatus = MutableLiveData<String>()
    val timeStatus : LiveData<String> = _timeStatus


    private val _curImageUri = MutableLiveData<Uri>()
    val curImageUri : LiveData<Uri> = _curImageUri

    private val _colorIndicatorStatus = MutableLiveData<String>()
    val colorIndicatorStatus : LiveData<String> = _colorIndicatorStatus

    private val _webLinkStatus = MutableStateFlow<Event<Resource<String>>>(Event(Resource.Init()))
    val webLinkStatus : MutableStateFlow<Event<Resource<String>>> = _webLinkStatus

    fun setColorIndicatorStatus(color : String) {
        viewModelScope.launch(dispatcher) {
            val result = repository.setColorIndicator(color)
            _colorIndicatorStatus.postValue(result)
        }
    }


    fun setWebLink(url : String , dialoge : Dialog?) {
        viewModelScope.launch(dispatcher) {
            url.let {
                if (url.isNotEmpty()) {
                    if (!Patterns.WEB_URL.matcher(url).matches()) {
                        val error = context.getString(R.string.invalid_web_link)
                        _webLinkStatus.emit(Event(Resource.Error(error)))
                    }else{
                        webLinkStatus.emit(Event(Resource.Loading()))
                        viewModelScope.launch(dispatcher) {
                            _webLinkStatus.emit(Event(Resource.Success(url)))
                        }
                    }
                }else {
                    _webLinkStatus.emit(Event(Resource.Loading()))
                    viewModelScope.launch(dispatcher) {
                        _webLinkStatus.emit(Event(Resource.Success(url)))
                    }
                }
                dialoge?.dismiss()

            }
        }
    }

    fun clearUri() {
        viewModelScope.launch {
            _webLinkStatus.emit(Event(Resource.Success("")))
        }
    }


    fun setCurImageUri(uri : Uri?) {
        uri?.let {uri->
            viewModelScope.launch(dispatcher) {
                _curImageUri.postValue(uri)

            }
        }

    }

    private fun getTimeAndData() {
        viewModelScope.launch(dispatcher) {
            val result = repository.getTimeAndData()
            _timeStatus.postValue(result)
        }

    }


    fun saveNote(inputTitle : EditText? , inputSubtitle : EditText? , input_note : EditText) {
       viewModelScope.launch(dispatcher) {
           when {
               inputTitle?.text.toString().trim().isEmpty() -> {
                   val error = context.getString(R.string.input_title_empty)
                   _createNoteStatus.emit(Event(Resource.Error(error)))
                   inputTitle?.requestFocus()
               }
               inputSubtitle?.text.toString().trim().isEmpty() -> {
                   val error = context.getString(R.string.input_subtitle_empty)
                   _createNoteStatus.emit(Event(Resource.Error(error)))
                   inputSubtitle?.requestFocus()
               }
               inputTitle?.text.toString().length < MIN_TITLENOTE_LENGTH -> {
                   val error =
                       context.getString(R.string.error_titlenote_too_short, MIN_TITLENOTE_LENGTH)
                   _createNoteStatus.emit(Event(Resource.Error(error)))
                   inputTitle?.requestFocus()
               }
               inputTitle?.text.toString().length > MAX_TITLENOTE_LENGTH -> {
                   val error =
                       context.getString(R.string.error_titlenote_too_long, MAX_TITLENOTE_LENGTH)
                   _createNoteStatus.emit(Event(Resource.Error(error)))
                   inputTitle?.requestFocus()
               }
               inputSubtitle?.text.toString().length < MIN_SUBTITLE_LENGTH -> {
                   val error =
                       context.getString(R.string.error_subtille_too_short, MIN_SUBTITLE_LENGTH)
                   _createNoteStatus.emit(Event(Resource.Error(error)))
                   inputSubtitle?.requestFocus()
               }
               inputSubtitle?.text.toString().length > MAX_SUBTITLE_LENGTH -> {
                   val error =
                       context.getString(R.string.error_subtitle_too_long, MAX_SUBTITLE_LENGTH)
                   _createNoteStatus.emit(Event(Resource.Error(error)))
                   inputSubtitle?.requestFocus()
               }
               input_note?.text.toString().trim().isEmpty() -> {
                   val error =
                       context.getString(R.string.input_note_empty)
                   _createNoteStatus.emit(Event(Resource.Error(error)))
                   inputSubtitle?.requestFocus()
               }
               else -> {
                   _createNoteStatus.emit(Event(Resource.Loading()))
                   viewModelScope.launch(dispatcher) {
                       val note = Note(
                           id=-1,
                           title = inputTitle?.text.toString(),
                           dataTime = timeStatus.value?:"",
                           subTitle = inputSubtitle?.text.toString(),
                           color = colorIndicatorStatus.value?:"",
                           // imagePath = curImageUri.value?.peekContent()?.data.toString()
                           imagePath = curImageUri.value?.toString(),
                           webLink = webLinkStatus.value.peekContent().data,
                           note = input_note.text.toString(),userId = -1
                       )

                       val result = repository.insertNote(note)
                       _createNoteStatus.emit(Event(result))
                   }
               }
           }
       }
    }

    fun updateNote(
        id:Int,
        inputTitle : EditText? ,
        inputSubtitle : EditText? ,
        input_note : EditText
    ) {
        viewModelScope.launch(dispatcher) {
            when {
                inputTitle?.text.toString().trim().isEmpty() -> {
                    val error = context.getString(R.string.input_title_empty)
                    _updateNoteStatus.emit(Event(Resource.Error(error)))
                    inputTitle?.requestFocus()
                }
                inputSubtitle?.text.toString().trim().isEmpty() -> {
                    val error = context.getString(R.string.input_subtitle_empty)
                    _updateNoteStatus.emit(Event(Resource.Error(error)))
                    inputSubtitle?.requestFocus()
                }
                inputTitle?.text.toString().length < MIN_TITLENOTE_LENGTH -> {
                    val error =
                        context.getString(R.string.error_titlenote_too_short, MIN_TITLENOTE_LENGTH)
                    _updateNoteStatus.emit(Event(Resource.Error(error)))
                    inputTitle?.requestFocus()
                }
                inputTitle?.text.toString().length > MAX_TITLENOTE_LENGTH -> {
                    val error =
                        context.getString(R.string.error_titlenote_too_long, MAX_TITLENOTE_LENGTH)
                    _updateNoteStatus.emit(Event(Resource.Error(error)))
                    inputTitle?.requestFocus()
                }
                inputSubtitle?.text.toString().length < MIN_SUBTITLE_LENGTH -> {
                    val error =
                        context.getString(R.string.error_subtille_too_short, MIN_SUBTITLE_LENGTH)
                    _updateNoteStatus.emit(Event(Resource.Error(error)))
                    inputSubtitle?.requestFocus()
                }
                inputSubtitle?.text.toString().length > MAX_SUBTITLE_LENGTH -> {
                    val error =
                        context.getString(R.string.error_subtitle_too_long, MAX_SUBTITLE_LENGTH)
                    _updateNoteStatus.emit(Event(Resource.Error(error)))
                    inputSubtitle?.requestFocus()
                }
                input_note?.text.toString().trim().isEmpty() -> {
                    val error =
                        context.getString(R.string.input_note_empty)
                    _updateNoteStatus.emit(Event(Resource.Error(error)))
                    inputSubtitle?.requestFocus()
                }
                else -> {
                    Log.i("aly", "saveNote: ${webLinkStatus.value?.peekContent()?.data}")
                    _updateNoteStatus.emit(Event(Resource.Loading()))
                    viewModelScope.launch(dispatcher) {
                        val note = Note(
                            id=id,
                            title = inputTitle?.text.toString(),
                            dataTime = timeStatus.value?:"",
                            subTitle = inputSubtitle?.text.toString(),
                            color = colorIndicatorStatus.value?:"",
                            // imagePath = curImageUri.value?.peekContent()?.data.toString()
                            imagePath = curImageUri.value?.toString() ?: "",
                            webLink = webLinkStatus.value?.peekContent()?.data ?: "",
                            note = input_note.text.toString()
                        ,userId = -1
                        )


                        val result = repository.updateNote(note)
                        _updateNoteStatus.emit(Event(result))
                    }
                }
            }
        }
    }
//
//    fun delete(note: Int) {
//        _deleteNoteStatus.postValue(Event(Resource.Loading()))
//        viewModelScope.launch(dispatcher) {
//        note?.let {
//            val result =  repository.delete(it)
//            _updateNoteStatus.postValue(Event(result))
//        }
//
//    }
//    }

    init {
        getTimeAndData()
    }

    override fun onCleared() {
        super.onCleared()

    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch(dispatcher) {
            _deleteNoteStatus.emit(Event(Resource.Loading()))
            val result= repository.deleteNote(noteId)
            _deleteNoteStatus.emit(Event(result))
        }
    }



}