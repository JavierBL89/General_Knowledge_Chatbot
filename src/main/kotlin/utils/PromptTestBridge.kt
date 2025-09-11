package utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PromptTestBridge {

    fun testUserInput(user_query: String, model_reply: String): Int {

        user_query.trim()

        val cleanedOutput = model_reply
            .replace(Regex("(?i)\\*\\*.*?\\*\\*"), "") // remove Markdown bold
            .replace(Regex("(?i)farewell.*"), "")     // remove farewell
            .replace(Regex("(?i)_⚠️.*"), "")           // remove model fallback warning
            .trim()

        val jsonEntryPrompt = mapOf("prompt" to user_query)
        val jsonEntryOutput = mapOf("output" to cleanedOutput
                )

        val promptJsonlPath = "src/test/assert_eval/model_prompt.jsonl"
        File(promptJsonlPath).writeText("") // clear file
        File(promptJsonlPath).appendText(jsonEntryPrompt.toString())


        // 2. Write model outputs to JSON file
        val outputsPath = "src/test/assert_eval/model-outputs.json"
        File(outputsPath).writeText(
            """[
        ${Json.encodeToString(jsonEntryOutput)}
        ]""".trimIndent()
        )

        // 3. Write assertions
        val promptfooPath = "src/test/assert_eval/promptfooconfig.yaml"
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

        // 4. Start the promptfoo view server
        val viewProcess = ProcessBuilder(
            "promptfoo", "view"
        )
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
        // 5. Wait a few seconds for the server to start
        Thread.sleep(3000) // Adjust as needed

        // 6. Run `promptfoo eval` command
        val result = ProcessBuilder(
            "promptfoo",
            "eval",
            "--config", "src/test/assert_eval/promptfooconfig.yaml",
            "--model-outputs", outputsPath, // model output as JSON or JSONL
        ).inheritIO().start().waitFor()

        ProcessBuilder(
            "node", "frontend/js/saveReport.js" // or "./scripts/saveReport.js" if it’s in a subfolder
        ).inheritIO().start().waitFor()


        println("Promptfoo eval completed with code: $result")

        return result
        }
}