package com.example.diary

import java.util.*

data class Plan(
    val travelDest: String,
    val content: String,
    val travelStart: Date,
    val travelEnd: Date,
    val Public: Boolean
)

data class Location(
    val date: Date,
    val time: String,
    val name: String,
    val address: String
)

data class Tag(
    val name: String
)

//서버에 전달(저장)
data class PlanData(
    val plan: Plan,
    val locations: List<Location>,
    val tags: List<Tag>
)

data class ApiResponse(
    val plans: List<Plan>,
    val locations: List<Location>,
    val tags: List<Tag>
)
