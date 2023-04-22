package com.example.trello.features.members

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trello.R
import com.example.trello.common.BaseActivity
import com.example.trello.common.FirebaseKeyConstants
import com.example.trello.common.models.Board
import com.example.trello.common.models.User
import com.example.trello.databinding.ActivityBoardMembersBinding
import com.example.trello.databinding.AddMemberViewBinding
import com.example.trello.features.home.view.BoardsListScreen
import com.example.trello.features.members.adapters.MembersListAdapter
import com.example.trello.firebase.FirebaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class BoardMembersActivity : BaseActivity() {
    private var board: Board? = null
    lateinit var membersListAdapter: MembersListAdapter
    private lateinit var binding: ActivityBoardMembersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardMembersBinding.inflate(layoutInflater)
        board =
            intent?.extras?.getBundle("params")?.getParcelable<Board>(BoardsListScreen.BOARD)
        assert(board != null) {
            "Board can not be null in ${activityName()}"
        }
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.commonToolbar)
        supportActionBar?.let {
            it.title = resources.getString(R.string.members)
            it.setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.commonToolbar.setNavigationOnClickListener {
            pop()
        }
        initRecyclerView()
        fetchMembersAndLoadInRecyclerView(boardId = board!!.id!!)
        addClickListeners()
    }

    private fun initRecyclerView() {
        membersListAdapter = MembersListAdapter()
        with(binding.membersList) {
            adapter = membersListAdapter
            layoutManager = LinearLayoutManager(this@BoardMembersActivity)
        }
    }

    private fun addClickListeners() {
        binding.addMemberBtn.setOnClickListener {
            showAddMemberDialog()
        }
    }

    private fun fetchMembersAndLoadInRecyclerView(boardId: String) {
        lifecycleScope.launch {
            showLoader()
            refreshBoard()
            try {
                membersListAdapter.addMembers(getBoardMembersList(boardId))
            } catch (exception: Exception) {
                showErrorSnackBar(resources.getString(R.string.failed_to_fetch_data))
            } finally {
                hideLoader()
            }
        }
    }


    private suspend fun getBoardMembersList(boardId: String): List<User> {
        return withContext(Dispatchers.IO) {
            val members: MutableList<User> = mutableListOf()
            val board = FirebaseHelper.getInstance().getBoardDetails(boardId)
            board.assignedTo?.forEach { assigneeId ->
                val user = FirebaseHelper.getInstance().getUserDetails(assigneeId)
                members.add(user)
            }
            return@withContext members
        }
    }

    private fun showAddMemberDialog() {
        val dialog = Dialog(this)
        val dialogViewBinding = AddMemberViewBinding.inflate(layoutInflater, binding.root, false)
        dialog.setContentView(dialogViewBinding.root)
        val layoutParams = WindowManager.LayoutParams()
        val attribute = dialog.window?.attributes
        layoutParams.copyFrom(attribute)
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = layoutParams
        dialog.setCanceledOnTouchOutside(false)
        dialogViewBinding.addMemberBtn.setOnClickListener {
            dialog.cancel()
            if (dialogViewBinding.memberEmailEdt.text.isNullOrBlank()) {
                dialogViewBinding.textInputLayout.error =
                    resources.getString(R.string.empty_email_error)
            } else {
                dialogViewBinding.textInputLayout.error = null
                lifecycleScope.launch {
                    showLoader()
                    try {
                        maybeAddMember(dialogViewBinding.memberEmailEdt.text.toString())
                        showSuccessSnackBar(resources.getString(R.string.member_added_success))
                        hideLoader()
                        fetchMembersAndLoadInRecyclerView(boardId = board!!.id!!)
                    } catch (error: Exception) {
                        Log.d(activityName(),error.toString())

                        hideLoader()
                        showErrorSnackBar(resources.getString(R.string.failed_to_add_member))
                    }
                }
            }
        }
        dialogViewBinding.cancelButton.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    private suspend fun refreshBoard(){
        withContext(Dispatchers.IO){
            board = FirebaseHelper.getInstance().getBoardDetails(boardId = board!!.id!!)
        }
    }
    private suspend fun maybeAddMember(email: String) {
        withContext(Dispatchers.IO) {
            val user = FirebaseHelper.getInstance().getUserDetailsByEmail(email)
            board?.assignedTo?.contains(user.id)?.let {
                if (!it) {
                    FirebaseHelper.getInstance().updateBoard(
                        board!!.id!!,
                        mapOf(FirebaseKeyConstants.assignedTo to board!!.assignedTo!!.plus(user.id))
                    )
                    sendMemberAddedPushNotification(user)
                    refreshBoard()
                }
            }
        }
    }

    private fun sendMemberAddedPushNotification(user:User){
        FirebaseHelper.getInstance().sendPushNotification(user.fcmToken!!,"Added to board","You are added to ${board!!.name} board by ${getCurrentUserDetails()!!.name}",object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d(activityName(),e.toString())
            }
            override fun onResponse(call: Call, response: Response) {
                Log.d(activityName(),response.toString())
            }
        })
    }

    override fun activityName(): String {
        return BoardMembersActivity::class.java.simpleName
    }
}