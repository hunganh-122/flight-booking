$(document).ready(function() {
    const chatNavLink = $('#ai-chat-nav-link');
    const chatContainer = $('#ai-chat-container');
    const chatClose = $('#ai-chat-close');
    const chatInput = $('#ai-chat-input');
    const sendBtn = $('#ai-send-btn');
    const chatMessages = $('#ai-chat-messages');

    chatNavLink.on('click', function(e) {
        e.preventDefault();
        chatContainer.toggleClass('d-none');
        if (!chatContainer.hasClass('d-none')) {
            chatContainer.addClass('animate__fadeInUp');
        }
    });

    chatClose.on('click', function() {
        chatContainer.addClass('d-none');
    });

    function addMessage(text, isUser = false) {
        const msgClass = isUser ? 'user-message' : 'ai-message';
        const msgHtml = `<div class="${msgClass}">${text}</div>`;
        chatMessages.append(msgHtml);
        chatMessages.scrollTop(chatMessages[0].scrollHeight);
    }

    function sendMessage() {
        const message = chatInput.val().trim();
        if (!message) return;

        addMessage(message, true);
        chatInput.val('');

        const typingId = 'typing-' + Date.now();
        chatMessages.append(`<div id="${typingId}" class="ai-message typing-indicator">AI đang suy nghĩ...</div>`);

        $.ajax({
            url: '/api/ai/chat',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ message: message }),
            success: function(data) {
                $(`#${typingId}`).remove();
                addMessage(data.response);
            },
            error: function() {
                $(`#${typingId}`).remove();
                addMessage('Xin lỗi, tôi gặp lỗi kết nối. Vui lòng thử lại sau.');
            }
        });
    }

    sendBtn.on('click', sendMessage);
    chatInput.on('keypress', function(e) {
        if (e.which == 13) sendMessage();
    });
});
