// Chat Popup JavaScript
let stompClient = null;
let currentUsername = null;
let isConnected = false;

function initChatPopup(username) {
    console.log('Initializing chat popup for user:', username);
    currentUsername = username;
    
    const chatToggleBtn = document.getElementById('chatToggleBtn');
    const chatBox = document.getElementById('chatPopupBox');
    
    if (!chatToggleBtn || !chatBox) {
        console.error('Chat elements not found!');
        return;
    }
    
    // Load unread count initially
    updateUnreadCount();
    
    // Update unread count every 10 seconds
    setInterval(updateUnreadCount, 10000);
    
    // Toggle chat popup
    chatToggleBtn.addEventListener('click', function() {
        console.log('Chat toggle clicked');
        chatBox.classList.toggle('active');
        
        if (chatBox.classList.contains('active')) {
            console.log('Chat box opened');
            if (!isConnected) {
                connectWebSocket();
            }
            loadChatHistory();
            markMessagesAsRead();
        }
    });
    
    // Close chat
    document.getElementById('chatCloseBtn').addEventListener('click', function() {
        document.getElementById('chatPopupBox').classList.remove('active');
    });
    
    // Send message
    document.getElementById('chatSendBtn').addEventListener('click', sendMessage);
    document.getElementById('chatMessageInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
}

function connectWebSocket() {
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        isConnected = true;
        
        // Subscribe to personal queue
        stompClient.subscribe('/queue/chat/' + currentUsername, function(message) {
            const chatMessage = JSON.parse(message.body);
            displayMessage(chatMessage);
            
            // Update unread count if chat is not open
            const chatBox = document.getElementById('chatPopupBox');
            if (!chatBox.classList.contains('active')) {
                updateUnreadCount();
            }
        });
    }, function(error) {
        console.error('WebSocket connection error:', error);
        isConnected = false;
        setTimeout(connectWebSocket, 5000); // Retry after 5 seconds
    });
}

function sendMessage() {
    const input = document.getElementById('chatMessageInput');
    const content = input.value.trim();
    
    if (content && stompClient && isConnected) {
        const message = {
            senderUsername: currentUsername,
            content: content
        };
        
        stompClient.send("/app/chat.sendToAdmin", {}, JSON.stringify(message));
        input.value = '';
    }
}

function displayMessage(message) {
    const messagesContainer = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    
    const isSent = message.senderUsername === currentUsername;
    messageDiv.className = 'chat-message ' + (isSent ? 'sent' : 'received');
    
    const time = new Date(message.timestamp).toLocaleTimeString('vi-VN', { 
        hour: '2-digit', 
        minute: '2-digit' 
    });
    
    messageDiv.innerHTML = `
        <div class="chat-message-content">
            ${escapeHtml(message.content)}
            <div class="chat-message-time">${time}</div>
        </div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function loadChatHistory() {
    fetch(`/api/chat/history?username1=${currentUsername}&username2=admin`)
        .then(response => response.json())
        .then(messages => {
            const messagesContainer = document.getElementById('chatMessages');
            messagesContainer.innerHTML = '';
            
            if (messages.length === 0) {
                messagesContainer.innerHTML = `
                    <div class="chat-empty-state">
                        <i class="fas fa-comments"></i>
                        <p>Chào mừng bạn đến với hỗ trợ khách hàng!<br>Hãy gửi tin nhắn để bắt đầu.</p>
                    </div>
                `;
            } else {
                messages.forEach(message => displayMessage(message));
            }
        })
        .catch(error => console.error('Error loading chat history:', error));
}

function markMessagesAsRead() {
    fetch(`/api/chat/mark-read?username=${currentUsername}`, {
        method: 'POST'
    })
    .then(() => updateUnreadCount())
    .catch(error => console.error('Error marking messages as read:', error));
}

function updateUnreadCount() {
    fetch(`/api/chat/unread-count?username=${currentUsername}`)
        .then(response => response.json())
        .then(data => {
            const badge = document.getElementById('chatUnreadBadge');
            if (badge) {
                if (data.unreadCount > 0) {
                    badge.textContent = data.unreadCount > 99 ? '99+' : data.unreadCount;
                    badge.style.display = 'flex';
                } else {
                    badge.style.display = 'none';
                }
            }
        })
        .catch(error => console.error('Error fetching unread count:', error));
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Disconnect on page unload
window.addEventListener('beforeunload', function() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
});
