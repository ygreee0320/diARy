package com.example.diary

import android.util.Log
import com.bumptech.glide.module.AppGlideModule
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class ApiSearchImg{
    val clientId = "PTbDG8TOZrpK0jDyPC1u"   //애플리케이션 클라이언트 아이디
    val clientSecret = "_xvsnPcwRQ"         //애플리케이션 클라이언트 시크릿

    val requestHeaders = mapOf<String, String>("X-Naver-Client-Id" to clientId, "X-Naver-Client-Secret" to clientSecret)

    lateinit var text: String //검색어

    fun searchImg(text: String): String {
        var image = "image/sample.png" //기본 이미지

        this.text = text

        val apiURL = "https://openapi.naver.com/v1/search/image?query=${text}&display=1&sort=sim"; //JSON 결과
        val responseBody = get(apiURL, requestHeaders)

        Log.d("mylog", "이미지 검색 테스트 - ${responseBody}")

        //JSON 파싱 - 이미지 링크 추출
        val items = JSONObject(responseBody).getJSONArray("items")
        if (items.length() > 0) {
            var src = items.getJSONObject(0).getString("thumbnail")
            image = src

            Log.d("mylog", "추출된 링크 - ${src}")
        } else {
            Log.d("mylog", "링크를 찾을 수 없습니다.")
        }

        Log.d("mylog", "전달될 링크 - ${image}")
        return image
    }

    fun get(apiUrl: String, requestHeaders: Map<String, String>): String {
        val con: HttpURLConnection = connect(apiUrl)

        try {
            con.setRequestMethod("GET");
            for (header in requestHeaders) {
                con.setRequestProperty(header.key, header.value)
            }

            val responseCode = con.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) { //정상 호출
                return readBody(con.inputStream)
            } else { //오류 발생
                return readBody(con.errorStream)
            }
        } catch (e: IOException) {
            throw RuntimeException("API 요청과 응답 실패")
        } finally {
            con.disconnect()
        }
    }

    fun connect(apiUrl: String): HttpURLConnection {
        try {
            val url: URL = URL(apiUrl)
            return url.openConnection() as HttpURLConnection
        } catch (e: MalformedURLException) {
            throw RuntimeException("API URL이 잘못되었습니다: ${apiUrl}")
        } catch (e: IOException) {
            throw RuntimeException("연결이 실패했습니다: ${apiUrl}")
        }
    }

    fun readBody(body: InputStream): String {
        val streamReader = InputStreamReader(body)

        try {
            BufferedReader(streamReader).use { lineReader ->
                val responseBody = StringBuilder()
                var line: String?
                while (lineReader.readLine().also { line = it } != null) {
                    responseBody.append(line)
                }
                return responseBody.toString()
            }
        } catch (e: IOException) {
            throw RuntimeException("API 응답을 읽는 데 실패했습니다.")
        }
    }

}