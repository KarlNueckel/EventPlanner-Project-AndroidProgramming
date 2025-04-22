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
            val title = binding.titleET.text.toString().trim()
            val description = binding.descriptionET.text.toString().trim()
            val startTime = binding.startTimeET.text.toString().trim()
            val endTime = binding.endTimeET.text.toString().trim()
            val location = binding.locationET.text.toString().trim()
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val event = hashMapOf(
                "title" to title,
                "description" to description,
                "startTime" to startTime,
                "endTime" to endTime,
                "location" to location,
                "creatorUid" to userId,
                "attendees" to emptyMap<String, Boolean>()
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
