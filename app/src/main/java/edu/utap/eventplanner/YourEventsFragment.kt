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
    private val userEvents = mutableListOf<Event>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // needed if you ever use onCreateOptionsMenu, but harmless here
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYourEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

//override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//    adapter = EventAdapter()
//    binding.yourEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//    binding.yourEventsRecyclerView.adapter = adapter
//
//    fetchUserEvents()
//}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = EventAdapter(onEventClick = { event ->
            val context = requireContext()
            val intent = Intent(context, EventDetailActivity::class.java)
            intent.putExtra("eventId", event.id)
            intent.putExtra("isOwner", true)  // 👈 creator viewing their own event
            context.startActivity(intent)
        })

        binding.yourEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.yourEventsRecyclerView.adapter = adapter

            // ────────────────────────────────────────────────────
            // ✅  Sign‑Out menu (reuses res/menu/menu_posts.xml)
//            val menuHost: MenuHost = requireActivity()
//            menuHost.addMenuProvider(object : MenuProvider {
//                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                            menuInflater.inflate(R.menu.menu_posts, menu)
//                        }
//                    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                            return when (menuItem.itemId) {
//                                    R.id.menu_sign_out -> {
//                                            FirebaseAuth.getInstance().signOut()
//                                            Intent(requireContext(), StartActivity::class.java).apply {
//                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                                                }.also(::startActivity)
//                                            true
//                                        }
//                                    else -> false
//                                }
//                        }
//                }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    fetchUserEvents()
    }


    private fun fetchUserEvents() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("events")
            .whereEqualTo("creatorUid", uid)
            .get()
            .addOnSuccessListener { result ->
                val newList = result.map { doc ->
                    val event = doc.toObject(Event::class.java)
                    event.id = doc.id  // ✅ set the Firestore doc ID
                    event
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

