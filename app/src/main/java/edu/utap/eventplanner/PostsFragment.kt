//
//package edu.utap.eventplanner
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import edu.utap.eventplanner.databinding.FragmentPostsBinding
//
//import android.content.Intent
//
//
//
//
//class PostsFragment : Fragment() {
//
//    private var _binding: FragmentPostsBinding? = null
//    private val binding get() = _binding!!
//    private val db = FirebaseFirestore.getInstance()
//    private lateinit var adapter: EventAdapter  // make this lateinit so we can initialize later
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentPostsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        adapter = EventAdapter(onEventClick = { event ->
//            val context = requireContext()
//            val intent = Intent(context, EventDetailActivity::class.java)
//            intent.putExtra("eventId", event.id)
//            intent.putExtra("isOwner", false)  // 👈 Not the creator
//            context.startActivity(intent)
//        })
//
//        binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.postsRecyclerView.adapter = adapter
//
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null) {
//            db.collection("events")
//                .whereNotEqualTo("creatorUid", currentUser.uid)
//                .get()
//                .addOnSuccessListener { result ->
//                    val events = result.map { doc ->
//                        val event = doc.toObject(Event::class.java)
//                        event.id = doc.id
//                        event
//                    }
//                    adapter.setEvents(events)
//                }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
//
//
//
//



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


class PostsFragment : Fragment() {

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: EventAdapter

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

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        adapter = EventAdapter(onEventClick = { event ->
//            val context = requireContext()
//            val intent = Intent(context, EventDetailActivity::class.java)
//            intent.putExtra("eventId", event.id)
//            intent.putExtra("isOwner", false)
//            context.startActivity(intent)
//        })
//
//        binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.postsRecyclerView.adapter = adapter
//
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null) {
//            db.collection("events")
//                .whereNotEqualTo("creatorUid", currentUser.uid)
//                .get()
//                .addOnSuccessListener { result ->
//                    val events = result.map { doc ->
//                        val event = doc.toObject(Event::class.java)
//                        event.id = doc.id
//                        event
//                    }
//                    adapter.setEvents(events)
//                }
//        }
//    }
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

    // 👇 INSERT THIS HERE
    val menuHost: MenuHost = requireActivity()
    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_posts, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.menu_sign_out -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), StartActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    // 👇 Fetch posts
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        db.collection("events")
            .whereNotEqualTo("creatorUid", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                val events = result.map { doc ->
                    val event = doc.toObject(Event::class.java)
                    event.id = doc.id
                    event
                }
                adapter.setEvents(events)
            }
    }
}


//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_posts, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.menu_sign_out -> {
//                FirebaseAuth.getInstance().signOut()
//                val intent = Intent(requireContext(), StartActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
