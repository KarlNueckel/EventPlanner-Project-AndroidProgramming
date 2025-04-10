package edu.utap.eventplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.utap.eventplanner.databinding.FragmentRsvpsBinding

class YourRSVPsFragment : Fragment() {

    private var _binding: FragmentRsvpsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRsvpsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rsvpsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Later, set adapter here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
