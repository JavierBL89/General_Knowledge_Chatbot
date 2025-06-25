package com.chatbot


import com.github.kittinunf.fuel.Fuel
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.math.pow

import java.sql.Time


@Serializable
data class PromptInput(val inputs: String)

@Serializable
data class PromptResponse(val generated_text: String)


/**
 * Class to handle the interaction with the Zephyr7B model on HF Space
 * **/
class Zephyr7bBeta {

    private val dotenv = dotenv()
    private val HF_TOKEN = dotenv["HF_API_KEY_TOKEN"] // Make sure this exists in your .env

    /**
     * Function to format the user input into a prompt for the model
     * **/
    private fun formatPrompt(userInput: String): String {
        val formattedInput = """
            <|system|>
            You are a concise assistant. Use Markdown formatting where appropriate (headings, lists, bold, italics, links, code).
            
            Instructions:
            1. Start with a clear introduction of what your response is about.
            2. Keep the tone **concise**, **professional**, and **easy to read**.
            3. Do **not repeat the question**.
            4. Avoid vague filler phrases like "it's a good idea" or "you may find that...".
            5. End with a short summary or conclusion if appropriate.
            6. Always provide a **farewell** such as "Feel free to ask if you have more questions!".
            7. Answer in **Markdown format** using **bold**, **lists**, and a **farewell message**.
            <|user|>
            $userInput.
            <|assistant|>
            """.trimIndent()

        val jsonBody = buildJsonObject {
            put("inputs", formattedInput)
        }
        return jsonBody.toString()
   }

    /**
     * Function to format the model response
     * @param zephyrReply The response from the model
     * @return The formatted response
     */
    fun cleanZephyrResponse(raw: String, userQuery: String): String {
        return raw
            // Remove system/instruction block before the user query
            .replace(Regex("(?s)^.*?\\b${Regex.escape(userQuery)}\\b\\s*"), "")
            // Remove special tags like <|user|>, <|system|>, <|assistant|>
            .replace(Regex("<\\|.*?\\|>"), "")
            // Remove extra whitespace
            .replace(Regex("""(\r?\n){3,}"""), "\n\n")
            .trim()
    }



    /**
     * Function to format the user input into a prompt for the model
     * **/
     fun responseFormatWithLines(raw: String): String {
        val result = StringBuilder()
        var i = 0;

        while (i < raw.length) {
            // Detect "digit + dot + space" pattern (e.g., "1. ")
            if (i + 2 < raw.length && raw[i].isDigit() && raw[i + 1] == '.' && raw[i + 2] == ' ') {
                result.append("\n") // Inject a newline before the numbered item
            }
            result.append(raw[i])
            i++
        }
            return result.toString().trim()
     }


    /**
     * Function responsible for making the API call to the model
     * **/
    fun getModelResponseFromHFSpace(input: String): String {
        val customMessage = "🤖 Sorry, the AI model is unavailable. Try again in a moment!"
        HF_TOKEN ?: return "❌ No HF token found."

        val api_url = "https://api-inference.huggingface.co/models/HuggingFaceH4/zephyr-7b-beta"
        val formattedInput = formatPrompt(input)

        var maxAttemps = 1
        val retryDelayMillis = 2000L
        // Retry logic when connection timeout accurs
        while (maxAttemps > 0) {
            try {
                val (_, response, result) = Fuel.post(api_url)
                    .header("Authorization", "Bearer $HF_TOKEN")
                    .header("Content-Type", "application/json")
                    .timeout(15000) // set timeout 15s
                    .timeoutRead(15000)
                    .body(formattedInput.toString())
                    .responseString()

                println("Status: ${response.statusCode}")
                println("Response: ${result.get()}")

                if (response.statusCode != 200) {
                    println("Error (${response.statusCode}): ${response.responseMessage}")
                    return customMessage
                }

                return result.fold(
                    success = { body ->
                        try {
                            val parsedJson = Json.parseToJsonElement(body)
                            if (parsedJson is JsonArray && parsedJson.isNotEmpty()) {
                                val reply = parsedJson[0].jsonObject["generated_text"]?.jsonPrimitive?.content
                                if (reply != null) {
                                    val cleanedReply = cleanZephyrResponse(reply, input)
                                    return cleanedReply
                                }
                            }
                            return "⚠️ Unexpected response format from Zephyr: $body"
                        } catch (e: Exception) {
                            return ("❌ Deserialization error: ${e.message}")
                        }
                    },
                    failure = { error ->
                        return ("❌ HF Space error: ${error.message}")
                    }
                )

            }catch (e: Exception) {
                if (e.message?.contains("Read timed out", ignoreCase = true) == true) {
                    maxAttemps--
                    val backoffDelay = retryDelayMillis * (2.0.pow((2 - maxAttemps).toDouble())).toLong()
                    println("Connection Timeout. Retrying in ${backoffDelay}ms... ($maxAttemps attempts left)")
                    Thread.sleep(backoffDelay)
                } else {
                    return "❌ Unhandled exception: ${e.message}"
                }
                println("❌ FULL ERROR: ${e.message}")
                e.printStackTrace() // <--- add this
            }

        }

        println("⚠️ Zephyr failed after retries. Falling back to Mistral7B.")
        println("⚠ Initiating request in 5 seconds...")
        Thread.sleep(5000)
        try {
            return runMistral7BInIsolatedThread(input)
        } catch (e: Exception) {
            println("❌ Mistral7B fallback failed: ${e.message}")
            return customMessage
        }
    }
}


fun runMistral7BInIsolatedThread(input: String): String {
    val resultHolder = arrayOfNulls<String>(1)
    val thread = Thread {
        try {
            println("Launching Mistral7B fallback...")
            resultHolder[0] = Mistral7bFallBackModelHFAPI().callMistral7b(input)
        } catch (e: Exception) {
            println("❌ Mistral7B isolated thread failed: ${e.message}")
            e.printStackTrace()
            resultHolder[0] = "❌ Mistral7B fallback failed: ${e.message}"
        }
    }
    thread.start()
    thread.join(30000) // wait max 30s
    return resultHolder[0] ?: "❌ Mistral7B did not respond in time."
}


//DeepSeek API (main): Payment is required for using the DeepSeek API, with costs determined by the number of input and output tokens.
//DeepSeek-R1 on OpenRouter: You can access DeepSeek-R1 with a free API key on OpenRouter.
//DeepSeek-R1 on Azure: DeepSeek-R1 endpoints are also available for free on Azure.