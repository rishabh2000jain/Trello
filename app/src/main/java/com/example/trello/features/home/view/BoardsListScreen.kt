package com.example.trello.features.home.view

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.trello.R
import com.example.trello.common.BaseActivity
import com.example.trello.common.FirebaseKeyConstants
import com.example.trello.common.OnRecyclerViewItemClick
import com.example.trello.common.models.Board
import com.example.trello.common.models.User
import com.example.trello.databinding.ActivityBoardListBinding
import com.example.trello.databinding.DrawerHeaderLayoutBinding
import com.example.trello.features.taskList.TaskListActivity
import com.example.trello.features.createBoard.CreateBoardActivity
import com.example.trello.features.home.view.adapters.BoardGridAdapter
import com.example.trello.features.profile.ProfileActivity
import com.example.trello.firebase.FirebaseHelper
import com.example.trello.services.MyFirebaseMessagingService
import com.example.trello.services.NotificationCallbacks
import com.example.trello.sharedpreference.SharedPrefKeys
import com.example.trello.sharedpreference.SharedPreferenceHelper
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoardsListScreen : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnRecyclerViewItemClick<Board>, FetchUserDetailsCallback,NotificationCallbacks {
    lateinit var binding: ActivityBoardListBinding
    lateinit var boardListAdapter: BoardGridAdapter

    companion object {
        val BOARD: String = "Board"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainContent.toolbar)
        MyFirebaseMessagingService.registerListener(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(
            ContextCompat.getDrawable(
                this,
                R.drawable.baseline_menu_24
            )
        )
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainContent.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)

        actionBarDrawerToggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
        binding.mainContent.createBoardBtn.setOnClickListener {
            navigateToWithResult(CreateBoardActivity::class.java)
        }
        bindRecyclerView()
        lifecycleScope.launch {
            getBoardList()
        }
        lifecycleScope.launch {
            try {
                updateUserFcmToken(getCurrentUserDetails()!!)
            } catch (error: Exception) {
            }
        }
    }

    private suspend fun getBoardList(silent:Boolean=false) {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            return@async FirebaseHelper.getInstance().getBoardList(getUser()!!.uid)
        }
        if(!silent) {
            showLoader()
        }
            try {
                val boardList = deferred.await()
                if (this::boardListAdapter.isInitialized && (!silent || boardList.isNotEmpty())) {
                    boardListAdapter.updateBoardList(boardList)
                }
            } catch (exception: Exception) {
                showErrorSnackBar(resources.getString(R.string.failed_to_fetch_data))
            } finally {
                hideLoader()
                if (boardListAdapter.itemCount == 0) {
                    binding.mainContent.noResultTxt.visibility = View.VISIBLE
                } else {
                    binding.mainContent.noResultTxt.visibility = View.GONE
                }
            }
    }

    private fun bindRecyclerView() {
        boardListAdapter = BoardGridAdapter()
        binding.mainContent.boardsList.adapter = boardListAdapter
        binding.mainContent.boardsList.layoutManager = GridLayoutManager(this, 2)
        boardListAdapter.itemClickListener = this
        boardListAdapter.registerFetchUserCallback(this)
    }

    override fun onResume() {
        super.onResume()
        populateNavDrawer()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                signOut()
                binding.drawerLayout.close()
                true
            }
            R.id.my_profile -> {
                binding.drawerLayout.close()
                navigateTo(ProfileActivity::class.java)
                true
            }
            else -> false
        }
    }

    private fun populateNavDrawer() {
        val headerLayoutBinding =
            DrawerHeaderLayoutBinding.bind(binding.navigationView.getHeaderView(0))
        val user = getCurrentUserDetails()
        user?.let {
            if(!it.profile.isNullOrEmpty()){
                Glide.with(this).load(it.profile).centerCrop()
                    .placeholder(R.drawable.splash_img)
                    .into(headerLayoutBinding.profileImg)
            }

            headerLayoutBinding.userNameTxt.text = it.name
        }

    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isOpen) {
            binding.drawerLayout.close()
        } else {
            handleBackPressed()
        }
    }

    override fun activityName(): String = this::class.java.simpleName
    override fun onActivityResult(result: ActivityResult) {
        super.onActivityResult(result)
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                getBoardList()
            }
        }
    }

    override fun onClick(item: Board) {
        val bundle = Bundle()
        bundle.putParcelable(BOARD, item)
        navigateTo(TaskListActivity::class.java, bundle)
    }

    private suspend fun getUserNameFromId(uid: String) {
        try {
            val user = FirebaseHelper.getInstance().getUserDetails(uid)
            boardListAdapter.setUserDetails(user)
        } catch (exception: Exception) {

        }
    }

    override fun fetchDetails(uid: String) {
        lifecycleScope.launch {
            getUserNameFromId(uid)
        }
    }

    private suspend fun updateUserFcmToken(user: User) {
        val fcmToken = SharedPreferenceHelper.getInstance().getString(SharedPrefKeys.fcmToken)
        if (fcmToken.isNullOrEmpty()) return
        val user = withContext(Dispatchers.IO) {
            return@withContext FirebaseHelper.getInstance()
                .updateUser(user.id, mapOf(FirebaseKeyConstants.fcmToken to fcmToken))
        }
        updateUserDetails(user)
    }

    override fun onNotificationReceived() {
        lifecycleScope.launch {
            getBoardList(silent = true)
        }
    }

}