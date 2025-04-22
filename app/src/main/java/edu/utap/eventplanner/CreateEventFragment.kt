package edu.utap.eventplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.eventplanner.databinding.FragmentCreateEventBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CreateEventFragment : Fragment() {
    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)

        binding.createEventButton.setOnClickListener {
            val title       = binding.titleET.text.toString().trim()
            val description = binding.descriptionET.text.toString().trim()
            val startTime   = binding.startTimeET.text.toString().trim()
            val endTime     = binding.endTimeET.text.toString().trim()
            val location    = binding.locationET.text.toString().trim()
            val userId      = auth.currentUser?.uid ?: return@setOnClickListener

            // 1) basic empty check
            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2) parse & validate “date + time”
            val dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val startDT: LocalDateTime
            val endDT:   LocalDateTime
            try {
                startDT = LocalDateTime.parse(startTime, dtFmt)
                endDT   = LocalDateTime.parse(endTime,   dtFmt)
            } catch (e: DateTimeParseException) {
                Toast.makeText(
                    requireContext(),
                    "Date+time must be in format YYYY‑MM‑DD HH:mm (e.g. 2025-04-23 13:45)",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 3) ensure end is after start
            if (!endDT.isAfter(startDT)) {
                Toast.makeText(
                    requireContext(),
                    "End must be after start",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 4) OK! build & save
            val event = hashMapOf(
                "title"       to title,
                "description" to description,
                "startTime"   to startTime,
                "endTime"     to endTime,
                "location"    to location,
                "creatorUid"  to userId,
                "attendees"   to emptyMap<String, Boolean>()
            )

            db.collection("events").add(event)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Event created!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to create event", Toast.LENGTH_SHORT).show()
                }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
