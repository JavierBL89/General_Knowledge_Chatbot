## First model to try

2. **FLAN-T5** is not really chat-optimized
The model you're using (google/flan-t5-large) is:

- A text-to-text model fine-tuned on instruction-following tasks (not casual conversation).
- Not designed for dialogue flow or multi-turn chat.
- It often hallucinates (makes up info) or gives unexpected completions because it's not fine-tuned for chatlike interaction.

2. It always expects a “task”
   FLAN-T5 was trained more like:

"Translate English to German: Hello"
"Summarize this: <text>"
"Answer the question: What is AI?"

So without a format like that, it gets confused.

3. What you can do:
   ✅ Use better prompts
   Format the input more like a task:

val betterPrompt = "Answer like a helpful assistant: $input"

   ✅ Switch to a chat-optimized model
   
   - tiiuae/falcon-7b-instruct
   - meta-llama/Llama-2-7b-chat
   - mistralai/Mistral-7B-Instruct
   
   These are specifically tuned to give better answers in a chatbot-style flow.
   
   ⚠️ But these chat models may require more memory or not be available on free-tier Hugging Face Inference API.