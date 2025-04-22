package edu.utap.eventplanner

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.eventplanner.databinding.FragmentPostsBinding

import androidx.core.view.MenuProvider
import androidx.core.view.MenuHost
import androidx.lifecycle.Lifecycle

import androidx.core.widget.addTextChangedListener


class PostsFragment : Fragment() {

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: EventAdapter


    private var fullEventsList: List<Event> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // 👈 enable options menu
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    adapter = EventAdapter(onEventClick = { event ->
        val context = requireContext()
        val intent = Intent(context, EventDetailActivity::class.java)
        intent.putExtra("eventId", event.id)
        intent.putExtra("isOwner", false)
        context.startActivity(intent)
    })

    binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    binding.postsRecyclerView.adapter = adapter

    // ✅ Fetch all events (not created by current user)
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        db.collection("events")
            .whereNotEqualTo("creatorUid", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                fullEventsList = result.map { doc ->
                    val event = doc.toObject(Event::class.java)
                    event.id = doc.id
                    event
                }
                adapter.setEvents(fullEventsList)
            }
    }

    // ✅ Search filter
    binding.searchBar.addTextChangedListener {
        val query = it.toString().trim()
        val filtered = if (query.isEmpty()) {
            fullEventsList
        } else {
            fullEventsList.filter { event ->
                event.title.contains(query, ignoreCase = true)
            }
        }
        adapter.setEvents(filtered)
    }
}


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
