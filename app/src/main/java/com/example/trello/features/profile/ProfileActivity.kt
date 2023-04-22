package com.example.trello.features.profile

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.trello.R
import com.example.trello.common.*
import com.example.trello.common.models.User
import com.example.trello.databinding.ActivityProfileBinding
import com.example.trello.firebase.FirebaseHelper
import com.example.trello.sharedpreference.SharedPrefKeys
import com.example.trello.sharedpreference.SharedPreferenceHelper
import com.example.trello.util.AppUtil
import com.example.trello.util.PermissionManager
import com.example.trello.util.toJsonString
import com.example.trello.util.validPhoneNumber
import kotlinx.coroutines.*

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var pickedImage: Uri? = null
    private var userDetails: User? = null
    private val imageChooser: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {
            pickedImage = it
            if (pickedImage != null) {
                showResetBtn()
            }
            setProfileImage(it?.toString())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.commonToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = resources.getString(R.string.my_profile)
        }
        populateDataInViews()
        handleClickEvents()
        addProfileUpdateListener()
    }

    private fun populateDataInViews() {
        userDetails = getCurrentUserDetails()
        userDetails?.let {
            setProfileImage(it.profile)
            binding.nameEdt.setText(it.name)
            binding.emailEdt.setText(it.email)
            binding.phoneEdt.setText(it.phone)
        }
    }

    private fun handleClickEvents() {
        binding.toolbar.commonToolbar.setNavigationOnClickListener {
            pop()
        }
        binding.profileEdtBtn.setOnClickListener {
            val isPermissionGranted = PermissionManager.checkStoragePermission(this)
            if (!isPermissionGranted) {
                requestStorageReadPermission()
            } else {
                launchMediaPicker()
            }
        }
        binding.updateBtn.setOnClickListener {
            lifecycleScope.launch {
                saveProfile()
                hideResetBtn()
            }
        }
        binding.resetBtn.setOnClickListener {
            populateDataInViews()
            hideResetBtn()
        }
    }

    private fun launchMediaPicker() {
        imageChooser.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))

    }

    private fun getUpdatedDataMap(): Map<String, String> {
        var updatedData = mapOf<String, String>()
        if (userDetails?.name != binding.nameEdt.text.toString()) {
            updatedData =
                updatedData.plus(Pair<String, String>("name", binding.nameEdt.text.toString()))
        }
        if (userDetails?.email != binding.emailEdt.text.toString()) {
            updatedData =
                updatedData.plus(Pair<String, String>("email", binding.emailEdt.text.toString()))
        }
        if (userDetails?.phone != binding.phoneEdt.text.toString()) {
            updatedData =
                updatedData.plus(Pair<String, String>("phone", binding.phoneEdt.text.toString()))
        }
        return updatedData
    }

    private suspend fun saveProfile() {
        if (userDetails == null) return

        when {
            !AppUtil.validateEmail(binding.emailEdt.text.toString()) -> {
                binding.emailEdt.error = resources.getString(R.string.invalid_email_error)
            }
            !binding.phoneEdt.text.toString().validPhoneNumber() -> {
                binding.phoneEdt.error = resources.getString(R.string.invalid_phone_error)
            }
            binding.nameEdt.text.isNullOrBlank() -> {
                binding.nameEdt.error = resources.getString(R.string.empty_name_error)
            }
            else -> {
                try {
                    binding.updateBtn.setLoading(true)
                    showLoader()
                    val deferred = lifecycleScope.async(Dispatchers.IO) {
                        var imagePath: String? = null
                        if (pickedImage != null) {
                            imagePath = uploadImage(pickedImage!!)
                        }
                        var updatedData = mapOf<String, String>()
                        if (imagePath != null) {
                            updatedData =
                                updatedData.plus(Pair<String, String>("profile", imagePath))
                        }
                        withContext(Dispatchers.Main) {
                            updatedData = updatedData.plus(getUpdatedDataMap())
                        }
                        if (updatedData.isEmpty()) return@async
                        val updatedUser =
                            FirebaseHelper.getInstance().updateUser(userDetails!!.id, updatedData)
                        withContext(Dispatchers.Main) {
                            SharedPreferenceHelper.getInstance().saveString(
                                SharedPrefKeys.userDetails,
                                updatedUser.toJsonString<User>().orEmpty()
                            )
                            userDetails = updatedUser
                            populateDataInViews()
                            showSuccessSnackBar(
                                resources.getText(R.string.profile_updated).toString()
                            )
                        }
                    }
                    withTimeout(timeoutMillis) {
                        deferred.await()
                    }

                } catch (exception: java.lang.Exception) {
                    showErrorSnackBar(resources.getText(R.string.profile_update_failed).toString())
                } finally {
                    hideLoader()
                    binding.updateBtn.setLoading(false)
                }
            }
        }
    }

    private fun setProfileImage(uri: String?) {
        if (uri == null) return
        Glide.with(this).load(uri).centerCrop().placeholder(R.drawable.splash_img).into(binding.profileImg)
    }

    private suspend fun uploadImage(uri: Uri): String? {
        val extension =
            MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
                ?: return null
        return FirebaseHelper.getInstance().uploadProfileImage(uri, extension)
    }

    private fun addProfileUpdateListener() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                showResetBtn()
            }
        }
        binding.nameEdt.addTextChangedListener(textWatcher)
        binding.emailEdt.addTextChangedListener(textWatcher)
        binding.phoneEdt.addTextChangedListener(textWatcher)
    }

    private fun showResetBtn() {
        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.hor_translate_animation)
        binding.resetBtn.visibility = View.VISIBLE
        binding.resetBtn.startAnimation(slideAnimation)
    }

    private fun hideResetBtn() {
        binding.resetBtn.visibility = View.GONE
    }

    override fun activityName(): String = this::class.java.simpleName

}