# About Promptfoo for LLM Testing and Evaluation

[Promptfoo docs](https://www.promptfoo.dev/docs/intro/)
[Github repo](https://github.com/promptfoo/promptfoo)

Promptfoo is an open-source framework for testing, evaluating, and improving LLM prompts and prompt templates.

It helps you systematically develop better prompts — whether you're using OpenAI, HuggingFace, Mistral, Claude, or any other LLM — by automating evaluation, scoring, and comparison.

Promptfoo is one of those “Swiss-army-knife” tools that’s small enough to drop into a side project yet powerful enough to sit in a production CI pipeline.
Here’s a quick map of where it shines beyond the simple prompt-tuning workflow we just fixed:

### Key Capabilities
✅ **Prompt Testing & Debugging**
Define test cases with expected outputs or assertions to ensure your prompts behave as intended.

✅ **Prompt Regression Testing**
Catch unintended changes when updating prompts or LLM models.

✅ **Prompt Version Comparison**
Easily compare multiple prompts, templates, or models side-by-side.

✅ **Automated Evaluation**
Run automated tests on your prompts with assertions like:

 - contains
 - icontains-any 
 - Regex 
 - jsonSchema 
 - javascript (custom eval)
- and more

✅ **Multi-provider Support**
Works with:
- OpenAI (ChatGPT, GPT-4o, GPT-4-turbo, etc.)
- HuggingFace Inference API (Mistral, Zephyr, LLaMA, etc.)
- Ollama
- Bedrock
- Azure OpenAI
- Google Gemini
- and more

✅ **Powerful CLI**
Run promptfoo eval, promptfoo view, promptfoo diff and more — integrate with CI/CD.

✅ **Visual UI
Promptfoo provides a built-in web UI (promptfoo view) for inspecting runs, seeing per-assertion results, and comparing prompts.

✅ **Prompt Templates**
Supports flexible template formats:
- raw (simple text templates with variables)
- messages (chat format, system/user/assistant)
- functionCall (for structured calling LLMs)
- Multi-prompt configs

✅ **Flexible Data Sources**
Run tests with:
- YAML configs
- CSV files
- Google Sheets
- JSON test sets
- 
✅ **Caching**
Smart caching reduces LLM call costs while iterating.

✅ **Scoring & Weighting**
Supports minScore, weighted assertions, and customizable thresholds.


# Typical Use Cases

- 🔹 Improving chatbot reliability
- 🔹 Testing summarization or extraction prompts
- 🔹 Comparing model performance on your tasks
- 🔹 Automating LLM app QA
- 🔹 Preventing regressions in production prompts

🔹 **LLM Prompt Development**
Iteratively improve prompts for reliability and performance.
Compare different versions of a prompt across models.

🔹 **Prompt Regression Testing**
- Ensure updates to prompts don't break behavior.
- Run automated prompt tests in CI pipelines (GitHub Actions, GitLab CI, etc.).

🔹**Model Evaluation**
- Compare different models (e.g. GPT-4 vs Mistral vs Claude) on the same test suite.
- Select the best model for your app.

🔹 **LLM Application Quality Assurance**
- Test chatbots, search agents, summarizers, data extractors, and more.
- Assert critical behavior (e.g. "must extract customer name", "must avoid hallucination", etc.).

🔹 **Prompt Experimentation**
- Rapidly prototype and A/B test different prompt strategies.
- Explore prompt engineering ideas.

🔹 **Cost-aware Evaluation**
- Track latency, token usage, and cost across providers.


| Scenario                                                       | What Promptfoo gives you                                                                                                                                                                                                                               | Typical metrics to plug in                                                             |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------- |
| **Retrieval-Augmented Generation (RAG)**                       | • Treat each test row as a *(query, ground-truth answer, ground-truth source docs)* triple<br>• Call your own *retriever → LLM* pipeline through a custom HTTP or JavaScript provider.<br>• Compare answers *and* citation links against ground truth. | *Accuracy / F1, Jensen-Shannon for citation overlap, faithfulness, ROUGE-L on answers* |
| **Multi-model bake-offs** (e.g. Zephyr vs. Mistral vs. GPT-4o) | • One `prompts:` block, many `providers:`.<br>• `promptfoo eval --diff` renders side-by-side tables; `--output html` gives a shareable report.                                                                                                         | *Pass-rate, average cost, token latency, custom “grade\_my\_code” scripts*             |
| **Regression guardrails in CI/CD**                             | • Keep a `*.yaml` suite in the repo.<br>• Run `promptfoo eval --format junit` inside GitHub Actions; fail the build if key assertions drop.                                                                                                            | *Per-prompt pass/fail, response JSON schema validation*                                |
| **Conversational agents & tool-use chains**                    | • Each step in the chain can be its own provider (call your router, the tool-executor, the final summariser).<br>• Chain-level assertions catch tool-calling regressions faster than E2E manual testing.                                               | *Exact-match on emitted function names, latency budget*                                |
| **Human-in-the-loop qualitative review**                       | • `promptfoo playground` opens the same prompts/tests in a browser; product or UX folks can up-vote/down-vote without touching code.                                                                                                                   | *“Judge” score aggregation; CSV export for annotation*                                 |

---


### Where do **you** want to take it next?

* Building a RAG benchmark with your own Confluence docs?
* Tracking hallucination rate as you swap out Zephyr for TinyLLaMA fallback?
* Automating nightly regression tests for a production Chatbot?

Let me know what you’re aiming for and we can sketch a concrete folder layout or sample YAML. 🚀



## Installation(global) and Setup



#### Export environment variables

Ve a tu HuggingFace Profile → Settings → Access Tokens
Crea un token con permiso read

**Install dotenv-cli**

- npm install -g dotenv-cli

In CML run
export HF_TOKEN=hf_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

**verifica que se ha exportado correctamente**
echo $HF_API_KEY_TOKEN


#### Install Promptfoo

**Opción 1: Install Globally**
- npm install -g promptfoo
- runs with -> promptfoo eval --config promptfoo.yaml

**Opción 2: Install Locally**
- npm install --save-dev promptfoo
- runs with -> promptfoo eval --config promptfoo.yaml
- For FULL INFORMATION run-> promptfoo eval --verbose -c .github/promptfoo.yaml

**Report View**

- run after tests-> promptfoo view

## 🚀 Promptfoo CI/CD Setup Guide

- **Create directory structure and files for Promptfoo CI/CD integration.**

.github/workflows/promptfoo.yaml      # GitHub Actions workflow
promptfoo.yaml                        # Promptfoo config (providers + tests)




## Workflow Setup

Example .github/workflows/promptfoo.yaml:

```
name: Run Promptfoo Tests

on: [push, pull_request]

jobs:
promptfoo:
runs-on: ubuntu-latest
steps:
- uses: actions/checkout@v3
- name: Setup Node
uses: actions/setup-node@v3
with:
node-version: 18
- name: Install Promptfoo
run: npm install -g promptfoo
- name: Run Promptfoo Tests
env:
HF_TOKEN: ${{ secrets.HF_TOKEN }}
run: promptfoo eval --config promptfoo_relevance_or_sanity_tests.yaml
```



## promptfoo.yaml Template

Simple promptfoo.yaml file to define providers and tests:
```
prompts:
  - id: 'Prompt #1'
    raw: |
      Use Markdown formatting where appropriate (headings, lists, bold, italics, links, code) when needed to the user query. Instructions End.
      User query: {{question}}

providers:
  - id: http
    label: Zephyr-7B
    config:
      url: "https://api-inference.huggingface.co/models/HuggingFaceH4/zephyr-7b-beta"
      method: POST
      headers:
        Content-Type: application/json
        Authorization: "Bearer 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"
      body:
        inputs: "{{prompt}}"
        options:
          wait_for_model: true  # Forces the API to wait until the model loads
      transformResponse: "json[0].generated_text"

  - id: http
    label: Mistral-7B
    config:
      url: "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.3"
      method: POST
      headers:
        Content-Type: application/json
        Authorization: "Bearer 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"
      body:
        inputs: "{{prompt}}"
        options:
          wait_for_model: true  # Forces the API to wait until the model loads
      transformResponse: "json[0].generated_text"


tests:
  - description: "Check AI explanation"
    vars:
      question: What is AI?
    assert:
      - type: contains
        value: "algorithms"
      - type: contains
        value: "learning"
      - type: contains
        value: "neural networks"
      - type: contains
        value: "data"

  - description: "Check benefits of a healthy diet"
    vars:
      question: What are 3 benefits of a healthy diet?
    assert:
      - type: contains
        value: "weight"
      - type: contains
        value: "energy"
      - type: contains
        value: "disease"

  - description: "Check personal benefits of a romantic relationship"
    vars:
      question: What are the personal benefits of having a romantic relationship?
    assert:
      - type: contains
        value: "companionship"
      - type: contains
        value: "emotional support"
      - type: contains
        value: "support"
      - type: contains
        value: "stress"
      - type: contains
        value: "well-being"

  - description: "Check the downsides of a romantic relationship"
    vars:
      question: What are the downsides of having a romantic relationship?
    assert:
      - type: contains
        value: "conflict"
      - type: contains
        value: "stress"
      - type: contains
        value: "jealousy"
      - type: contains
        value: "heartbreak"
      - type: contains
        value: "dependency"
      - type: contains
        value: "arguments"

```

**Use import test cases**

##### promptfoo_correctness_tests.yaml WITH PROMPT TEMPLATES

✅ **Relevance**  
✅ **Coverage / completeness**  
✅ **Key concept coverage tests** → implemented via `assert: contains` for expected keywords.

```
providers:
  - id: huggingface:text-generation:HuggingFaceH4/zephyr-7b-beta
    config:
      hf_token: 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

  - id: huggingface:text-generation:mistralai/Mistral-7B-Instruct-v0.3
    config:
      hf_token: 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

defaultTest:  # default prompt for all tests
  vars:
    template: |
      Instructions:  Use Markdown formatting where appropriate (headings, lists, bold, italics, links, code) when needed to the user query. Instructions End.
      User query: {{question}}
    question: "What is AI?"


tests:
  - vars:
      question: "What is AI?"
    assert:
      - type: contains
        value: [ "algorithms", "learning", "neural networks", "data" ]

  - vars:
      question: "What are 3 benefits of a healthy diet?"
    assert:
      - type: contains
        value: [ "weight", "energy", "disease" ]

```

**Main promptfoo.yaml**

```
providers:
  - id: huggingface:text-generation:HuggingFaceH4/zephyr-7b-beta
    config:
      hf_token: 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
      temperature: 0.7

  - id: huggingface:text-generation:mistralai/Mistral-7B-Instruct-v0.3
    config:
      temperature: 0.7


tests:
  - src: "src/test/promptfoo_tests/promptfoo_correctness_tests.yaml"
```

# How to Evaluate Promptfoo Tests

It is difficult to guess what exact words the model will output.

##### There are different strategies to evaluate the output of a model:

1. Use the `type: contains` assertion type to check if the response contains certain keywords or phrases, but that would try to match the specific words or sentecnce.
For open-ended questions, do NOT use hard "contains" on words like "neural networks", instead use "networks" or a more flexible approach.

2. Use the `type: containsAny` assertion type to check if the response contains any of a list of keywords or phrases, which is more flexible and allows for variations in wording.
```
assert:
- type: contains
  value: "neural networks"
- type: icontains-any
  value:
    - "deep learning"
    - "artificial neural networks"
    - "ANNs"
```
Then if the model mentions any of these → test will pass.

3. Use the `type: equals` assertion type to check if the response matches exactly what you expect, but this is less flexible and may fail if the model generates slightly different wording.
4. Use the `type: regex` assertion type to check if the response matches a specific pattern, which is more flexible than exact matching.

Assertions & metrics -> [Official docs](https://www.promptfoo.dev/docs/configuration/expected-outputs/)

5. Use "semantic" assertions (advanced — not built-in to Promptfoo v3 yet):
   Check: does the meaning of the answer cover the required concept?

👉 But this needs a semantic similarity model — not a simple "contains"