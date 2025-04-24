

package edu.utap.eventplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.utap.eventplanner.databinding.ItemEventBinding

class EventAdapter(
    private var events: List<Event> = emptyList(),
    private val onEventClick: (Event) -> Unit
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
                onEventClick(event)
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

