package com.example.diary

import java.sql.Time
import java.sql.Date

// 일정 작성
data class Plan(
    val travelDest: String,
    val content: String,
    val travelStart: Date,
    val travelEnd: Date,
    val public: Boolean
)

data class Location(
    val date: Date,
    val timeStart: Time,
    val timeEnd: Time,
    val name: String,
    val address: String,
    val x: String,
    val y: String
)

data class Tag(
    val name: String
)

// 일정 작성 서버에 전달(저장)
data class PlanData(
    val plan: Plan,
    val locations: List<Location>,
    val tags: List<Tag>
)

// 내 일정 목록
data class MyPlan(
    val planId: Int,
    val travelDest: String,
    val content: String,
    val travelStart: Date,
    val travelEnd: Date,
    val createdAt: Date,
    val updatedAt: Date,
    val public: Boolean
)

data class MyTag(
    val tagId: Int,
    val name: String
)

data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val password: String,
    val image: String?
)

data class Origin(
    val userId: Int,
    val username: String,
    val email: String,
    val password: String,
    val image: String?
)

// 서버에 내 일정 목록 불러오기
data class MyPlanListResponse(
    val user: User,
    val origin: Origin,
    val plan: MyPlan,
    val locations: List<LocationDetail>,
    val tags: List<MyTag>
)

// 일정 상세 조회
data class PlanDetail(
    val planId: Int,
    val travelDest: String,
    val content: String,
    val travelStart: Date,
    val travelEnd: Date,
    val createdAt: Date,
    val public: Boolean
)

data class LocationDetail(
    val locationId: Int,
    val date: Date,
    val timeStart: Time,
    val timeEnd: Time,
    val name: String,
    val address: String,
    val x: String,
    val y: String
)

data class TagDetail(
    val tagId: Int,
    val name: String
)

data class PlanLikesList(
    val planId: Int,
    val userId: Int
)

data class PlanDetailResponse(
    val user: User,
    val origin: Origin,
    val plan: PlanDetail,
    val locations: List<LocationDetail>,
    val tags: List<TagDetail>,
    val likes: List<PlanLikesList>
)

// 일정 좋아요 한 유저 목록
data class PlanLikeList(
    val user: User
)

// 회원가입 요청 서버에 전달(저장)
data class JoinData(
    val email: String,
    val password: String,
    val username: String
)

// 로그인 요청 서버에 전달(저장) -> 토큰 전달받음
data class LogInData(
    val email: String,
    val password: String
)