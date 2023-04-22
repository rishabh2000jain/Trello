package com.example.trello.firebase

import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.trello.BuildConfig
import com.example.trello.common.FirebaseKeyConstants
import com.example.trello.util.AppUtil
import com.example.trello.common.FirebasePathConstants
import com.example.trello.common.models.Board
import com.example.trello.common.models.ResponseModel
import com.example.trello.common.models.Task
import com.example.trello.common.models.User
import com.example.trello.util.fromJsonString
import com.example.trello.util.toJsonString
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.tasks.await
import okhttp3.*
import java.io.IOException
import java.util.Calendar


class FirebaseHelper private constructor() {
    private val mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val mFirebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    companion object {
        private val INSTANCE: FirebaseHelper = FirebaseHelper()
        fun getInstance() = INSTANCE
    }

    suspend fun createUser(user: User): ResponseModel<User, String> {
        return try {
            val ref = FirebaseFirestore.getInstance().collection(FirebasePathConstants.users)
                .document(user.id)
            ref.set(user).await()
            ResponseModel(user)
        } catch (exception: java.lang.Exception) {
            AppUtil.log(this::class.java, exception.message.toString())
            ResponseModel()
        }
    }

    suspend fun createBoard(board: Board): ResponseModel<Unit, String> {
        return try {
            val ref = FirebaseFirestore.getInstance().collection(FirebasePathConstants.board)
                .document()
            val boardWithId = board.copy(id = ref.id)
            ref.set(boardWithId).await()
            ResponseModel(success = true)
        } catch (exception: java.lang.Exception) {
            AppUtil.log(this::class.java, exception.message.toString())
            ResponseModel(success = false)
        }
    }

    suspend fun getUserDetails(userId: String): User {
        val snapshot =
            mFirestore.collection(FirebasePathConstants.users).document(userId).get().await().data
        return User(
            snapshot?.get("id").toString(),
            snapshot?.get("name").toString(),
            snapshot?.get("email")?.toString(),
            snapshot?.get("phone")?.toString(),
            snapshot?.get("profile")?.toString(),
            fcmToken = snapshot?.get(FirebaseKeyConstants.fcmToken)?.toString()
        )

    }

    suspend fun getUserDetailsByEmail(email: String): User {
        val snapshot =
            mFirestore.collection(FirebasePathConstants.users)
                .where(Filter.equalTo(FirebaseKeyConstants.email, email)).limit(1).get()
                .await().documents.first().data
        return User(
            snapshot?.get("id").toString(),
            snapshot?.get("name").toString(),
            snapshot?.get("email")?.toString(),
            snapshot?.get("phone")?.toString(),
            snapshot?.get("profile")?.toString(),
            fcmToken = snapshot?.get(FirebaseKeyConstants.fcmToken)?.toString()
        )

    }

    suspend fun uploadProfileImage(uri: Uri, extension: String): String? {
        return uploadImage(
            uri,
            "${FirebasePathConstants.storageProfileImagePath}/USER_PROFILE_${System.currentTimeMillis()}.${extension}"
        )
    }

    suspend fun uploadBoardImage(uri: Uri, extension: String): String? {
        return uploadImage(
            uri,
            "${FirebasePathConstants.storageBoardImagePath}/BOARD_IMAGE_${System.currentTimeMillis()}.${extension}"
        )
    }

    private suspend fun uploadImage(uri: Uri, fileNameWithPath: String): String? {
        val profileReference =
            mFirebaseStorage.reference.child(fileNameWithPath)
        profileReference.putFile(uri).await()
        return profileReference.downloadUrl.await().toString()
    }

    suspend fun updateUser(id: String, user: Map<String, String>): User {
        val ref =
            FirebaseFirestore.getInstance().collection(FirebasePathConstants.users).document(id)
        ref.update(user).await()

        return getUserDetails(id)
    }

    suspend fun getBoardList(uid: String): List<Board> {
        val snapshot = mFirestore.collection(FirebasePathConstants.board)
            .where(
                Filter.or(
                    Filter.arrayContains(FirebaseKeyConstants.assignedTo, uid),
                    Filter.equalTo(FirebaseKeyConstants.createdBy, uid)
                )
            )
            .get()
            .await()
        val boardList: List<Board> = snapshot.documents.map {
            it.data!!.toJsonString<Board>().fromJsonString(Board::class.java)
        }
        return boardList
    }

    suspend fun createTask(boardId: String, task: Task) {
        val taskRef = mFirestore.collection(FirebasePathConstants.board).document(boardId)
            .collection(FirebasePathConstants.taskListPath).document()
        taskRef.set(task.copy(id = taskRef.id, createdAt = Calendar.getInstance().time.time))
            .await()
    }

    suspend fun getTasksOfBoard(boardId: String): List<Task> {
        val taskListSnapshot = mFirestore.collection(FirebasePathConstants.board).document(boardId)
            .collection(FirebasePathConstants.taskListPath).orderBy(FirebaseKeyConstants.createdAt)
            .get().await()
        return taskListSnapshot.documents.map {
            it.data!!.toJsonString<Task>().fromJsonString(Task::class.java)
        }
    }

    suspend fun getBoardDetails(boardId: String): Board {
        val taskListSnapshot =
            mFirestore.collection(FirebasePathConstants.board).document(boardId).get().await()
        return taskListSnapshot.data!!.toJsonString<Board>().fromJsonString(Board::class.java)
    }

    suspend fun getTaskOfBoard(boardId: String, taskId: String): Task? {
        val result = mFirestore.collection(FirebasePathConstants.board).document(boardId)
            .collection(FirebasePathConstants.taskListPath).document(taskId).get().await()
        return result.data?.toJsonString<Task>()?.fromJsonString(Task::class.java)
    }

    suspend fun updateTask(boardId: String, taskId: String, task: Map<String, Any>) {
        val taskRef = mFirestore.collection(FirebasePathConstants.board).document(boardId)
            .collection(FirebasePathConstants.taskListPath).document(taskId)
        taskRef.update(task).await()
    }

    suspend fun deleteTask(boardId: String, taskId: String) {
        val taskRef = mFirestore.collection(FirebasePathConstants.board).document(boardId)
            .collection(FirebasePathConstants.taskListPath).document(taskId)
        taskRef.delete().await()
    }

    suspend fun updateBoard(id: String, data: Map<String, Any>) {
        mFirestore.collection(FirebasePathConstants.board).document(id).update(data).await()
    }

    fun sendPushNotification(to: String, title: String, message: String, callbacks: Callback) {
        val httpRequestBuilder = Request.Builder()
        httpRequestBuilder.url(BuildConfig.FIREBASE_PUSH_URL)
        val requestBody = PushRequestBody(
            to,
            NotificationBody(title, message, true, "Tri-tone")
        ).toJsonString<PushRequestBody>()
        val json = MediaType.parse("application/json; charset=utf-8");
        httpRequestBuilder.header("Authorization","key=${BuildConfig.FIREBASE_SERVER_KEY}")
        val request = httpRequestBuilder.post(RequestBody.create(json, requestBody)).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(callbacks)
    }

}

data class PushRequestBody(val to: String, val notification: NotificationBody)
data class NotificationBody(
    val title: String,
    val body: String,
    @SerializedName("mutable_content")
    val mutableContent: Boolean,
    val sound: String
)