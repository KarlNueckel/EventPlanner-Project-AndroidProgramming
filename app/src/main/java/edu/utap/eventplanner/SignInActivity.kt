package edu.utap.eventplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.eventplanner.databinding.ActivitySigninBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, StartActivity::class.java))
        finish()
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameET.text.toString().trim()
            val password = binding.passwordET.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Look up user by username
            db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val user = querySnapshot.documents[0]
                        val email = user.getString("email") ?: ""

                        // 2. Sign in with email/password
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking username", Toast.LENGTH_SHORT).show()
                }
        }
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

    }
}
