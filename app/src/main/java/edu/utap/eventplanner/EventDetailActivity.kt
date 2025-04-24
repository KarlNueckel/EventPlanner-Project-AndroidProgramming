package edu.utap.eventplanner

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import edu.utap.eventplanner.databinding.ActivityEventDetailBinding

class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wait briefly if auth isn’t ready
        if (auth.currentUser == null) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (auth.currentUser == null) {
                    startActivity(Intent(this, StartActivity::class.java))
                    finish()
                } else {
                    continueSetup()
                }
            }, 500)
        } else {
            continueSetup()
        }
    }

    private fun continueSetup() {
        val eventId = intent.getStringExtra("eventId") ?: run { finish(); return }
        val isOwner = intent.getBooleanExtra("isOwner", false)

        //BACK BUTTON (UI) ︎
        binding.backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Show/hide the RSVP vs Delete buttons
        binding.rsvpButton.visibility   = if (isOwner) View.GONE else View.VISIBLE
        binding.deleteButton.visibility = if (isOwner) View.VISIBLE else View.GONE

        // DELETE
        binding.deleteButton.setOnClickListener {
            db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        }

        // RSVP / Un‑RSVP
        binding.rsvpButton.setOnClickListener {
            toggleRsvp(eventId)
        }

        // Load all the UI
        loadEvent(eventId)
    }

    private fun loadEvent(eventId: String) {
        db.collection("events").document(eventId).get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.data ?: run {
                    Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                // Basic fields
                binding.eventTitle.text       = data["title"]       as? String ?: ""
                binding.eventDescription.text = data["description"] as? String ?: ""
                val start = data["startTime"] as? String ?: ""
                val end   = data["endTime"]   as? String ?: ""
                binding.eventTime.text        = "$start – $end"
                binding.eventLocation.text    = data["location"] as? String ?: ""

                // Parse attendees into a Map<String,Boolean>
                val attendeesMap: Map<String, Boolean> = when (val raw = data["attendees"]) {
                    is Map<*, *> -> raw.entries
                        .filter { it.key is String }
                        .associate { it.key as String to (it.value as? Boolean ?: true) }
                    is List<*>    -> raw.filterIsInstance<String>().associateWith { true }
                    else          -> emptyMap()
                }

                // Adjust RSVP button text
                val uid = auth.currentUser?.uid
                val going = uid != null && attendeesMap.containsKey(uid)
                binding.rsvpButton.text = if (going) "Un‑RSVP" else "RSVP"

                // Load host info
                val creatorUid = data["creatorUid"] as? String ?: ""
                db.collection("users").document(creatorUid).get()
                    .addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("name") ?: "Unknown"
                        val user = userDoc.getString("username") ?: "unknown"
                        binding.eventHost.text = "$name (@$user)"
                    }
                    .addOnFailureListener {
                        binding.eventHost.text = "Host: Unknown"
                    }

                // Show attendee names
                if (attendeesMap.isNotEmpty()) {
                    val names = mutableListOf<String>()
                    val tasks = attendeesMap.keys.map { uid ->
                        db.collection("users").document(uid).get()
                    }
                    Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                        .addOnSuccessListener { docs ->
                            docs.forEach { doc ->
                                val n = doc.getString("name") ?: "Unknown"
                                val u = doc.getString("username") ?: "unknown"
                                names.add("$n (@$u)")
                            }
                            binding.eventAttendees.text = "Going:\n" + names.joinToString("\n")
                        }
                        .addOnFailureListener {
                            binding.eventAttendees.text = "Going:\nError loading attendees"
                        }
                } else {
                    binding.eventAttendees.text = "Going:\n(No one yet)"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleRsvp(eventId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("events").document(eventId).get()
            .addOnSuccessListener { snap ->
                val raw = snap.get("attendees")
                val going = when (raw) {
                    is Map<*, *> -> raw.containsKey(uid)
                    is List<*>    -> raw.contains(uid)
                    else          -> false
                }
                val op = if (going) {
                    mapOf("attendees.$uid" to FieldValue.delete())
                } else {
                    mapOf("attendees.$uid" to true)
                }
                db.collection("events").document(eventId)
                    .update(op)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            if (going) "Un‑RSVP’d" else "RSVP’d!",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update RSVP", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}