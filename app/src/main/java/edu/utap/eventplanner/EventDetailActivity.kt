package edu.utap.eventplanner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.eventplanner.databinding.ActivityEventDetailBinding
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import android.view.View

class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Wait a bit and check again
            Handler(Looper.getMainLooper()).postDelayed({
                if (auth.currentUser == null) {
                    val intent = Intent(this, StartActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    continueSetup()
                }
            }, 500) // Wait 0.5 seconds
        } else {
            continueSetup()
        }
    }

    private fun continueSetup() {
        val eventId = intent.getStringExtra("eventId")
        if (eventId == null) {
            finish()
            return
        }

        // 👇 Hide RSVP if this is the owner's view
        val isOwner = intent.getBooleanExtra("isOwner", false)
        if (isOwner) {
            binding.rsvpButton.visibility = View.GONE
        }

        loadEvent(eventId)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.rsvpButton.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("events").document(eventId)
                .update("attendees.$uid", true)
                .addOnSuccessListener {
                    Toast.makeText(this, "RSVP'd!", Toast.LENGTH_SHORT).show()
                    loadEvent(eventId)
                }
        }
    }
    private fun loadEvent(eventId: String) {
        db.collection("events").document(eventId).get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.data
                if (data == null) {
                    Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 1. Extract all fields
                val title       = data["title"]       as? String ?: ""
                val description = data["description"] as? String ?: ""
                val startTime   = data["startTime"]   as? String ?: ""
                val endTime     = data["endTime"]     as? String ?: ""
                val location    = data["location"]    as? String ?: ""
                val creatorUid  = data["creatorUid"]  as? String ?: ""

                // 2. Coerce attendees to Map<String,Boolean>
                val attendeesMap: Map<String, Boolean> = when (val raw = data["attendees"]) {
                    is Map<*, *> -> raw.entries
                        .filter { it.key is String }
                        .associate { it.key as String to (it.value as? Boolean ?: true) }
                    is List<*>    -> raw.filterIsInstance<String>().associateWith { true }
                    else          -> emptyMap()
                }

                // 3. Populate UI fields
                binding.eventTitle.text       = title
                binding.eventDescription.text = description
                binding.eventTime.text        = "$startTime – $endTime"
                binding.eventLocation.text    = location

                // 4. Load host info
                db.collection("users").document(creatorUid).get()
                    .addOnSuccessListener { userDoc ->
                        val name     = userDoc.getString("name")     ?: "Unknown"
                        val username = userDoc.getString("username") ?: "unknown"
                        binding.eventHost.text = "$name (@$username)"
                    }
                    .addOnFailureListener {
                        binding.eventHost.text = "Host: Unknown"
                    }

                // 5. Load attendee names
                if (attendeesMap.isNotEmpty()) {
                    val usernames = mutableListOf<String>()
                    val tasks = attendeesMap.keys.map { uid ->
                        db.collection("users").document(uid).get()
                    }
                    Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                        .addOnSuccessListener { docs ->
                            docs.forEach { doc ->
                                val name     = doc.getString("name")     ?: "Unknown"
                                val username = doc.getString("username") ?: "unknown"
                                usernames.add("$name (@$username)")
                            }
                            binding.eventAttendees.text = "Going:\n" + usernames.joinToString("\n")
                        }
                        .addOnFailureListener {
                            binding.eventAttendees.text = "Going:\nError loading attendees"
                        }
                } else {
                    binding.eventAttendees.text = "Going:\n"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show()
            }
    }
}
