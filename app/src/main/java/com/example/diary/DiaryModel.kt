package com.example.diary

import org.w3c.dom.Comment
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

data class DiaryDtoMyList(
    val diaryId: Int,
    val title: String,
    val travelDest: String,
    val memo: String,
    val satisfaction: Int,
    val travelStart: Date,
    val travelEnd: Date,
    val tags: List<TagName>,
    val likes: List<LikesList>,
    val comments: List<CommentList>,
    val public: Boolean,
)

data class DiaryLocationDtoList(
    val diaryLocationId: Int,
    val diaryId: Int,
    val date: Date,
    val timeStart: Time,
    val timeEnd: Time,
    val content: String,
    val name: String,
    val address: String,
    val diaryLocationImageDtoList: List<DiaryLocationImageDto>
)

data class TagName(
    val name: String
)

data class CommentList(
    val commentId: Int,
    val diaryId: Int,
    val content: String,
    val createdAt: String,
    val userId: Int,
    val updatedAt: String,
    val comment: Comment, //답글, 리스트 형식이라 수정 필요
)

data class ReplyList(
    val commentId: Int,
    val diaryId: Int,
    val content: String,
    val userId: Int
)

data class LikesList(
    val diaryId: Int,
    val userId: Int,
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
    val diaryDto: DiaryDtoMyList,
    val diaryLocationDtoList: List<DiaryLocationDtoList>
)

// 일기 상세 불러오기
data class DiaryDetailResponse(
    val userDto: User,
    val diaryDto: DiaryDtoMyList,
    val diaryLocationDtoList: List<DiaryLocationDtoList>,
)

// 댓글 작성
data class CommentData(
    val content: String
)

// 일기 별 댓글 조회
data class CommentListResponse(
    val diaryId: Int,
    val content: String,
    val userId: Int,
    val replyDtos: List<ReplyList>
)