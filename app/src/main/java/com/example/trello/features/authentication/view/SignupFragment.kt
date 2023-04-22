package com.example.trello.features.authentication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.trello.R
import com.example.trello.util.AppUtil
import com.example.trello.util.SnackBarType
import com.example.trello.common.models.User
import com.example.trello.databinding.SignupLoginLayoutBinding
import com.example.trello.features.authentication.callbacks.AuthCallbacks
import com.example.trello.firebase.FirebaseHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignupFragment : BottomSheetDialogFragment() {
    private var authCallbacks: AuthCallbacks? = null
    private lateinit var signupLayoutBinding: SignupLoginLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        signupLayoutBinding = SignupLoginLayoutBinding.inflate(inflater)
        return signupLayoutBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_BottomSheet)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewForSignupScreen()
        handleClickEvents()
    }

    private fun handleClickEvents() {
        signupLayoutBinding.closeBtn.setOnClickListener {
            dismiss()
        }
        signupLayoutBinding.signupLoginBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                createUser()
            }
        }
    }

    private fun setupViewForSignupScreen() {
        signupLayoutBinding.pageTitleTxt.text = resources.getString(R.string.signup)
        signupLayoutBinding.emailEdt.visibility = View.VISIBLE
        signupLayoutBinding.signupLoginBtn.setText(resources.getString(R.string.signup))
    }


    fun registerAuthCallbacks(callbacks: AuthCallbacks) {
        authCallbacks = callbacks
    }


    private suspend fun createUser() {
        if (!validateUserDetails()) return
        try {
            signupLayoutBinding.signupLoginBtn.setLoading(true)
            val authResult = FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                signupLayoutBinding.emailEdt.text.toString(),
                signupLayoutBinding.passwordEdt.text.toString()
            ).await()
            authResult.user?.let { addUserInUserTable(it) }

        } catch (exception: Exception) {
            AppUtil.log(SignupFragment::class.java, exception.message.toString())
            AppUtil.showSnackBar(
                requireActivity(),
                exception.message.toString(),
                SnackBarType.ERROR
            )
        } finally {
            signupLayoutBinding.signupLoginBtn.setLoading(false)
        }
    }

    private suspend fun addUserInUserTable(firebaseUser: FirebaseUser) {
        signupLayoutBinding.signupLoginBtn.setLoading(true)
        val user = User(
            firebaseUser.uid,
            signupLayoutBinding.nameEdt.text.toString(),
            firebaseUser.email,
            firebaseUser.phoneNumber,
            firebaseUser.photoUrl?.toString(),
        )
        val response = FirebaseHelper.getInstance().createUser(user)
        if (response.success) {
            authCallbacks?.onAuthenticationSuccess(user)
            dismiss()
            AppUtil.showSnackBar(
                requireActivity(),
                resources.getString(R.string.user_create_success),
                SnackBarType.SUCCESS
            )
        } else {
            AppUtil.showSnackBar(
                requireActivity(),
                resources.getString(R.string.failed_to_create_user),
                SnackBarType.ERROR
            )
        }
        signupLayoutBinding.signupLoginBtn.setLoading(false)

    }


    private fun validateUserDetails(): Boolean {
        when {
            signupLayoutBinding.nameEdt.text.isNullOrBlank() -> {
                signupLayoutBinding.nameEdt.error = resources.getString(R.string.empty_name_error)
            }
            !AppUtil.validateEmail(signupLayoutBinding.emailEdt.text.toString()) -> {
                signupLayoutBinding.emailEdt.error =
                    resources.getString(R.string.invalid_email_error)
            }
            signupLayoutBinding.passwordEdt.text.toString().length < 8 -> {
                signupLayoutBinding.passwordEdt.error =
                    resources.getString(R.string.empty_password_error).replace("@1", "8")
            }
            else -> {
                return true
            }
        }
        return false
    }
}