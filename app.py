refrom transformers import AutoTokenizer, AutoModelForSeq2SeqLM, pipeline
from flask import Flask, request, jsonify

import numpy
import torch


# Initialize app
app = Flask(__name__)

print("NumPy version:", numpy.__version__)
print("PyTorch version:", torch.__version__)

# Load tokenizer + model
tokenizer = AutoTokenizer.from_pretrained("google/flan-t5-base")
model = AutoModelForSeq2SeqLM.from_pretrained("google/flan-t5-base")

# Build pipeline with tokenizer explicitly
generator = pipeline("text2text-generation", model=model, tokenizer=tokenizer)


def generate_response(prompt):
    """Helper function for consistent generation settings."""
    return generator(
        prompt,
        max_new_tokens=250,
        min_new_tokens=80,  # Forces longer responses
        temperature=0.3,
        top_k=50,
        top_p=0.95,
        num_beams=3,   # Beam search for better coherence
        do_sample=True,
        early_stopping=False,  # Crucial - prevents premature stopping
        no_repeat_ngram_size=3,  # Avoid redundancy # Blocks repeating 3-word phrases
        length_penalty=1.1,    # Encourages longer responses
        repetition_penalty=1.5  # Avoids repetition
    )[0]["generated_text"]


def apply_chain(user_input, role="Assistant", tone="professional", instructions=None):
    """Chain: Role → Tone → Instruction → Final Output"""
    # Step 1: Role-Specific Task
    role_prompt = f"""As a {role}, generate 2-4 key points about: "{user_input}".
    Make each point detailed and practical."""
    role_output = generate_response(role_prompt)

    # Step 2: Instruction Enforcement
    if instructions:
        instruction_prompt = f"""{instructions} \n{role_output}"""
        instruction_output = generate_response(instruction_prompt)
    else:
        instruction_output = role_output

    # Step 3: Tone Refinement (with length preservation)
    tone_prompt = f"""Rewrite this in a {tone} tone WITHOUT shortening:\n{instruction_output}"""
    final_output = generate_response(tone_prompt)

    return final_output


@app.route("/local_flanT5/predict", methods=["POST"])
def predict_flan_t5():
    user_input = request.json.get("inputs", "")
    if not user_input:
        return jsonify({"error": "No input provided"}), 400

    role = "Expert"  # Default: Generic Assistant
    tone = "Professional"  # Default: Professional
    instructions = "Use numbered Markdown list with scientific references to convert this ->"  # Optional

    final_answer = apply_chain(user_input, role, tone, instructions)

    return jsonify({"response": final_answer})



@app.route("/", methods=["GET"])
def health_check():
    return "✅ Flan-T5 is running."

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=7860)
