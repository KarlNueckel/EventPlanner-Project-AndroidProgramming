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
import edu.utap.eventplanner.databinding.FragmentYourEventsBinding

class YourEventsFragment : Fragment() {

    private var _binding: FragmentYourEventsBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // harmless unless you actually inflate an options menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYourEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = EventAdapter(onEventClick = { event ->
            val intent = Intent(requireContext(), EventDetailActivity::class.java).apply {
                putExtra("eventId", event.id)
                putExtra("isOwner", true)
            }
            startActivity(intent)
        })

        binding.yourEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.yourEventsRecyclerView.adapter = adapter

        fetchUserEvents()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list whenever we return from detail/delete/RSVP
        fetchUserEvents()
    }

    private fun fetchUserEvents() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("events")
            .whereEqualTo("creatorUid", uid)
            .get()
            .addOnSuccessListener { result ->
                val newList = result.map { doc ->
                    val data = doc.data

                    // coerce attendees field to Map<String,Boolean>
                    val attendeesMap: Map<String, Boolean> = when (val raw = data["attendees"]) {
                        is Map<*, *> -> raw.entries
                            .filter { it.key is String }
                            .associate { it.key as String to (it.value as? Boolean ?: true) }
                        is List<*>    -> raw.filterIsInstance<String>().associateWith { true }
                        else          -> emptyMap()
                    }

                    Event(
                        id = doc.id,
                        title = data["title"]       as? String ?: "",
                        description = data["description"] as? String ?: "",
                        startTime = data["startTime"]   as? String ?: "",
                        endTime = data["endTime"]       as? String ?: "",
                        location = data["location"]     as? String ?: "",
                        creatorUid = data["creatorUid"] as? String ?: "",
                        attendees = attendeesMap
                    )
                }

                adapter.setEvents(newList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}