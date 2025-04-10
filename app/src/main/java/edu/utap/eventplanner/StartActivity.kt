package edu.utap.eventplanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import edu.utap.eventplanner.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }



        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

    }
}
