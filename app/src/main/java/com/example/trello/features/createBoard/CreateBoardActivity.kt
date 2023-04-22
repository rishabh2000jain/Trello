package com.example.trello.features.createBoard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.trello.R
import com.example.trello.common.BaseActivity
import com.example.trello.common.models.Board
import com.example.trello.databinding.ActivityCreateBoardBinding
import com.example.trello.firebase.FirebaseHelper
import com.example.trello.util.PermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

class CreateBoardActivity : BaseActivity() {
    private val imageChooser: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {
            selectedImageUri = it
            setBoardImage()
        }
    private var selectedImageUri: Uri? = null
    private lateinit var binding: ActivityCreateBoardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.commonToolbar)
        supportActionBar?.let {
            it.setTitle(R.string.board)
            it.setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.commonToolbar.setNavigationOnClickListener {
            pop()
        }
        bindListeners()
    }

    private fun launchMediaPicker() {
        val isPermissionGranted = PermissionManager.checkStoragePermission(this)
        if (!isPermissionGranted) {
            requestStorageReadPermission()
        } else {
            imageChooser.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setBoardImage() {
        binding.boardImg.setImageURI(selectedImageUri)
    }

    private fun bindListeners() {
        binding.boardImg.setOnClickListener {
            launchMediaPicker()
        }
        binding.createBtn.setOnClickListener {
            when {
                binding.boardNameEdt.text.isNullOrBlank() -> binding.textInputLayout.error =
                    resources.getString(R.string.empty_name_error)
                selectedImageUri == null -> Toast.makeText(
                    this,
                    R.string.image_empty_error,
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    lifecycleScope.launch {
                        createBoard()
                    }
                }
            }
        }
        binding.boardNameEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.textInputLayout.error = resources.getString(R.string.empty_name_error)
                } else {
                    binding.textInputLayout.error = null
                }
            }

        })
    }

    override fun activityName(): String {
        return this::class.java.simpleName
    }

    private suspend fun createBoard() {
        try {
            binding.createBtn.setLoading(true)
            showLoader()
            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentResolver.getType(selectedImageUri!!))
            val deferred = lifecycleScope.async(Dispatchers.IO) {
                val imageUrl = FirebaseHelper.getInstance()
                    .uploadBoardImage(selectedImageUri!!, extension ?: ".png")
                val user = getCurrentUserDetails() ?: return@async
                FirebaseHelper.getInstance().createBoard(
                    Board(
                        name = binding.boardNameEdt.text.toString(),
                        createdBy = user.id,
                        image = imageUrl.orEmpty(),
                        assignedTo = listOf(user.id)
                    )
                )
            }
            deferred.await()
            showSuccessSnackBar(resources.getString(R.string.board_create_success))
            setResult(Activity.RESULT_OK)
            finish()
        } catch (e: Exception) {
            showSuccessSnackBar(resources.getString(R.string.failed_to_create_board))
        } finally {
            binding.createBtn.setLoading(true)
            hideLoader()
        }
    }
}