////package edu.utap.eventplanner
////
////import android.os.Bundle
////import android.view.LayoutInflater
////import android.view.View
////import android.view.ViewGroup
////import androidx.fragment.app.Fragment
////
////class PostsFragment : Fragment() {
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? {
////        return inflater.inflate(R.layout.fragment_posts, container, false)
////    }
////}
//
//
//
//
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
//class PostsFragment : Fragment() {
//
//    private var _binding: FragmentPostsBinding? = null
//    private val binding get() = _binding!!
//    private val db = FirebaseFirestore.getInstance()
//    private lateinit var adapter: EventAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentPostsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}




package edu.utap.eventplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.eventplanner.databinding.FragmentPostsBinding

class PostsFragment : Fragment() {

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val adapter = EventAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.postsRecyclerView.adapter = adapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("events")
                .whereNotEqualTo("creatorUid", currentUser.uid)
                .get()
//                .addOnSuccessListener { result ->
//                    val events = result.toObjects(Event::class.java)
//                    adapter.setEvents(events)
//                }
                .addOnSuccessListener { result ->
                    val events = result.map { doc ->
                        val event = doc.toObject(Event::class.java)
                        event.id = doc.id  // Store Firestore doc ID for navigation
                        event
                    }
                    adapter.setEvents(events)
                }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



