package com.example.trello.features.taskList.callbacks

import com.example.trello.common.models.Task

interface TasksCallbacks {
    fun createOrUpdateTask(task: Task, isUpdate: Boolean)
    fun createOrUpdateCard(taskId: String, task: Task,isUpdate: Boolean)
    fun deleteTask(taskId: String)
}
