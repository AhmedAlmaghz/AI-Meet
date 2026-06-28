package com.example.network

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiReq(val contents: List<GeminiContent>)

data class GeminiContent(val parts: List<GeminiPart>)

data class GeminiPart(val text: String)

data class GeminiResp(val candidates: List<GeminiCandidate>? = null)

data class GeminiCandidate(val content: GeminiContent? = null)

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiReq
    ): GeminiResp
}

object GeminiNetwork {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun translateInstant(originalText: String, targetLangName: String): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            // Simulated instant fallback if key is not configured in Secrets
            return@withContext "[$targetLangName]: $originalText"
        }
        val prompt = "Translate the following spoken meeting sentence into $targetLangName accurately, naturally, and concisely (only return the translated text): \"$originalText\""
        val req = GeminiReq(listOf(GeminiContent(listOf(GeminiPart(prompt)))))
        try {
            val resp = service.generateContent("gemini-3.5-live-translate-preview", key, req)
            resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: "[$targetLangName] $originalText"
        } catch (e: Exception) {
            "[$targetLangName - Offline] $originalText"
        }
    }

    suspend fun askClassroomTeacher(question: String, classroomTopic: String, langName: String): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext if (langName.contains("عربي")) {
                "أهلاً بك في فصلي الافتراضي حول ($classroomTopic)! إجابة سؤالك باختصار: هذا المفهوم يعتمد على هيكلية معمارية تضمن السرعة والموثوقية."
            } else {
                "Welcome to the ($classroomTopic) virtual classroom! Briefly: this concept relies on a modular architecture ensuring high speed and reliability."
            }
        }
        val prompt = "You are an expert friendly AI Teacher Assistant inside a virtual meeting room about '$classroomTopic'. A student asked: '$question'. Answer concisely in 2-3 helpful sentences in $langName."
        val req = GeminiReq(listOf(GeminiContent(listOf(GeminiPart(prompt)))))
        try {
            val resp = service.generateContent("gemini-3.1-flash-lite", key, req)
            resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: "AI Teacher response temporarily unavailable."
        } catch (e: Exception) {
            "AI Teacher offline mode: Great question about $classroomTopic!"
        }
    }
}
