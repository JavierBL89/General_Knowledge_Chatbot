const form = document.getElementById('chat-form');
const input = document.getElementById('user-input');
const chatbox = document.getElementById('chatbox');

// listen for input events
form.addEventListener('submit', async (e) => {
  e.preventDefault();
  const message = input.value;
  displayMessage(message, 'user');
  input.value = '';

  try {
    const response = await fetch('http://localhost:8080/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message }),
    });

    const data = await response.json();
    displayMessage(data.reply, 'bot');
  } catch (err) {
    displayMessage('Error: Could not connect to server.', 'bot');
  }
});

// Function to display bot response in the chatbox
function displayMessage(message, sender) {
  const div = document.createElement('div');
  div.className = sender;
  div.textContent = message;
  chatbox.appendChild(div);
  chatbox.scrollTop = chatbox.scrollHeight;
}
