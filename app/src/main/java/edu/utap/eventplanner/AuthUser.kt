package edu.utap.eventplanner

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

// This is our abstract concept of a User, which is visible
// outside AuthUser.  That way, client code will not change
// if we use something other than Firebase for authentication
data class User (private val nullableName: String?,
                 private val nullableEmail: String?,
                 val uid: String) {
    val name: String = nullableName ?: "User logged out"
    val email: String = nullableEmail ?: "User logged out"
}
const val invalidUserUid = "-1"
// Extension function to determine if user is valid
fun User.isInvalid(): Boolean {
    return uid == invalidUserUid
}
val invalidUser = User(null, null,
    invalidUserUid)

// This class must be an observer to the lifecycle of an activity or
// fragment.  It is only by being an observer that it can start the activity
// that logs a user in.
class AuthUser(private val registry: ActivityResultRegistry) :
    FirebaseAuth.AuthStateListener,
    DefaultLifecycleObserver {
    companion object {
        private const val TAG = "AuthUser"
    }
    // This variable makes it so if you try to log in multiple times (e.g., by clicking
    // the button many times, you only get one login screen
    private var pendingLogin = false
    // https://developer.android.com/training/basics/intents/result#separate
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private var liveUser = MutableLiveData<User>().apply {
        this.postValue(invalidUser)
    }
    init {
        // Listen to FirebaseAuth state
        // That way, if the server logs us out, we know it and change the view
        Firebase.auth.addAuthStateListener(this)
    }

    fun observeUser(): LiveData<User> {
        // XXX Write me, not too difficult
        return liveUser
    }

    // Active live data upon a change of state for our FirebaseUser
    private fun postUserUpdate(firebaseUser: FirebaseUser?) {
        // XXX Write me
        if (firebaseUser == null) {
            liveUser.postValue(invalidUser)
        } else {
            liveUser.postValue(User(firebaseUser.displayName, firebaseUser.email, firebaseUser.uid))
        }
    }
    // This override makes us a valid FirebaseAuth.AuthStateListener
    override fun onAuthStateChanged(p0: FirebaseAuth) {
        postUserUpdate(p0.currentUser)
    }

    // https://developer.android.com/training/basics/intents/result#separate
    override fun onCreate(owner: LifecycleOwner) {
//        signInLauncher = registry.register("key", owner,
//            FirebaseAuthUIActivityResultContract()) { result ->
//            Log.d(TAG, "sign in result ${result.resultCode}")
//            // XXX Write me, pendingLogin
//            pendingLogin = false
//        }
        signInLauncher = registry.register("key", owner,
            FirebaseAuthUIActivityResultContract()) { result ->
            Log.d(TAG, "sign in result ${result.resultCode}")
            pendingLogin = false
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                postUserUpdate(Firebase.auth.currentUser)
            } else {
                Log.e(TAG, "Login failed or canceled")
                postUserUpdate(null)
            }
        }

    }
    private fun user(): FirebaseUser? {
        return Firebase.auth.currentUser
    }
    fun setDisplayName(displayName: String) {
        Log.d(TAG, "XXX profile change request")
        // If no user, no way to set display name, just return
        val user = user() ?: return
        // https://firebase.google.com/docs/auth/android/manage-users#update_a_users_profile
        // XXX Write me.
        val profileUpdates = userProfileChangeRequest {
            setDisplayName(displayName)
        }

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                Log.d(TAG, "Display name updated successfully")
//                postUserUpdate(user)
//            }
            //
            if (task.isSuccessful) {
                Log.d(TAG, "Display name updated successfully")
                user.reload().addOnCompleteListener {
                    postUserUpdate(Firebase.auth.currentUser)
                }
            }
            else {
                Log.e(TAG, "Failed to update display name")
            }
        }
    }

    fun login() {
        if (user() == null && !pendingLogin) {
            Log.d(TAG, "Launching Firebase Login UI")

            pendingLogin = true
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build()
            )

            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                //.setTheme(R.style.Theme_FirebaseAuth_NoActionBar)
                .build()

            signInLauncher.launch(signInIntent)
        } else {
            Log.d(TAG, "User already logged in: ${user()?.email}")
        }
    }




    //    fun logout() {
//        if(user() == null) return
//        Firebase.auth.signOut()
//    }
    fun logout() {
        if (user() != null) {
            Firebase.auth.signOut()
            postUserUpdate(null)
        }
    }

}
