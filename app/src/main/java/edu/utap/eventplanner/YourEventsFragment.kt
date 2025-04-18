//package edu.utap.eventplanner
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import edu.utap.eventplanner.databinding.FragmentYourEventsBinding
//
//class YourEventsFragment : Fragment() {
//
//    private var _binding: FragmentYourEventsBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentYourEventsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        binding.yourEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        // Later, set adapter here
//    }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYourEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        adapter = EventAdapter(userEvents)
//        binding.yourEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.yourEventsRecyclerView.adapter = adapter
//
//        fetchUserEvents()
//    }
//
//    private fun fetchUserEvents() {
//        val uid = auth.currentUser?.uid ?: return
//
//        db.collection("events")
//            .whereEqualTo("creatorUid", uid)
//            .get()
//            .addOnSuccessListener { result ->
//                userEvents.clear()
//                for (doc in result) {
//                    val event = doc.toObject(Event::class.java)
//                    userEvents.add(event)
//                }
//                adapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener {
//                Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
//            }
//    }
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    adapter = EventAdapter()
    binding.yourEventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    binding.yourEventsRecyclerView.adapter = adapter

    fetchUserEvents()
}

    private fun fetchUserEvents() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("events")
            .whereEqualTo("creatorUid", uid)
            .get()
            .addOnSuccessListener { result ->
                val newList = result.toObjects(Event::class.java)
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

