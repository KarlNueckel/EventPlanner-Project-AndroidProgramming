package edu.utap.eventplanner

data class Event(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val creatorUid: String = "",
    var attendees: Map<String, Boolean> = emptyMap()

)
