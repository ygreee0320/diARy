package com.example.diary

import java.sql.Date
import java.sql.Time

data class DiaryDto(
    val title: String,
    val satisfaction: Int,
    val public: Boolean,
    val travelStart: Date,
    val travelEnd: Date,
    val travelDest: String,
    val memo: String,
    val tags: List<TagName>
)

data class DiaryUser(
    val userId: Int

)

data class TagName(
    val name: String
)

data class DiaryLocationDto(
    val date: Date,
    val timeStart: Time,
    val timeEnd: Time,
    val content: String,
    val name: String,
    val address: String,
    val diaryLocationImageDtoList: List<DiaryLocationImageDto>
)

data class DiaryLocationImageDto( //수정 필요
    val imageData: String
)

// 일기 작성
data class DiaryData(
    val diaryDto: DiaryDto,
    val diaryLocationDtoList: List<DiaryLocationDto>
)

// 유저별 일기 목록
data class MyDiaryList(
    val userDto: User,
    val diaryDto: DiaryDto,
)

data class MyDiary(
    val diaryId: Int,
    val travelDest: String,
    val title: String,
    val travelStart: String, //임시 스트링
    val travelEnd: String,
    val diaryLike: Int,
    val comment: Int,
    val createdAt: String,
    val updatedAt: String,
    val public: Boolean
)


