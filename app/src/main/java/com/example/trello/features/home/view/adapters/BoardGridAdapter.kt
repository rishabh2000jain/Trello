package com.example.trello.features.home.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trello.common.models.Board
import com.example.trello.databinding.BoardListItemBinding
import androidx.recyclerview.widget.DiffUtil
import com.example.trello.R
import com.example.trello.common.OnRecyclerViewItemClick
import com.example.trello.common.models.User
import com.example.trello.features.home.view.FetchUserDetailsCallback

class BoardGridAdapter() : RecyclerView.Adapter<BoardGridAdapter.BoardViewHolder>() {
    private val boardList: MutableList<Board> = mutableListOf()
    private val userDetailMap = mutableMapOf<String,User>()
   private var fetchUserDetailsCallback: FetchUserDetailsCallback?=null
    var itemClickListener:OnRecyclerViewItemClick<Board>?=null
    set(value) {
        field = value
    }

    fun registerFetchUserCallback(fetchUserDetailsCallback: FetchUserDetailsCallback){
        this.fetchUserDetailsCallback = fetchUserDetailsCallback
    }

    inner class BoardViewHolder(val viewBinding: BoardListItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root){
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val viewBinding =
            BoardListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoardViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return boardList.size
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.viewBinding.boardName.text = boardList[position].name
        val createdByName = userDetailMap[boardList[position].createdBy]?.name.orEmpty()
        if(createdByName.isBlank()){
            holder.viewBinding.creatorNameLoader.visibility = View.VISIBLE
            holder.viewBinding.createdByTxt.visibility = View.GONE
            boardList[position].createdBy?.let { fetchUserDetailsCallback?.fetchDetails(it) }
        }else{
            holder.viewBinding.creatorNameLoader.visibility = View.GONE
            holder.viewBinding.createdByTxt.visibility = View.VISIBLE
            holder.viewBinding.createdByTxt.text = createdByName
        }
        if(!boardList[position].image.isNullOrBlank()) {
            Glide.with(holder.viewBinding.root.context)
                .load(boardList[position].image)
                .centerCrop()
                .placeholder(R.drawable.splash_img)
                .into(holder.viewBinding.boardImg)
        }
        holder.viewBinding.root.setOnClickListener {
            itemClickListener?.onClick(boardList[position])
        }
    }
    fun updateBoardList(boardList:List<Board>){
        val boardListDiffUtil = BoardListDiffUtil(this.boardList,boardList)
        val diffResult:DiffUtil.DiffResult = DiffUtil.calculateDiff(boardListDiffUtil)
        this.boardList.clear()
        this.boardList.addAll(boardList)
        diffResult.dispatchUpdatesTo(this)
    }
    fun setUserDetails(user:User){
        userDetailMap[user.id] = user
        boardList.forEachIndexed { index, board ->
            if(board.createdBy == user.id){
                notifyItemChanged(index)
            }
        }
    }

}

class BoardListDiffUtil(private val oldList:List<Board>, private val newList:List<Board>) : DiffUtil.Callback(){
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}

