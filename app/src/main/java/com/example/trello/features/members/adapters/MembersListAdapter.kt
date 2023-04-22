package com.example.trello.features.members.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trello.R
import com.example.trello.common.models.User
import com.example.trello.databinding.MembersListItemBinding

class MembersListAdapter : RecyclerView.Adapter<MembersListAdapter.MemberViewHolder>() {
    private val membersList: MutableList<User> = mutableListOf<User>();

    class MemberViewHolder(val binding: MembersListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding =
            MembersListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return membersList.size
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.binding.userNameTxt.text = membersList[position].name
        holder.binding.userEmailTxt.text = membersList[position].email
        if(!membersList[position].profile.isNullOrBlank()) {
            Glide.with(holder.itemView.context).load(membersList[position].profile)
                .placeholder(R.drawable.splash_img)
                .into(holder.binding.userProfileImg)
        }
    }

    fun addMembers(members:List<User>){
        membersList.clear()
        membersList.addAll(members)
        notifyDataSetChanged()
    }
}