const form = document.getElementById('chat-form');
const input = document.getElementById('user-input');
const chatbox = document.getElementById('chatbox');
let timeoutMessageShown = false;
let timeoutMessageDiv = null;
let timeoutHandle = null;  // ✅ Needed to clear the timeout later

window.addEventListener('DOMContentLoaded', () => {
  typeBotResponse("👋 Hey! How can I help you today?\n\nE.g, try asking:\n➡️ *List 3 benefits of meditation*", "bot");
});

// Function to create a bot bubble with a loading spinner
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

// Function to remove the loading spinner
function removeLoading() {
  const spinner = document.getElementById('loading-spinner');
  if (spinner) spinner.remove();
}

// listen for input events
form.addEventListener('submit', async (e) => {
  e.preventDefault();
  const message = input.value;
  displayMessage(message, 'user');
  input.value = '';

  createBotBubbleWithSpinner(); //start spinner

 //  Show a timeout message if the spinner is still there after 5 seconds
  const timeOutMessage= setTimeout (() => {
      if(!timeoutMessageShown){
          timeoutMessageDiv = document.createElement('div');
          timeoutMessageDiv.className = 'bot';
          timeoutMessageDiv.id = 'timeout-hint';
          timeoutMessageDiv.textContent = "⏳ This is taking longer... server might be busy.";
          chatbox.appendChild(timeoutMessageDiv);
          chatbox.scrollTop = chatbox.scrollHeight;
          timeoutMessageShown = true;
      }
  }, 5000); // 3 seconds


  try {
      const response = await fetch('/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message }),
      });

      if (!response.ok) {
          const text = await response.text(); // fallback
          throw new Error("Server error: " + text);
      }

      const data = await response.json();
      clearTimeout(timeOutMessage); // clear timeout message
      removeLoading(); //remover spinner

      // Check if the timeout message is shown and remove it
       if (timeoutMessageDiv) {
           timeoutMessageDiv.remove();
           timeoutMessageDiv = null;
           timeoutMessageShown = false; // Reset the flag
       }

      typeBotResponse(data.reply, 'bot');

  } catch (err) {
    clearTimeout(timeOutMessage);
    if (timeoutMessageDiv) {
      timeoutMessageDiv.remove();
      timeoutMessageDiv = null;
      timeoutMessageShown = false; // Reset the flag

    }
    removeLoading();
    console.error("❌ Error talking to server:", err);
    typeBotResponse('Ey sorry, I could not connect to server\n Please try once again!', 'bot');
  }
});



function typeBotResponse(message, sender) {
      const content = document.createElement('div');
      const spinner = document.getElementById('loading-spinner');


      // Remove Spinner && Display Timeout Message
      if (timeoutMessageShown && spinner){
          removeLoading();// remove spinner
          timeoutMessageDiv.remove(); // remove timeout message
          timeoutMessageDiv = null; // reset timeout message
          timeoutMessageShown = false; // Reset the flag
      }

       if (!message) {
          content.textContent = "⚠️ No response received.";
          return;
       }

      content.className = sender;
      chatbox.appendChild(content);

//      const markdownHTML = marked.parse(message).replace(/<[^>]+>/g, ''); // strip HTML tags

      // First, split message into natural segments
  const rawChunks = message.split(/(?<=[\n\!\?])\s+(?=[A-Z])/); // ends with . ! ? followed by capital
      let index = 0;

      const typeChunk = () => {
           if (index < rawChunks.length) {
               const chunk = marked.parse(rawChunks[index]); // ✅ Parse each segment
               content.innerHTML += chunk; // Append the chunk to the content
               chatbox.scrollTop = chatbox.scrollHeight;
               index++;
               setTimeout(typeChunk, 500); // Adjust delay per chunk
           }
        };

       typeChunk();
       chatbox.scrollTop = chatbox.scrollHeight;
}

/**
* Function to display bot response in the chatbox
* Typing effect for chatbot response
* Function to Support Line Breaks
*/
function displayMessage(message, sender) {
  const div = document.createElement('div');
    div.className = sender;
    chatbox.appendChild(div);

    div.textContent = message;
}
