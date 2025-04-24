package com.chatbot

import ZephyrRequest
import com.github.kittinunf.fuel.Fuel
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*


class Zephyr7b {

    private val dotenv = dotenv()
    private val API_URL = "https://api-inference.huggingface.co/models/HuggingFaceH4/zephyr-7b-beta"
    private val API_KEY = dotenv["HUGGINGFACE_API_KEY"]

    /**
     * Function to format the user input into a prompt for the model
     * **/
    private fun formatPrompt(userInput: String): String {
        return """
        You are a helpful assistant. Respond clearly, factually, and only answer the question below without repeating it.
        User: $userInput
        Bot:
    """.trimIndent()

   }




    /**
     * Function responsible for making the API call to the model
     * **/
     fun getModelResponse(input: String): String {

        val customMessage = "🤖 Sorry, the AI model is temporarily unavailable. Try again in a moment!"
         val formattedInput = formatPrompt(input)

         // formatted input
         val payload = ZephyrRequest(
             inputs = formattedInput,
             parameters = buildJsonObject {
                 put("temperature", JsonPrimitive(0.7))
                 put("max_new_tokens", JsonPrimitive(128))
             }
         )

         val jsonBody = Json.encodeToString(payload)

        val (_, response, result) = Fuel.post(API_URL)
            .header("Authorization" to "Bearer $API_KEY")
            .header("Content-Type", "application/json")
            .body(jsonBody)
            .responseString()

        if (response.statusCode != 200) {
            println("Error (${response.statusCode}): ${response.responseMessage}")
            return customMessage
        }

        return result.fold(
            success = { body ->
                try {
                    val jsonResponse = Json.parseToJsonElement(body)
                    jsonResponse.jsonArray.firstOrNull()
                        ?.jsonObject?.get("generated_text")
                        ?.jsonPrimitive?.content ?: "No response from model."
                } catch (e: Exception) {
                    println("Deserialization error: ${e.message}")
                    customMessage
                }
            },
            failure = { error ->
                println("Request error: ${error.message}")
                customMessage
            }
        )
    }
}
