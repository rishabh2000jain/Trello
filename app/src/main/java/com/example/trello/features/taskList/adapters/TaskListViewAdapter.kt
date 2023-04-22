package com.example.trello.features.taskList.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trello.R
import com.example.trello.common.models.Card
import com.example.trello.common.models.Task
import com.example.trello.databinding.ItemTaskBinding
import com.example.trello.features.taskList.callbacks.TasksCallbacks
import java.util.UUID

class TaskListViewAdapter : RecyclerView.Adapter<TaskListViewAdapter.TaskViewHolder>() {
    private var taskCallback: TasksCallbacks? = null
    private val taskList: MutableList<Task> = mutableListOf()
    fun updateTasks(taskList: List<Task>) {
        this.taskList.clear()
        this.taskList.addAll(taskList)
        notifyDataSetChanged()
    }
    fun updateTask(task:Task) {
        val taskIndex = taskList.indexOfFirst {
            it.id == task.id
        }
        if(taskIndex!=-1){
            taskList[taskIndex]=task
            notifyItemChanged(taskIndex)
        }
    }

    fun addTaskCallbackListener(tasksCallback: TasksCallbacks) {
        this.taskCallback = tasksCallback
    }

    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun enterAddListMode() {
            binding.addListBtn.visibility = View.GONE
            binding.listTitleLayout.visibility = View.VISIBLE
            binding.listTitleEdt.addTextChangedListener(listTitleTextWatcher)

        }

        fun exitAddListMode() {
            binding.addListBtn.visibility = View.VISIBLE
            binding.listTitleLayout.visibility = View.GONE
            binding.listTitleLayout.error = null
            binding.listTitleEdt.removeTextChangedListener(listTitleTextWatcher)
            binding.listTitleEdt.text?.clear()
        }

        fun enterAddCardMode() {
            binding.addCardBtn.visibility = View.GONE
            binding.addCardEdtLayout.visibility = View.VISIBLE
            binding.addCardEdt.addTextChangedListener(cardTitleTextWatcher)

        }

        fun exitAddCardMode() {
            binding.addCardBtn.visibility = View.VISIBLE
            binding.addCardEdtLayout.visibility = View.GONE
            binding.addCardEdtLayout.error = null
            binding.addCardEdt.removeTextChangedListener(cardTitleTextWatcher)
            binding.addCardEdt.text?.clear()
        }

        fun enterEditTaskTitleMode() {
            binding.listTitleLayout.visibility = View.VISIBLE
            binding.listTitleGrp.visibility = View.GONE
            binding.listTitleEdt.setText(taskList[layoutPosition].title)
            binding.listTitleEdt.addTextChangedListener(listTitleTextWatcher)
        }

        fun exitEditTaskTitleMode() {
            binding.listTitleLayout.visibility = View.GONE
            binding.listTitleGrp.visibility = View.VISIBLE
            binding.listTitleEdt.removeTextChangedListener(listTitleTextWatcher)
            binding.listTitleEdt.text?.clear()
            binding.listTitleLayout.error = null
        }

        fun setLastTaskListItemView() {
            binding.addListBtn.visibility = View.VISIBLE
            binding.listTitleGrp.visibility = View.GONE
            binding.listTitleLayout.visibility = View.GONE
            binding.cardList.visibility = View.GONE
            binding.addCardBtn.visibility = View.GONE
            binding.addCardEdtLayout.visibility = View.GONE
        }

        fun setNonLastTaskListItemView() {
            binding.listTitleLayout.visibility = View.GONE
            binding.addListBtn.visibility = View.GONE
            binding.addCardEdtLayout.visibility = View.GONE
            binding.cardList.visibility = View.VISIBLE
            binding.listTitleGrp.visibility = View.VISIBLE
            binding.addCardBtn.visibility = View.VISIBLE
            binding.cardList.visibility = View.VISIBLE
        }

        private val listTitleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    binding.listTitleLayout.error =
                        binding.root.context.resources.getString(
                            R.string.empty_title_error
                        )
                }else if(binding.listTitleLayout.error!=null){
                    binding.listTitleLayout.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        private val cardTitleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    binding.addCardEdtLayout.error =
                        binding.root.context.resources.getString(
                            R.string.empty_title_error
                        )
                }else if(binding.addCardEdtLayout.error!=null){
                    binding.addCardEdtLayout.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size + 1
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val isLastTask = position >= taskList.size
        if (isLastTask) {
            holder.setLastTaskListItemView()
        } else {
            holder.binding.listTitleTxt.text = taskList[position].title
            holder.setNonLastTaskListItemView()
            val cardAdapter = CardListAdapter()
            holder.binding.cardList.layoutManager = LinearLayoutManager(holder.binding.root.context)
            holder.binding.cardList.adapter = cardAdapter
            cardAdapter.updateCards(taskList[position].cards ?: ArrayList())
            holder.binding.addCardBtn.setOnClickListener {
                holder.enterAddCardMode()
            }
            holder.binding.addCardEdtLayout.setStartIconOnClickListener {
                holder.exitAddCardMode()
            }

            holder.binding.addCardEdtLayout.setEndIconOnClickListener {
                if (holder.binding.addCardEdt.text.isNullOrBlank()) return@setEndIconOnClickListener
                val cards = taskList[position].cards?: ArrayList()
                cards.add(Card(title = holder.binding.addCardEdt.text.toString(),
                    ),)
                taskCallback?.createOrUpdateCard(
                    taskId = taskList[position].id.toString(),
                    task = taskList[position].copy(cards = cards),
                    isUpdate = false
                )
                holder.exitAddCardMode()
            }
        }
        holder.binding.listTitleLayout.setStartIconOnClickListener {
            if (isLastTask) {
                holder.exitAddListMode()
            } else {
                holder.exitEditTaskTitleMode()
            }
        }
        holder.binding.listTitleLayout.setEndIconOnClickListener {
            if (holder.binding.listTitleEdt.text.isNullOrBlank()) return@setEndIconOnClickListener
            if (isLastTask || holder.binding.listTitleEdt.text.toString() != taskList[position].title) {
                taskCallback?.createOrUpdateTask(
                    task = if (isLastTask) Task(
                        title = holder.binding.listTitleEdt.text.toString()
                    ) else taskList[position].copy(title = holder.binding.listTitleEdt.text.toString()),
                    isUpdate = !isLastTask
                )
            }
            if (isLastTask) {
                holder.exitAddListMode()
            } else {
                holder.exitEditTaskTitleMode()
            }
        }

        holder.binding.editTitleBtn.setOnClickListener {
            holder.enterEditTaskTitleMode()
        }
        holder.binding.addListBtn.setOnClickListener {
            holder.enterAddListMode()
        }
        holder.binding.deleteListBtn.setOnClickListener {
            taskCallback?.deleteTask(taskId = taskList[position].id.toString())
        }
    }
}