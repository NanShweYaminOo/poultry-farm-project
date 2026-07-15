/* ==========================================================================
   Broiler Farming System — User Dashboard
   AI Chatbot (chatbot.jsp) — POST /api/v1/chatbot/diagnose, multipart with
   a required "message" text part and an optional "image" file part. The
   backend is single-shot (no server-side conversation history, no language
   flag -- the model detects Burmese vs English from the message itself), so
   the transcript here is purely client-side/in-memory and resets on reload.
   Entire path is PAID/ADMIN-only.
   ========================================================================== */

(function () {
  var pendingImageFile = null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function showUpgradeNotice() {
    var content = document.getElementById('chatbotContent');
    content.innerHTML = '<div class="card empty-state">' +
      '<i class="bi bi-lock-fill"></i>' +
      '<p>' + (lang() === 'en'
        ? 'The AI chatbot is available once you have an approved, active batch.'
        : 'Batch တစ်ခု အတည်ပြုပြီး လည်ပတ်နေမှသာ AI စကားပြောစနစ်ကို အသုံးပြုနိုင်ပါသည်။') + '</p>' +
      '</div>';
  }

  function scrollToBottom() {
    var messagesEl = document.getElementById('chatbotMessages');
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }

  function clearEmptyState() {
    var empty = document.querySelector('#chatbotMessages .chat-list-empty');
    if (empty) empty.remove();
  }

  function appendUserBubble(text, imageDataUrl) {
    clearEmptyState();
    var messagesEl = document.getElementById('chatbotMessages');
    var imageHtml = imageDataUrl
      ? '<img src="' + imageDataUrl + '" style="max-width:180px;border-radius:10px;display:block;margin-bottom:6px;">'
      : '';
    var row = document.createElement('div');
    row.className = 'chat-bubble-row own';
    row.innerHTML = '<div class="chat-bubble-wrap"><div class="chat-bubble">' + imageHtml + escapeHtml(text) + '</div></div>';
    messagesEl.appendChild(row);
    scrollToBottom();
  }

  function appendBotBubble(text) {
    clearEmptyState();
    var messagesEl = document.getElementById('chatbotMessages');
    var row = document.createElement('div');
    row.className = 'chat-bubble-row other';
    row.innerHTML = '<div class="chat-bubble-wrap"><div class="chat-bubble">' + escapeHtml(text) + '</div></div>';
    messagesEl.appendChild(row);
    scrollToBottom();
    return row;
  }

  function appendTypingIndicator() {
    clearEmptyState();
    var messagesEl = document.getElementById('chatbotMessages');
    var row = document.createElement('div');
    row.className = 'chat-bubble-row other';
    row.setAttribute('data-typing', 'true');
    row.innerHTML = '<div class="chat-bubble-wrap"><div class="chat-bubble">' +
      '<span class="spinner"></span> ' + (lang() === 'en' ? 'Thinking...' : 'တွေးနေသည်...') +
      '</div></div>';
    messagesEl.appendChild(row);
    scrollToBottom();
    return row;
  }

  function fileToDataUrl(file) {
    return new Promise(function (resolve) {
      var reader = new FileReader();
      reader.onload = function () { resolve(reader.result); };
      reader.onerror = function () { resolve(null); };
      reader.readAsDataURL(file);
    });
  }

  function showImagePreview(file) {
    pendingImageFile = file;
    fileToDataUrl(file).then(function (dataUrl) {
      document.getElementById('chatbotImagePreviewThumb').src = dataUrl || '';
      document.getElementById('chatbotImagePreviewName').textContent = file.name;
      document.getElementById('chatbotImagePreviewRow').style.display = 'flex';
    });
  }

  function clearImagePreview() {
    pendingImageFile = null;
    document.getElementById('chatbotImageInput').value = '';
    document.getElementById('chatbotImagePreviewRow').style.display = 'none';
  }

  function sendMessage(text) {
    var imageFile = pendingImageFile;
    var sendBtn = document.getElementById('chatbotSendBtn');

    var doAppendAndSend = function (imageDataUrl) {
      appendUserBubble(text, imageDataUrl);
      clearImagePreview();

      var typingRow = appendTypingIndicator();
      sendBtn.disabled = true;

      var formData = new FormData();
      formData.append('message', text);
      if (imageFile) formData.append('image', imageFile);

      window.DashboardUI.authFetch('/api/v1/chatbot/diagnose', { method: 'POST', body: formData })
        .then(function (response) {
          return response.json().catch(function () { return {}; }).then(function (respBody) {
            return { ok: response.ok, body: respBody };
          });
        })
        .then(function (result) {
          typingRow.remove();
          if (!result.ok) {
            var message = (result.body && result.body.message) ||
              (lang() === 'en' ? 'Could not reach the assistant. Please try again.' : 'အကူအညီစနစ်နှင့် ဆက်သွယ်၍မရပါ။ ထပ်မံကြိုးစားပါ။');
            appendBotBubble(message);
            return;
          }
          appendBotBubble(result.body.reply);
        })
        .catch(function () {
          typingRow.remove();
          appendBotBubble(lang() === 'en' ? 'Could not reach the assistant. Please try again.' : 'အကူအညီစနစ်နှင့် ဆက်သွယ်၍မရပါ။ ထပ်မံကြိုးစားပါ။');
        })
        .finally(function () {
          sendBtn.disabled = false;
        });
    };

    if (imageFile) {
      fileToDataUrl(imageFile).then(doAppendAndSend);
    } else {
      doAppendAndSend(null);
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    var session = window.DashboardUI && window.DashboardUI.getSession();
    if (!session || !session.user || (session.user.role !== 'PAID' && session.user.role !== 'ADMIN')) {
      showUpgradeNotice();
      return;
    }

    var form = document.getElementById('chatbotComposerForm');
    var input = document.getElementById('chatbotMessageInput');

    document.getElementById('chatbotAttachBtn').addEventListener('click', function () {
      document.getElementById('chatbotImageInput').click();
    });
    document.getElementById('chatbotImageInput').addEventListener('change', function (e) {
      if (e.target.files && e.target.files[0]) showImagePreview(e.target.files[0]);
    });
    document.getElementById('chatbotRemoveImageBtn').addEventListener('click', clearImagePreview);

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      var text = input.value.trim();
      if (!text && !pendingImageFile) return;
      // Backend requires a non-blank message even when an image is attached.
      var messageText = text || (lang() === 'en' ? 'Please assess this photo.' : 'ဤဓာတ်ပုံကို အကဲဖြတ်ပေးပါ။');
      sendMessage(messageText);
      input.value = '';
    });

    document.addEventListener('dashboardlangchange', function (e) {
      input.placeholder = e.detail.lang === 'en' ? input.getAttribute('data-en-ph') : input.getAttribute('data-my-ph');
    });
  });
})();
