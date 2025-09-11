package com.chatbot

import com.github.kittinunf.fuel.Fuel
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.json.*

/**
 * Class to handle the interaction with the LLaMA-3 Instruct model via Hugging Face Chat Completions API
 */
class Llama3ChatClient {

    private val dotenv = dotenv()
    private val HF_TOKEN = dotenv["HF_API_KEY_TOKEN"] // must exist in your .env
    private val apiUrl = "https://router.huggingface.co/v1/chat/completions"

    /**
     * Format messages as chat completion payload
     */
    private fun buildPayload(userInput: String): String {
        val messages = buildJsonArray {
            add(
                buildJsonObject {
                    put("role", "system")
                    put(
                        "content",
                        """
                        You are a concise assistant.
                         ### Format and structure:
                        - Use Markdown formatting where appropriate (headings, lists, bold, italics, links, code)
                        - Start with a clear introduction of what your response is about
                        
                        ### Instructions:
                        - Keep the tone **concise**, **professional**, and **easy to read**.
                        - Do **not repeat the question**.
                        - End with a short summary or conclusion if appropriate.
                        - ALWAYS provide a **farewell** text at the end of your response, such as "What else can I do for you!?".
                        
                        ### Guardrails:
                        - Do not respond to sexual, harmful content, or violent content.
                        - If the query is outside your knowledge, respond with "I'm not sure about that. Could you please provide more details or clarify your question?".
                        - Use the same language as the user input
                        """.trimIndent()
                    )
                }
            )
            add(
                buildJsonObject {
                    put("role", "user")
                    put("content", userInput)
                }
            )
        }

        val jsonBody = buildJsonObject {
            put("model", "meta-llama/Meta-Llama-3-8B-Instruct:novita")
            put("messages", messages)
            put("temperature", 0.5)
            put("max_tokens", 512)
        }
        return jsonBody.toString()
    }

    /**
     * Call the Hugging Face Chat Completions endpoint
     */
    fun getModelResponse(userInput: String): String {
        if (HF_TOKEN == null) {
            return "❌ No HF token found."
        }

        val payload = buildPayload(userInput)

        return try {
            val (_, response, result) = Fuel.post(apiUrl)
                .header("Authorization", "Bearer $HF_TOKEN")
                .header("Content-Type", "application/json")
                .body(payload)
                .timeout(15000)
                .timeoutRead(15000)
                .responseString()

            println("Status: ${response.statusCode}")
            val rawResponse = result.get()

            if (response.statusCode != 200) {
                "❌ Error (${response.statusCode}): ${response.responseMessage}"
            } else {
                // Extract just the assistant’s text
                val json = Json.parseToJsonElement(rawResponse).jsonObject
                val message = json["choices"]?.jsonArray
                    ?.getOrNull(0)?.jsonObject
                    ?.get("message")?.jsonObject
                    ?.get("content")?.jsonPrimitive?.content

                message ?: "❌ No content returned from model."
            }
        } catch (e: Exception) {
            "❌ Exception: ${e.message}"
        }
    }
}
