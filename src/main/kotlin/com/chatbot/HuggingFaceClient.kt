package com.chatbot

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.cdimascio.dotenv.dotenv


object HuggingFaceClient {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, HuggingFaceResponse::class.java)
    private val adapter = moshi.adapter<List<HuggingFaceResponse>>(listType)

    private val dotenv = dotenv()
    private val API_URL = "https://api-inference.huggingface.co/models/google/flan-t5-large"
    private val API_KEY = dotenv["HUGGINGFACE_API_KEY"]

    suspend fun getModelResponse(input: String): String {
        val (_, _, result) = Fuel.post(API_URL)
            .header("Authorization" to "Bearer $API_KEY")
            .jsonBody("""{"inputs":"$input"}""")
            .responseObject(moshiDeserializerOf(adapter))

        return result.fold(
            success = { it.firstOrNull()?.generated_text ?: "No response" },
            failure = { "Error: ${it.message}" }
        )
    }
}

// ONLY to deserialize the API result.
data class HuggingFaceResponse(val generated_text: String)
