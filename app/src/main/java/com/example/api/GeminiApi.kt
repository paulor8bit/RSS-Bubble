package com.example.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String? = null)

@Serializable
data class GenerateContentResponse(val candidates: List<Candidate>)

@Serializable
data class Candidate(val content: Content)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

suspend fun summarizeNewsContext(title: String, description: String): String {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if(apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return "Resumo indisponível - Configure a chave API do Gemini."
    }
    
    val instruction = "Você é um assistente que resume notícias curtas em pt-BR. Retorne apenas o resumo, sem mais comentários. Tamanho máximo: 2-3 frases."
    val prompt = "Título: $title\n\nDescrição: $description\n\nResuma."
    
    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
        systemInstruction = Content(parts = listOf(Part(text = instruction)))
    )
    
    return try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Erro ao gerar resumo."
    } catch(e: Exception) {
        "Erro ao acessar API do Gemini: ${e.message}"
    }
}
