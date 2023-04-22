package com.example.trello.common

interface OnRecyclerViewItemClick<T> {
    fun onClick(item:T)
}