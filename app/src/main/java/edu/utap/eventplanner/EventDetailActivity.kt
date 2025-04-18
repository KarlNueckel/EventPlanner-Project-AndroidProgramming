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



class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//
//        // ✅ Check if user is logged in FIRST
//        if (FirebaseAuth.getInstance().currentUser == null) {
//            val intent = Intent(this, StartActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            return
//        }
//
//
//
//        binding = ActivityEventDetailBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val eventId = intent.getStringExtra("eventId")
//        if (eventId == null) {
//            finish()
//            return
//        }
//
//        loadEvent(eventId)
//
//        binding.backButton.setOnClickListener {
//            finish()
//        }
//
//        binding.rsvpButton.setOnClickListener {
//            val uid = auth.currentUser?.uid ?: return@setOnClickListener
//            db.collection("events").document(eventId)
//                .update("attendees.$uid", true)
//                .addOnSuccessListener {
//                    Toast.makeText(this, "RSVP'd!", Toast.LENGTH_SHORT).show()
//                    loadEvent(eventId) // Refresh attendee list
//                }
//        }
//    }

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
            //Log.d("EventDetail", "Received eventId: $eventId")

            finish()
            return
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
                val event = snapshot.toObject(Event::class.java)
                if (event != null) {
                    binding.eventTitle.text = event.title
                    binding.eventDescription.text = event.description
                    binding.eventTime.text = "${event.startTime} - ${event.endTime}"
                    binding.eventLocation.text = event.location

                    val creatorUid = event.creatorUid
                    db.collection("users").document(creatorUid).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "Unknown"
                            val username = userDoc.getString("username") ?: "unknown"
                            binding.eventHost.text = "$name (@$username)"
                        }

//                    val attendees = (snapshot.get("attendees") as? Map<*, *>)?.keys?.map { it.toString() } ?: listOf()
//                    binding.eventAttendees.text = "Going:\n" + attendees.joinToString("\n")

                    val attendeesMap = snapshot.get("attendees") as? Map<*, *> ?: emptyMap<String, Boolean>()
                    val attendeeUIDs = attendeesMap.keys.map { it.toString() }

                    val usernames = mutableListOf<String>()

                    val db = FirebaseFirestore.getInstance()
                    val tasks = attendeeUIDs.map { uid ->
                        db.collection("users").document(uid).get()
                    }

                    Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { docs ->
                        docs.forEach { doc ->
                            val name = doc.getString("name") ?: "Unknown"
                            val username = doc.getString("username") ?: "unknown"
                            usernames.add("$name (@$username)")
                        }
                        binding.eventAttendees.text = "Going:\n" + usernames.joinToString("\n")
                    }

                }
            }
    }
}
