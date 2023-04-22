package com.example.trello.features.taskList.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trello.common.models.Card
import com.example.trello.databinding.CardListItemBinding

class CardListAdapter() : RecyclerView.Adapter<CardListAdapter.CardListViewHolder>() {
    var cardList = mutableListOf<Card>()

    fun updateCards(cards: List<Card>) {
        this.cardList.clear()
        this.cardList.addAll(cards)
        notifyDataSetChanged()
    }

    inner class CardListViewHolder(val binding: CardListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardListViewHolder {
        val binding =
            CardListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(holder: CardListViewHolder, position: Int) {
        holder.binding.titleTxt.text = cardList[position].title
    }
}