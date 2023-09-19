package com.example.diary

data class SearchResponse (
    val documents: List<Place>,
)

data class Place (
    val id: String,
    val place_name: String,
    val phone: String,
    val address_name: String,       //전체 지번 주소
    val road_address_name: String,  //전체 도로명 주소
    val x: String,
    val y: String
)