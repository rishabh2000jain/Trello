package com.example.trello.common

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.trello.R
import com.example.trello.common.models.User
import com.example.trello.features.authentication.AuthActivity
import com.example.trello.firebase.FirebaseHelper
import com.example.trello.sharedpreference.SharedPrefKeys
import com.example.trello.sharedpreference.SharedPreferenceHelper
import com.example.trello.util.AppUtil
import com.example.trello.util.SnackBarType
import com.example.trello.util.fromJsonString
import com.example.trello.util.toJsonString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.coroutines.launch

abstract class BaseActivity() : AppCompatActivity(), OnActivityResult ,AuthStateListener,CurrentActivityName{
    private var isBackButtonPressed = false;
    private var loadingDialog: Dialog? = null
    private val startActivityWithResultContracts: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onActivityResult(it)
        }
    protected val timeoutMillis:Long = 10000L
    fun handleBackPressed() {
        if (isBackButtonPressed) {
            onBackPressedDispatcher.onBackPressed()
            isBackButtonPressed = false
            return
        }
        isBackButtonPressed = true
        Toast.makeText(this, resources.getString(R.string.press_again_exit), Toast.LENGTH_SHORT)
            .show()
        Handler(Looper.getMainLooper()).postDelayed({
            isBackButtonPressed = false
        }, 2000L)
    }

    fun showLoader(cancelable: Boolean = false) {
        loadingDialog = Dialog(this).apply {
            setContentView(R.layout.loading_layout)
            setCancelable(cancelable)
            show()
        }
    }

    fun hideLoader() {
        loadingDialog?.cancel()
        loadingDialog = null
    }

    fun showErrorSnackBar(message: String) {
        AppUtil.showSnackBar(this, message, SnackBarType.ERROR)
    }

    fun showSuccessSnackBar(message: String) {
        AppUtil.showSnackBar(this, message, SnackBarType.SUCCESS)
    }

    fun showNormalSnackBar(message: String) {
        AppUtil.showSnackBar(this, message, SnackBarType.NORMAL)
    }

    fun isLoggedIn() = FirebaseAuth.getInstance().currentUser != null

    fun getUser() = FirebaseAuth.getInstance().currentUser


    fun signOut() {
        lifecycleScope.launch {
            FirebaseHelper.getInstance().updateUser(getUser()!!.uid,mapOf(FirebaseKeyConstants.fcmToken to ""))
            FirebaseAuth.getInstance().signOut()
        }
    }


    private fun moveBackToLogin() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(authState: FirebaseAuth) {
        if(authState.currentUser==null && activityName()!=AuthActivity::class.java.simpleName){
            moveBackToLogin()
        }
    }

    fun getCurrentUserDetails(): User? {
        return SharedPreferenceHelper.getInstance().getString(SharedPrefKeys.userDetails)?.fromJsonString(User::class.java)
    }

    fun updateUserDetails(user:User){
        SharedPreferenceHelper.getInstance().saveString(SharedPrefKeys.userDetails,user.toJsonString<User>())
    }

    ///Navigation Helpers

    fun <T> navigateTo(clazz: Class<T>, param: Bundle? = null) {
        val intent = Intent(this, clazz)
        intent.putExtra("params", param)
        startActivity(intent)
    }

    fun <T> navigateToWithResult(clazz: Class<T>, param: Bundle? = null) {
        val intent = Intent(this, clazz)
        intent.putExtra("params", param)
        startActivityWithResultContracts.launch(intent)
    }

    fun <T> pushReplacement(clazz: Class<T>, param: Bundle? = null) {
        navigateTo(clazz, param)
        finish()
    }

    fun pop(param: Bundle? = null){
        if (param!=null){
            val intent = Intent()
            intent.putExtra("data",param)
            setResult(RESULT_OK,intent)
        }
        finish()
    }
    private val permissionResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                val shouldShowRational =
                    shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                if (shouldShowRational) {
                    showStoragePermissionRational()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun showStoragePermissionRational() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.permission))
        builder.setMessage(resources.getString(R.string.permission_rational_message))
        builder.setPositiveButton(resources.getString(R.string.setting)) { dialog, _ ->
            val settingIntent = Intent()
            settingIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            settingIntent.data = Uri.fromParts("package", packageName, null)
            startActivity(settingIntent)
        }
        builder.create().show()
    }

    protected fun requestStorageReadPermission() {
        permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun showConfirmationDialog(title:String,desc:String,onConfirm:()->Unit){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(title)
        dialog.setCancelable(false)
        dialog.setMessage(desc)
        dialog.setPositiveButton(R.string.confirm){ dialog,_ ->
            onConfirm()
            dialog.dismiss()
        }
        dialog.setNegativeButton(R.string.cancel){ dialog,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }

}

interface OnActivityResult {
    fun onActivityResult(result: ActivityResult) {}
}

interface CurrentActivityName{
    fun activityName():String
}