package com.developers.noteappktorserver.entities

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Note(
    val id: Int?,
    val title: String,
    val subTitle: String,
    val dataTime: String,
    val imagePath: String?=null,
    val note: String,
    val color: String="#333333",
    val webLink: String?=null,
    val userId: Int?
):Parcelable