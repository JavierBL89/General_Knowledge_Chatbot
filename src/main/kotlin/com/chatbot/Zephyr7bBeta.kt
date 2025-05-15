package com.chatbot


import com.github.kittinunf.fuel.Fuel
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*



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
                    User query: $userInput.
                    Instructions:  Format your answer in Markdown using numbered lists, bullet points and bold titles when needed to the user query. Instructions End.
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
    fun cleanZephyrResponse(raw: String): String {
        return raw
            .replace(Regex("""\[/?(INST|ASSIST|Assistant Query|USER)\]""", RegexOption.IGNORE_CASE), "") // strip tags
            .replace(Regex("""(?is)User\s*query\s*:\s*.*?Instructions\s*End\.?"""), "") // remove everything between 'User query:' and 'Instructions End'            .replace(Regex("""^.*?\[/?INST\]""", RegexOption.IGNORE_CASE), "") // removes any echoed prompt before INST
            .replace(Regex("""^\s*\n""", RegexOption.MULTILINE), "") // cleans up blank leading lines
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



        var maxAttemps = 3
        val retryDelayMillis = 2000
        // Retry logic when connection timeout accurs
        while (maxAttemps > 0) {
            try {
                val (_, response, result) = Fuel.post(api_url)
                    .header("Authorization", "Bearer $HF_TOKEN")
                    .header("Content-Type", "application/json")
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
                            val replyList = Json.decodeFromString<List<PromptResponse>>(body)
                            val reply = replyList.firstOrNull()?.generated_text ?: "❌ No response."
                            val cleanedReply = cleanZephyrResponse(reply)
                            return cleanedReply
                           // return responseFormatWithLines(cleanedReply)

                        } catch (e: Exception) {
                            return ("❌ Deserialization error: ${e.message}")
                        }
                    },
                    failure = { error ->
                        return ("❌ HF Space error: ${error.message}")
                    }
                )
            }catch (e: Exception) {
                if (e.message == "Read timed out") {
                    println("Connection Timeout")
                    println("Attempting to reconnect...")
                    maxAttemps--
                    Thread.sleep(700)

                }

                println("❌ FULL ERROR: ${e.message}")
                e.printStackTrace() // <--- add this
            }

        }
        // ⛔ Zephyr failed — Fallback to FlanT5
        println("⚠️ Falling back to FlanT5 model after Zephyr failed.")

//        val flanFallback = FlanT5_FallBackModel()
//        return flanFallback.callFlanT5(formattedInput)
        return customMessage
    }
}


//DeepSeek API (main): Payment is required for using the DeepSeek API, with costs determined by the number of input and output tokens.
//DeepSeek-R1 on OpenRouter: You can access DeepSeek-R1 with a free API key on OpenRouter.
//DeepSeek-R1 on Azure: DeepSeek-R1 endpoints are also available for free on Azure.