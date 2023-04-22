package com.example.trello.features.taskList

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.example.trello.R
import com.example.trello.common.BaseActivity
import com.example.trello.common.FirebaseKeyConstants
import com.example.trello.common.models.Board
import com.example.trello.common.models.Task
import com.example.trello.databinding.ActivityTaskListBinding
import com.example.trello.features.home.view.BoardsListScreen
import com.example.trello.features.members.BoardMembersActivity
import com.example.trello.features.taskList.adapters.TaskListViewAdapter
import com.example.trello.features.taskList.callbacks.TasksCallbacks
import com.example.trello.firebase.FirebaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class TaskListActivity : BaseActivity(), TasksCallbacks {
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var taskListAdapter: TaskListViewAdapter
    var board: Board? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        board =
            intent?.extras?.getBundle("params")?.getParcelable<Board>(BoardsListScreen.BOARD)
        assert(board != null) {
            "Board can not be null in ${activityName()}"
        }
        setSupportActionBar(binding.toolbar.commonToolbar)
        supportActionBar?.let {
            it.title = board?.name
            it.setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.commonToolbar.setNavigationOnClickListener {
            pop()
        }
        bindTaskListView()
       reloadAllTasks()
    }

    private fun bindTaskListView() {
        taskListAdapter = TaskListViewAdapter()
        binding.taskList.adapter = taskListAdapter
        taskListAdapter.addTaskCallbackListener(this)
    }

    private fun reloadAllTasks(){
        board?.id?.let {
            lifecycleScope.launch {
                showLoader()
                taskListAdapter.updateTasks(getAllTasksList(it))
                hideLoader()
            }
        }
    }
    private fun reloadSingleTask(id:String){
        board?.id?.let {boardId->
            lifecycleScope.launch {
                showLoader()
                getTask(boardId,id)?.let {
                    taskListAdapter.updateTask(it)
                }
                hideLoader()
            }
        }
    }

    private suspend fun getAllTasksList(boardId: String): List<Task> {
        val tasks = mutableListOf<Task>()
        try {
             tasks.addAll(withContext(Dispatchers.IO){
                return@withContext FirebaseHelper.getInstance().getTasksOfBoard(boardId)
            })
        } catch (exception: Exception) {
            Log.d(activityName(), exception.toString())
        }
        return tasks
    }
    private suspend fun getTask(boardId: String,taskId: String):Task? {
        return try {
            withContext(Dispatchers.IO){
                return@withContext FirebaseHelper.getInstance().getTaskOfBoard(boardId,taskId)
            }
        } catch (exception: Exception) {
            Log.d(activityName(), exception.toString())
            null
        }
    }

    private suspend fun createTask(boardId: String, task: Task) {
        try {
            withContext(Dispatchers.IO){
                return@withContext FirebaseHelper.getInstance().createTask(boardId, task)
            }
            showSuccessSnackBar(resources.getString(R.string.task_created))
        } catch (exception: Exception) {
            Log.d(activityName(), exception.toString())
            showErrorSnackBar(resources.getString(R.string.task_creation_failed))
        }
    }

    private suspend fun updateTaskInBackground(boardId: String, task: HashMap<String,Any>) {
        try {
            withContext(Dispatchers.IO) {
                return@withContext FirebaseHelper.getInstance().updateTask(boardId,task[FirebaseKeyConstants.id].toString(),task)
            }
            showSuccessSnackBar(resources.getString(R.string.task_updated))
        } catch (exception: Exception) {
            Log.d(activityName(), exception.toString())
            showErrorSnackBar(resources.getString(R.string.task_update_failed))
        }
    }

    private fun deleteTaskInBackground(taskId:String){
        try {
            lifecycleScope.launch {
                showLoader()
                withContext(Dispatchers.IO) {
                    FirebaseHelper.getInstance().deleteTask(board!!.id!!, taskId)
                }
                hideLoader()
                reloadAllTasks()
            }
            showSuccessSnackBar(resources.getString(R.string.task_delete))

        } catch (exception: Exception) {
            Log.d(activityName(), exception.toString())
            showErrorSnackBar(resources.getString(R.string.task_delete_failed))
        }
    }

    override fun activityName(): String {
        return this::class.java.simpleName
    }

    override fun createOrUpdateTask(task: Task, isUpdate: Boolean) {
        lifecycleScope.launch {
            if (board?.id != null) {
                showLoader()
                if(isUpdate){
                    val updatedValuesMap = HashMap<String,Any>()
                    updatedValuesMap[FirebaseKeyConstants.title] = task.title
                    updatedValuesMap[FirebaseKeyConstants.id] = task.id.toString()
                    updateTaskInBackground(boardId = board!!.id!!,updatedValuesMap)
                    hideLoader()
                    reloadSingleTask(task.id!!)
                }else{
                    createTask(board!!.id!!, task.copy(createdBy = getUser()!!.uid))
                    hideLoader()
                    reloadAllTasks()
                }

            }
        }
    }

    override fun createOrUpdateCard(taskId: String, task: Task,isUpdate: Boolean) {
        lifecycleScope.launch {
            if (board?.id != null) {
                showLoader()
                val updatedValuesMap = HashMap<String,Any>()
                updatedValuesMap[FirebaseKeyConstants.id] = task.id!!
                val uid = getUser()!!.uid
                task.cards?.set(task.cards!!.size-1,task.cards!!.last().copy(id = UUID.randomUUID().toString(), createdBy = uid, assignedTo = arrayListOf(uid)))
                updatedValuesMap[FirebaseKeyConstants.cards] = task.cards!!
                updateTaskInBackground(boardId = board!!.id!!,updatedValuesMap)
                hideLoader()
                reloadSingleTask(task.id)
            }
        }
    }

    override fun deleteTask(taskId: String) {
        showConfirmationDialog(resources.getString(R.string.delete_task),resources.getString(R.string.delete_task_desc)){
            deleteTaskInBackground(taskId)

        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val view = menuInflater.inflate(R.menu.members_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.members -> {
                val bundle = Bundle()
                bundle.putParcelable(BoardsListScreen.BOARD,board)
                navigateTo(BoardMembersActivity::class.java,bundle)
            }
        }
        return true
    }
}
