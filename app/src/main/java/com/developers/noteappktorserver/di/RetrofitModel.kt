package com.developers.noteappktorserver.di

import android.util.Log
import com.developers.noteappktorserver.data.local.DataStoreManager
import com.developers.noteappktorserver.data.network.ApiNoteService
import com.developers.noteappktorserver.qualifiers.Token
import com.developers.noteappktorserver.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(ViewModelComponent::class)
object RetrofitModel {



    @Provides
    @ViewModelScoped
    @Token
    fun provideTokenUser(dataStoreManager: DataStoreManager):String = dataStoreManager.glucoseFlow.value?.token?:""



    // TODO: 11/8/2021  For implementation Moshi

    @Provides
    @ViewModelScoped
    fun providesMoshi(): Moshi = Moshi
        .Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


    // TODO: 11/8/2021  For implementation OkHttpClient

    @Provides
    @ViewModelScoped
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val localHttpLoggingInterceptor = HttpLoggingInterceptor()
        localHttpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return localHttpLoggingInterceptor
    }

    @Provides
    @ViewModelScoped
    fun provideOkHttpClient(interceptor: HttpLoggingInterceptor, @Token token: String = ""): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original: Request = chain.request()
                val builder: Request.Builder = chain.request().newBuilder()
                val newQuest = if (token.isNotEmpty()) {
                    Log.i(Constants.TAG, "provideOkHttpClient: $token")
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .header("Content-Type", "application/json")
                        .method(original.method, original.body)
                        .build()
                } else {
                    Log.i(Constants.TAG, "provideOkHttpClient: is empty")

                    original.newBuilder()
                        .header("Content-Type", "application/json")
                        .method(original.method, original.body)
                        .build()
                }
//                 builder.addHeader(Constants.CONTENT_TYPE, Constants.APP_JSON)
//                builder.method(original.method, original.body)
                // chain.proceed(builder.build())
                chain.proceed(newQuest)
            }
            .addNetworkInterceptor(interceptor)
            .build()


    // TODO: 11/8/2021  For implementation Retrofit

    @Provides
    @ViewModelScoped
    fun providesApiService(moshi: Moshi, okHttpClient: OkHttpClient): ApiNoteService =
        Retrofit.Builder()
            .run {
                baseUrl(Constants.BASE_URL)
                client(okHttpClient)
                addConverterFactory(GsonConverterFactory.create())
                build()
            }.create(ApiNoteService::class.java)

}