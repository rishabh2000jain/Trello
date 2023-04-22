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
import com.example.trello.databinding.SignupLoginLayoutBinding
import com.example.trello.features.authentication.callbacks.AuthCallbacks
import com.example.trello.firebase.FirebaseHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : BottomSheetDialogFragment() {
    private var authCallbacks: AuthCallbacks? = null
    private lateinit var loginLayoutBinding: SignupLoginLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loginLayoutBinding = SignupLoginLayoutBinding.inflate(inflater)
        return loginLayoutBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_BottomSheet)
        isCancelable = false

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewForLoginScreen()

        handleClickEvents()
    }

    private fun handleClickEvents() {
        loginLayoutBinding.signupLoginBtn.setOnClickListener {
            lifecycleScope.launch {
                loginUser()
            }
        }
        loginLayoutBinding.closeBtn.setOnClickListener {
            dismiss()
        }
    }


    private fun setupViewForLoginScreen() {
        loginLayoutBinding.pageTitleTxt.text = resources.getString(R.string.login)
        loginLayoutBinding.nameEdt.visibility = View.GONE
        loginLayoutBinding.signupLoginBtn.setText(resources.getString(R.string.login))
    }


    fun registerAuthCallbacks(callbacks: AuthCallbacks) {
        authCallbacks = callbacks
    }

    private fun validateUserDetails(): Boolean {
        when {
            !AppUtil.validateEmail(loginLayoutBinding.emailEdt.text.toString()) -> {
                loginLayoutBinding.emailEdt.error =
                    resources.getString(R.string.invalid_email_error)
            }
            loginLayoutBinding.passwordEdt.text.toString().length < 8 -> {
                loginLayoutBinding.passwordEdt.error =
                    resources.getString(R.string.empty_password_error).replace("@1", "8")
            }
            else -> {
                return true
            }
        }
        return false
    }

    private suspend fun loginUser() {
        if (!validateUserDetails()) return;
        loginLayoutBinding.signupLoginBtn.setLoading(true)
        try {
            val loggedInUser = FirebaseAuth.getInstance().signInWithEmailAndPassword(
                loginLayoutBinding.emailEdt.text.toString(),
                loginLayoutBinding.passwordEdt.text.toString()
            ).await().user
            if (loggedInUser != null) {
                val user = FirebaseHelper.getInstance().getUserDetails(loggedInUser.uid)
                authCallbacks?.onAuthenticationSuccess(user)

                AppUtil.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.user_login_success),
                    SnackBarType.SUCCESS
                )
                dismiss()
            } else {
                AppUtil.showSnackBar(
                    requireActivity(),
                    resources.getString(R.string.some_thing_went_wrong),
                    SnackBarType.ERROR
                )
            }
        } catch (exception: java.lang.Exception) {
            AppUtil.showSnackBar(requireActivity(), exception.message.orEmpty(), SnackBarType.ERROR)

        } finally {
            loginLayoutBinding.signupLoginBtn.setLoading(false)
        }
    }

}