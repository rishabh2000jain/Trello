package com.example.trello.common.models

import android.os.Parcel
import android.os.Parcelable

data class Board(
    val id:String?=null,
    val createdBy: String?,
    val image: String?,
    val assignedTo: List<String>?,
    val name: String?
):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(createdBy)
        parcel.writeString(image)
        parcel.writeStringList(assignedTo)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Board> {
        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }


}
