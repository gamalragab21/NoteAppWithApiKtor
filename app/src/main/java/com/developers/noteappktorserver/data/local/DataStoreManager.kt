package com.developers.noteappktorserver.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.developers.noteappktorserver.entities.UserInfoDB
import com.developers.noteappktorserver.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(Constants.USERS_INFO_FILE)

class DataStoreManager(appContext: Context) {

    private val tokenDataStore = appContext.dataStore
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val _glucoseFlow = MutableLiveData<UserInfoDB>()
    // For public variables, prefer use LiveData just to read values.
    val glucoseFlow: LiveData<UserInfoDB> get() = _glucoseFlow

    suspend fun setUserInfo(email: String?=null,password:String?=null,token:String?=null) {
        tokenDataStore.edit {preferences->
            email?.let {email->
                preferences[Constants.USER_EMAIL] = email
            }
            password?.let{password->
                preferences[Constants.USER_PASSWORD] = password
            }
            token?.let {token->
                preferences[Constants.USER_TOKEN] = token
            }
        }
    }

    val infoUser: Flow<UserInfoDB> = tokenDataStore.data.map { preferences ->
        UserInfoDB(
            preferences[Constants.USER_EMAIL] ?: "",
            preferences[Constants.USER_PASSWORD] ?: "",
            preferences[Constants.USER_TOKEN] ?: ""
        )

    }


    init {
        getTokenUser()
    }

    private fun getTokenUser() {
        scope.launch {
            infoUser.collect { token ->
                _glucoseFlow.postValue(token)
            }
        }

    }







}