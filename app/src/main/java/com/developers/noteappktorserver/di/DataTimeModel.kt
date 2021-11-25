package com.developers.noteappktorserver.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.android.scopes.ViewModelScoped
import java.text.SimpleDateFormat
import java.util.*

@Module
@InstallIn(ViewModelComponent::class)
object DataTimeModel {

    @ViewModelScoped
    @Provides
    fun provideSimpleDateFormat(): SimpleDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())


    @ViewModelScoped
    @Provides
    fun provideDate(): Date = Date()
}