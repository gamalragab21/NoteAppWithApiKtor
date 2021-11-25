package com.developers.noteappktorserver.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Call
import okhttp3.ResponseBody
import retrofit2.HttpException;
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*

fun isNetworkConnected(@ApplicationContext context: Context): Flow<Boolean> = flow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm?.let {
            val activeNetwork = cm.activeNetworkInfo
            emit(activeNetwork != null && activeNetwork.isConnectedOrConnecting)
        }

        emit(false)
    }

fun setupTheme(isDarkMode: Boolean) {
    if (isDarkMode) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

fun errorMessageHandler(call: Call, t: Throwable): Flow<String?> =flow{
    if (t is SocketTimeoutException) {
        emit( "Connection timeout, Please try again!")
    } else if (t is HttpException) {
        val body: ResponseBody = (t as HttpException).response()?.errorBody()!!
        try {
            emit( body.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    } else  if (t is IOException) {
       emit( "Request timeout, Please try again!")
    } else {
        //Call was cancelled by user
        if (call.isCanceled()) {
            emit( "Call was cancelled forcefully, Please try again!")
        } else {
           emit( "Network Problem, Please try again!")
        }
    }
    emit( "Network Problem, Please try again!")
}

fun dateFormatter(Date: String?): Long {

    Date?.let {

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        val date: Date = formatter.parse(Date)
        return date.time
    }
    return 0
}


