let timeoutMessageShown = false;
let timeoutMessageDiv = null;
let timeoutHandle = null;

const form = document.getElementById('chat-form');
const input = document.getElementById('user-input');
const chatbox = document.getElementById('chatbox');

// ✅ Use markdown-it with proper configuration
const md = window.markdownit({
  breaks: true,
  html: false,
  linkify: true,
  typographer: true,
});

window.addEventListener('DOMContentLoaded', async () => {
  typeBotResponse("👋 Hey! How can I help you today?\n\nE.g, try asking:\n➡️ *List 3 benefits of meditation*", "bot");
  loadEvaluationReport();
});

function createBotBubbleWithSpinner() {
  const div = document.createElement('div');
  div.className = 'bot';

  const span = document.createElement('span');
  span.id = 'bot-reply-content';
  span.style.display = 'inline-block';

  const spinner = document.createElement('div');
  spinner.className = 'spinner';
  spinner.id = 'loading-spinner';

  div.appendChild(spinner);
  div.appendChild(span);
  chatbox.appendChild(div);
  chatbox.scrollTop = chatbox.scrollHeight;
}

function removeLoading() {
  const spinner = document.getElementById('loading-spinner');
  if (spinner) spinner.remove();
}

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  const message = input.value;
  displayMessage(message, 'user');
  input.value = '';

  createBotBubbleWithSpinner();

  const timeOutMessage = setTimeout(() => {
    if (!timeoutMessageShown) {
      timeoutMessageDiv = document.createElement('div');
      timeoutMessageDiv.className = 'bot';
      timeoutMessageDiv.id = 'timeout-hint';
      timeoutMessageDiv.textContent = "⏳ This is taking longer... Zephyr7b server might be busy.";
      chatbox.appendChild(timeoutMessageDiv);
      chatbox.scrollTop = chatbox.scrollHeight;
      timeoutMessageShown = true;
    }
  }, 5000);

  try {
    const response = await fetch('/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message }),
    });

    if (!response.ok) throw new Error(await response.text());

    const data = await response.json();
    clearTimeout(timeOutMessage);
    removeLoading();

    if (timeoutMessageDiv) {
      timeoutMessageDiv.remove();
      timeoutMessageDiv = null;
      timeoutMessageShown = false;
    }

    typeBotResponse(data.reply, 'bot');
    if (data.evalGenerated) showReportButton();

  } catch (err) {
    clearTimeout(timeOutMessage);
    if (timeoutMessageDiv) {
      timeoutMessageDiv.remove();
      timeoutMessageDiv = null;
      timeoutMessageShown = false;
    }
    removeLoading();
    console.error("❌ Error talking to server:", err);
    typeBotResponse('Ey sorry, I could not connect to server\n Please try once again!', 'bot');
  }
});


function typeBotResponse(message, sender) {
  const content = document.createElement('div');
  content.className = sender;
  chatbox.appendChild(content);

  if (timeoutMessageShown) {
    removeLoading();
    timeoutMessageDiv?.remove();
    timeoutMessageDiv = null;
    timeoutMessageShown = false;
  }

  if (!message) {
    content.textContent = "⚠️ No response received.";
    return;
  }

  // ✅ Enhanced Markdown cleaning with farewell handling
  let cleaned = message
     .replace(/\\n/g, '\n')  // Unescape newlines
       .replace(/\*\*\n(\d+\.)/g, '**$1')  // Fix broken bold list items
       .replace(/\n{3,}/g, '\n\n');  // Normalize excessive newlines
  // 2. Protect properly formatted bold list items
  cleaned = cleaned.replace(/^\*\*(\d+\. .+?)\*\*$/gm, '{{PROTECTED}}$1{{/PROTECTED}}');
  // 3. Remove unwanted section headers
  cleaned = cleaned.replace(/^\*\*(Introduction|Conclusion|Farewell):?\*\*$/gim, '');
  // 4. Handle farewell with spacing
  cleaned = cleaned.replace(/(.*)(Feel free to ask.*$)/i, '$1\n\n$2'); // Add two line breaks before farewell);  // 5. Restore protected items and final cleanup
  cleaned = cleaned
    .replace(/\{\{PROTECTED\}\}(.+?)\{\{\/PROTECTED\}\}/g, '**$1**')
    .trim();

  // Split into chunks while preserving list structure
  const rawChunks = cleaned.split(/\n{2,}(?!\s*\*\*\d+\.)/);

  let index = 0;
  const typeChunk = () => {
    if (index < rawChunks.length) {
      const chunk = md.render(rawChunks[index]);
      content.innerHTML += chunk;
      chatbox.scrollTop = chatbox.scrollHeight;
      index++;
      setTimeout(typeChunk, 500);
    }
  };

  typeChunk();
  chatbox.scrollTop = chatbox.scrollHeight;
}

function displayMessage(message, sender) {
  const div = document.createElement('div');
  div.className = sender;
  chatbox.appendChild(div);
  div.textContent = message;
}

function showReportButton() {
  const reportDiv = document.createElement('div');
  reportDiv.className = 'bot';
  reportDiv.style.textAlign = 'center';
  reportDiv.innerHTML = `
    <button onclick="showEvalReportModal()" style="padding: 8px 16px; margin-top: 10px; font-size: 14px; cursor: pointer, margin-bottom: 20px;">
      📊 View Evaluation Report
    </button>
  `;
  chatbox.appendChild(reportDiv);
  chatbox.scrollTop = chatbox.scrollHeight;
}

function showEvalReportModal() {
  if (!dynamicReport) {
    alert("Report not loaded yet.");
    return;
  }

  const { prompt, output, assertions } = dynamicReport;
  document.getElementById('modalPrompt').textContent = prompt;
  document.getElementById('modalOutputs').textContent = output;

  const assertionsList = document.getElementById('modalAssertions');
  assertionsList.innerHTML = '';

  for (const [icon, score, type, question, reason] of assertions) {
    if (icon === '_promptfooFileMetadata') continue;

    const li = document.createElement('li');
    li.innerHTML = `
      <span><strong style="color: black;">Model-based Eval -</strong> <span style="color: #007BFF;">${question}</span></span><br>
      <span>${icon} <em>${reason}</em></span>
    `;
    assertionsList.appendChild(li);
  }

  document.getElementById('reportModal').style.display = 'block';
}

async function loadEvaluationReport() {
  try {
    const res = await fetch('/api/report-data');
    if (!res.ok) throw new Error("Report not found");
    dynamicReport = await res.json();
    console.log("✅ Evaluation report loaded");
  } catch (e) {
    console.warn("⚠️ No evaluation report found or failed to load.");
  }
}
