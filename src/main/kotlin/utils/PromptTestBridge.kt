package utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class PromptTestBridge {

    private val backgroundExecutor = Executors.newCachedThreadPool()

    fun testUserInput(user_query: String, model_reply: String): Int {
        // Start the evaluation in the background - DON'T WAIT
        backgroundExecutor.submit {
            try {
                runEvaluation(user_query, model_reply)
            } catch (e: Exception) {
                println("Background evaluation failed: ${e.message}")
            }
        }

        // Return immediately - don't block the bot response
        println("Evaluation started in background")
        return 0 // Return success immediately
    }

    private fun runEvaluation(user_query: String, model_reply: String) {
        user_query.trim()

        val cleanedOutput = model_reply
            .replace(Regex("(?i)\\*\\*.*?\\*\\*"), "") // remove Markdown bold
            .replace(Regex("(?i)farewell.*"), "")     // remove farewell
            .replace(Regex("(?i)_⚠️.*"), "")           // remove model fallback warning
            .trim()

        val jsonEntryPrompt = mapOf("prompt" to user_query)
        val jsonEntryOutput = mapOf("output" to cleanedOutput)

        // Create the directory first
        File("assert_eval").mkdirs()

        val promptJsonlPath = "assert_eval/model_prompt.jsonl"
        File(promptJsonlPath).writeText("") // clear file
        File(promptJsonlPath).appendText(jsonEntryPrompt.toString())

        // 2. Write model outputs to JSON file
        val outputsPath = "assert_eval/model-outputs.json"
        File(outputsPath).writeText(
            """[
        ${Json.encodeToString(jsonEntryOutput)}
        ]""".trimIndent()
        )

        // 3. Write assertions
        val promptfooPath = "assert_eval/promptfooconfig.yaml"
        File(promptfooPath).writeText("""
        prompts: $user_query

        providers:
          - openai:gpt-4
        
        tests:
          - vars:
              input: $user_query
            assert:
              - type: model-graded-closedqa
                value: Does the output provide a clear and complete answer to the prompt?
        
              - type: model-graded-closedqa
                value: Does the response stay relevant to the user's prompt?
        
              - type: model-graded-closedqa
                value: Is the output coherent, logically structured, and easy to follow?
        """.trimIndent())

        // 4. Start the promptfoo view server (optional - skip if not needed)
        val viewProcess = ProcessBuilder("promptfoo", "view")
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        // 5. Wait a few seconds for the server to start
        Thread.sleep(3000)

        // 6. Run `promptfoo eval` command
        val result = ProcessBuilder(
            "promptfoo",
            "eval",
            "--config", "assert_eval/promptfooconfig.yaml",
            "--model-outputs", outputsPath
        ).inheritIO().start().waitFor()

        ProcessBuilder(
            "node", "frontend/js/saveReport.js"
        ).inheritIO().start().waitFor()

        println("Background evaluation completed with code: $result")

        // Clean up view process
        viewProcess.destroyForcibly()
    }
}