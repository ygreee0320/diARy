package com.example.diary

import com.google.gson.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.lang.reflect.Type
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

class MyApplication {
    val retrofit = Retrofit.Builder()
        .baseUrl("http:/192.168.84.222:8080/") // 서버 임시 URL(로컬)
        .addConverterFactory(GsonConverterFactory.create(getGson()))
        .build()

    val mapApi = Retrofit.Builder()
        .baseUrl("https://dapi.kakao.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getGson(): Gson {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()) // 원하는 시간 형식 지정

        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd") // Date 형식 지정
            .registerTypeAdapter(Time::class.java, TimeSerializer(timeFormat))
            .registerTypeAdapter(Time::class.java, TimeDeserializer(timeFormat))

            //.registerTypeAdapter(Time::class.java, timeDeserializer) // 시간 변환기 등록
            .create()
    }

    val searchService = mapApi.create(SearchService::class.java)

    val planService = retrofit.create(PlanService::class.java)
    val myPlanService = retrofit.create(MyPlanService::class.java)
    val planDetailService = retrofit.create(PlanDetailService::class.java)
    val deletePlanService = retrofit.create(DeletePlanService::class.java)
    val modPlanService = retrofit.create(ModPlanService::class.java)

    val planLikeListService = retrofit.create(PlanLikeListService::class.java)
    val planLikeService = retrofit.create(PlanLikeService::class.java)
    val deletePlanLikeService = retrofit.create(DeletePlanLikeService::class.java)

    val planTakeInService = retrofit.create(PlanTakeInService::class.java)

    val diaryService = retrofit.create(DiaryService::class.java)
    val myDiaryService = retrofit.create(MyDiaryService::class.java)
    val mapDiaryService = retrofit.create(MapDiaryService::class.java)
    val diaryDetailService = retrofit.create(DiaryDetailService::class.java)
    val deleteDiaryService = retrofit.create(DeleteDiaryService::class.java)
    val modDiaryService = retrofit.create(ModDiaryService::class.java)

    val creatediaryLikeService = retrofit.create(CreateDiaryLikeService::class.java)
    val deleteDiaryLikeService = retrofit.create(DeleteDiaryLikeService::class.java)

    val commentService = retrofit.create(CommentService::class.java)
    val commentListService = retrofit.create(CommentListService::class.java)

    val loginService = retrofit.create(LogInService::class.java)
    val joinService = retrofit.create(JoinService::class.java)
    val myPageService = retrofit.create(MyPageService::class.java)

    val hotTopicService = retrofit.create(HotTopicService::class.java)
    val rankingService = retrofit.create(RankingService::class.java)

    val searchTagDiaryService = retrofit.create(TagDiarySearchService::class.java)
    val searchWriterDiaryService = retrofit.create(WriterDiarySearchService::class.java)
    val searchDestDiaryService = retrofit.create(DestDiarySearchService::class.java)

    val searchTagRecentDiaryService = retrofit.create(TagRecentDiarySearchService::class.java)
    val searchWriterRecentDiaryService = retrofit.create(WriterRecentDiarySearchService::class.java)
    val searchDestRecentDiaryService = retrofit.create(DestRecentDiarySearchService::class.java)

    val searchTagPlanService = retrofit.create(TagPlanSearchService::class.java)
    val searchWriterPlanService = retrofit.create(WriterPlanSearchService::class.java)
    val searchDestPlanService = retrofit.create(DestPlanSearchService::class.java)

    val searchTagRecentPlanService = retrofit.create(TagRecentPlanSearchService::class.java)
    val searchWriterRecentPlanService = retrofit.create(WriterRecentPlanSearchService::class.java)
    val searchDestRecentPlanService = retrofit.create(DestRecentPlanSearchService::class.java)
}

class TimeSerializer(private val timeFormat: SimpleDateFormat) : JsonSerializer<Time> {
    override fun serialize(src: Time?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(timeFormat.format(src))
    }
}

class TimeDeserializer(private val timeFormat: SimpleDateFormat) : JsonDeserializer<Time> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Time {
        val timeString = json?.asString
        val time = timeFormat.parse(timeString)
        return Time(time.time)
    }
}