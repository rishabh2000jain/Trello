package com.example.trello.common.models

import android.os.Parcel
import android.os.Parcelable

data class Task(
    val id: String?=null,
    val title: String,
    val createdBy:String?=null,
    var createdAt: Long?=null,
    var cards: ArrayList<Card>?=ArrayList()
):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.createTypedArrayList(Card.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(createdBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }

}
