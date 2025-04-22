package edu.utap.eventplanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.eventplanner.databinding.FragmentRsvpsBinding

class YourRSVPsFragment : Fragment() {

    private var _binding: FragmentRsvpsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRsvpsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = EventAdapter(onEventClick = { event ->
            val context = requireContext()
            val intent = Intent(context, EventDetailActivity::class.java)
            intent.putExtra("eventId", event.id)
            intent.putExtra("isOwner", false) // RSVP'd to it but didn’t create it
            context.startActivity(intent)
        })

        binding.rsvpsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.rsvpsRecyclerView.adapter = adapter

        fetchRSVPEvents()
    }

    private fun fetchRSVPEvents() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("events")
            .whereGreaterThan("attendees.$uid", false)
            .get()
            .addOnSuccessListener { result ->
                val events = result.map { doc ->
                    val event = doc.toObject(Event::class.java)
                    event.id = doc.id
                    event
                }
                adapter.setEvents(events)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load RSVP events", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

