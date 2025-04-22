package edu.utap.eventplanner

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.Tasks
import edu.utap.eventplanner.databinding.ActivityEventDetailBinding

class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  wait for auth to be ready
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

        // Show/hide buttons
        binding.rsvpButton.visibility   = if (isOwner) View.GONE else View.VISIBLE
        binding.deleteButton.visibility = if (isOwner) View.VISIBLE else View.GONE

        // Delete action
        binding.deleteButton.setOnClickListener {
            db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show()
                    finish()  // go back to your list
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        }

        // RSVP toggle
        binding.rsvpButton.setOnClickListener {
            toggleRsvp(eventId)
        }

        // load + lay out everything
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

                // 1) Basic fields
                binding.eventTitle.text       = data["title"]       as? String ?: ""
                binding.eventDescription.text = data["description"] as? String ?: ""
                val startTime = data["startTime"] as? String ?: ""
                val endTime   = data["endTime"]   as? String ?: ""
                binding.eventTime.text        = "$startTime – $endTime"
                binding.eventLocation.text    = data["location"] as? String ?: ""

                // 2) Parse attendees (Map<String,Boolean> or List<String>)
                val attendeesMap: Map<String,Boolean> = when (val raw = data["attendees"]) {
                    is Map<*, *> -> raw.entries
                        .filter { it.key is String }
                        .associate { it.key as String to (it.value as? Boolean ?: true) }
                    is List<*>    -> raw.filterIsInstance<String>().associateWith { true }
                    else          -> emptyMap()
                }

                // 3) Set RSVP button text based on if current user is in attendees
                val uid = auth.currentUser?.uid
                val isGoing = uid != null && attendeesMap.containsKey(uid)
                binding.rsvpButton.text = if (isGoing) "Un‑RSVP" else "RSVP"

                // 4) Load host info
                val creatorUid = data["creatorUid"] as? String ?: ""
                db.collection("users").document(creatorUid).get()
                    .addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("name") ?: "Unknown"
                        val username = userDoc.getString("username") ?: "unknown"
                        binding.eventHost.text = "$name (@$username)"
                    }
                    .addOnFailureListener {
                        binding.eventHost.text = "Host: Unknown"
                    }

                // 5) Show attendee names
                if (attendeesMap.isNotEmpty()) {
                    val usernames = mutableListOf<String>()
                    val tasks = attendeesMap.keys.map { attendeeUid ->
                        db.collection("users").document(attendeeUid).get()
                    }
                    Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                        .addOnSuccessListener { docs ->
                            docs.forEach { doc ->
                                val n = doc.getString("name") ?: "Unknown"
                                val u = doc.getString("username") ?: "unknown"
                                usernames.add("$n (@$u)")
                            }
                            binding.eventAttendees.text = "Going:\n" + usernames.joinToString("\n")
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
        // Read current map one more time:
        db.collection("events").document(eventId).get()
            .addOnSuccessListener { snap ->
                val raw = snap.get("attendees")
                val isGoing = when (raw) {
                    is Map<*, *> -> raw.containsKey(uid)
                    is List<*>    -> raw.contains(uid)
                    else          -> false
                }
                val updateOp = if (isGoing) {
                    // remove user
                    mapOf("attendees.$uid" to FieldValue.delete())
                } else {
                    // add user
                    mapOf("attendees.$uid" to true)
                }
            db.collection("events").document(eventId)
                .update(updateOp)
                .addOnSuccessListener {
                    val msg = if (isGoing) "Un‑RSVP’d" else "RSVP’d!"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    // ① Tell Android “this Activity succeeded” (optional)
                    setResult(RESULT_OK)
                    // ② Close it so that BACK (and returning) goes back to the list
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update RSVP", Toast.LENGTH_SHORT).show()
                }
            }
    }
}