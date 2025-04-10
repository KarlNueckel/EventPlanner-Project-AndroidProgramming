package edu.utap.eventplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.eventplanner.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupButton.setOnClickListener {
            val name = binding.nameET.text.toString().trim()
            val username = binding.usernameET.text.toString().trim()
            val email = binding.emailET.text.toString().trim()
            val password = binding.passwordET.text.toString().trim()

            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener
                    val userData = hashMapOf(
                        "name" to name,
                        "username" to username,
                        "email" to email
                    )

                    db.collection("users").document(userId).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Sign-up failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
