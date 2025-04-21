//package edu.utap.eventplanner
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import edu.utap.eventplanner.databinding.ItemEventBinding
//
//import android.content.Intent
//
//
//class EventAdapter : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
//
//    private var events: List<Event> = emptyList()
//
//
//    fun setEvents(newEvents: List<Event>) {
//        events = newEvents
//        notifyDataSetChanged()
//    }
//
//
//    inner class EventViewHolder(private val binding: ItemEventBinding) :
//
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(event: Event) {
//            binding.eventTitle.text = event.title
//            binding.eventTime.text = "${event.startTime} - ${event.endTime}"
//            binding.eventLocation.text = event.location
//
//            binding.root.setOnClickListener {
//                val context = binding.root.context
//                val intent = Intent(context, EventDetailActivity::class.java)
//                intent.putExtra("eventId", event.id) // Make sure your Event model has this
//                context.startActivity(intent)
//            }
//        }
//    }
//
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
//        val binding = ItemEventBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return EventViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
//        holder.bind(events[position])
//    }
//
//    override fun getItemCount(): Int = events.size
//}




package edu.utap.eventplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.utap.eventplanner.databinding.ItemEventBinding

class EventAdapter(
    private var events: List<Event> = emptyList(),
    private val onEventClick: (Event) -> Unit  // 👈 this allows custom click behavior
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    fun setEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventTitle.text = event.title
            binding.eventTime.text = "${event.startTime} - ${event.endTime}"
            binding.eventLocation.text = event.location

            binding.root.setOnClickListener {
                onEventClick(event)  // 👈 call the click handler
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}

