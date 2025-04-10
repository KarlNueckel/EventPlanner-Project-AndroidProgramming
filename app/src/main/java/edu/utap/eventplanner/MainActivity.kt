package edu.utap.eventplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.utap.eventplanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Default fragment
        replaceFragment(PostsFragment())

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_posts -> replaceFragment(PostsFragment())
                R.id.nav_rsvps -> replaceFragment(YourRSVPsFragment())
                R.id.nav_your_events -> replaceFragment(YourEventsFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}























//package edu.utap.eventplanner
//
//import android.os.Bundle
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//import edu.utap.eventplanner.databinding.ActivityMainBinding
//
//class MainActivity : AppCompatActivity() {
//    companion object {
//        const val TAG = "MainActivity"
//    }
//    private lateinit var binding : ActivityMainBinding
//    private lateinit var authUser : AuthUser
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        if(savedInstanceState == null) {
//            binding.displayNameET.text?.clear()
//        }
//        binding.logoutBut.setOnClickListener {
//            // XXX Write me.
//            authUser.logout()
//        }
//        // If the user spam clicks the login button (clicking it many times in
//        // a row), we only want to log in once.
////        binding.loginBut.setOnClickListener {
////            val email = binding.userEmail.text.toString().trim()
////            val password = binding.displayNameET.text.toString().trim()  // Assume password field exists
////
////            if (email.isNotEmpty() && password.isNotEmpty()) {
////                authUser.login()
////            } else {
////                Log.e(TAG, "Email and password must not be empty")
////            }
////        }
////
//        binding.loginBut.setOnClickListener {
//            authUser.login()
//        }
//
//
//        binding.setDisplayName.setOnClickListener {
//            // XXX Write me.
//            val newDisplayName = binding.displayNameET.text.toString().trim()
//            if (newDisplayName.isNotEmpty()) {
//                authUser.setDisplayName(newDisplayName)
//            }
//        }
//    }
//
//    // We can only safely initialize AuthUser once onCreate has completed.
////    override fun onStart() {
////        super.onStart()
////        // Initialize AuthUser, observe data to display in UI
////        // https://developer.android.com/reference/androidx/lifecycle/Lifecycle#addObserver(androidx.lifecycle.LifecycleObserver)
////        // XXX Write me.
////        authUser = AuthUser(activityResultRegistry)
////        lifecycle.addObserver(authUser)
////
////        authUser.observeUser().observe(this) { user ->
////            binding.displayName.text = user.name
////            binding.userEmail.text = user.email
////            binding.userUid.text = user.uid
////        }
////
////
////    }
//    override fun onStart() {
//        super.onStart()
//        authUser = AuthUser(activityResultRegistry)
//        lifecycle.addObserver(authUser)
//
//        authUser.observeUser().observe(this) { user ->
//            binding.displayName.text = user.name
//            binding.userEmail.text = user.email
//            binding.userUid.text = user.uid
//
//            // Always launch login if user is logged out
//            Log.d(TAG, "Checking authentication state...")
//            if (user.isInvalid()) {
//                Log.d(TAG, "No user logged in, launching login screen...")
//                authUser.login()
//            } else {
//                Log.d(TAG, "User already logged in: ${user.email}")
//            }
//        }
//    }
//
//
//}