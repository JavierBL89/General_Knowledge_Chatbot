# 🤖 General Knowledge Chatbot (Kotlin + JavaScript)

[![Promptfoo Evals](https://github.com/your-username/your-repo/actions/workflows/llm-eval.yml/badge.svg)](https://github.com/your-username/your-repo/actions/workflows/llm-eval.yml)

This project is a simple and interactive **general knowledge chatbot** built using:

- 🧠 **Hugging Face Inference API** for intelligent responses.
- 💻 **Kotlin (Ktor)** as the backend server.
- 🌐 **HTML + JavaScript** frontend for desktop and mobile use.

---

## 🗂️ Sections

- [🛠 The App](#-the-app)
- [📊 Promptfoo Evaluations](#-promptfoo-evaluations)

---

## 🛠 The App

### 💡 Purpose

- Learn how to use **Kotlin** and set up a backend server.
- Explore API integration with **Hugging Face**.
- Build a simple chatbot UI using **HTML + JavaScript**.
- Deploy a lightweight, cross-platform chatbot app.

### ⚙️ Features

- User input field with chat history.
- Backend sends messages to Hugging Face models.
- Responses are dynamically shown in the UI.

### 🧰 Technologies

- Kotlin + Ktor (backend)
- JavaScript + HTML + CSS (frontend)
- Hugging Face Inference API (AI responses)

---

## 📊 Promptfoo Evaluations

This project includes automated LLM evaluations using **Promptfoo**, covering:

- **Sanity & relevance checks**
- **Semantic similarity scoring**
- **ROUGE-N overlap**
- **Latency measurements**

### 🧪 Evaluation Files

All evaluation configs are stored in `.github/workflows/`:

- `promptfoo_mixed_tests.yaml`
- `promptfoo_relevance_or_sanity_tests.yaml`
- `promptfoo_ROUGE_tests.yaml`
- `promptfoo_semantic_scoring_tests.yaml`

### 🚀 CI Integration

Every PR or push triggers `llm-eval.yml` to ensure that responses remain:

- Relevant ✅
- Well-structured 📋
- Fast ⏱
- Non-regressive 📉

---

