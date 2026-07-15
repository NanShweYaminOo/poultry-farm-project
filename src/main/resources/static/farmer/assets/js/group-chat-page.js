/* ==========================================================================
   Broiler Farming System — Farmer Dashboard
   Group Chat (group-chat.jsp) — PAID Farmer/Admin-only. There is exactly one
   group chat in the whole system (see GroupChatService's Javadoc on the
   backend), so this just resolves it via GET /api/v1/group-chats (which
   auto-provisions the room and auto-joins the caller) and loads its
   messages directly -- no room list, search, or create/invite UI. Refetches
   after every send rather than using a WebSocket (same pattern the old
   multi-room version used).
   ========================================================================== */

(function () {
  var sharedGroupId = null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text == null ? '' : String(text);
    return div.innerHTML;
  }

  function initials(text) {
    return text ? text.trim().slice(0, 2).toUpperCase() : '?';
  }

  function formatBubbleTime(iso) {
    if (!iso) return '';
    var d = new Date(iso);
    return isNaN(d.getTime()) ? '' : d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  function showUpgradeNotice() {
    var content = document.getElementById('groupChatContent');
    content.innerHTML = '<div class="card empty-state">' +
      '<i class="bi bi-lock-fill"></i>' +
      '<p>' + (lang() === 'en'
        ? 'Group Chat is available once you have an approved, active batch.'
        : 'Batch တစ်ခု အတည်ပြုပြီး လည်ပတ်နေမှသာ အဖွဲ့စကားပြောကို အသုံးပြုနိုင်ပါသည်။') + '</p>' +
      '</div>';
  }

  function renderBubble(message, currentUserId) {
    var isOwn = currentUserId != null && message.senderId === currentUserId;
    var rowClass = isOwn ? 'own' : 'other';
    var senderLabel = isOwn ? '' : '<div class="chat-bubble-sender">' + escapeHtml(message.senderUsername) + '</div>';
    return '<div class="chat-bubble-row ' + rowClass + '">' +
      senderLabel +
      '<div class="chat-bubble-wrap"><div class="chat-bubble">' + escapeHtml(message.content) + '</div></div>' +
      '<div class="chat-bubble-time">' + formatBubbleTime(message.sentAt) + '</div>' +
    '</div>';
  }

  function renderMessages(messages) {
    var session = window.DashboardUI.getSession();
    var currentUserId = session && session.user ? session.user.userId : null;

    var messagesEl = document.getElementById('chatThreadMessages');
    messagesEl.innerHTML = messages.length
      ? messages.map(function (message) { return renderBubble(message, currentUserId); }).join('')
      : '<div class="chat-list-empty i18n" data-my="မက်ဆေ့ချ်မရှိသေးပါ" data-en="No messages yet">No messages yet</div>';
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }

  function loadMessages() {
    return window.DashboardUI.authFetch('/api/v1/group-chats/' + sharedGroupId + '/messages')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load messages');
        return response.json();
      })
      .then(renderMessages)
      .catch(function () {
        window.DashboardUI.toast('မက်ဆေ့ချ်များ ရယူ၍မရပါ။', 'Failed to load messages.', 'bi-exclamation-triangle-fill');
      });
  }

  function loadSharedGroup() {
    return window.DashboardUI.authFetch('/api/v1/group-chats')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load group chat');
        return response.json();
      })
      .then(function (group) {
        sharedGroupId = group.id;
        document.getElementById('chatThreadAvatar').textContent = initials(group.groupName);
        document.getElementById('chatThreadName').textContent = group.groupName;
        document.getElementById('chatThreadSub').textContent =
          group.memberCount + (lang() === 'en' ? ' members' : ' ဝင်');
        return loadMessages();
      })
      .catch(function () {
        window.DashboardUI.toast('အဖွဲ့စကားပြော ရယူ၍မရပါ။', 'Failed to load group chat.', 'bi-exclamation-triangle-fill');
      });
  }

  function sendMessage(content) {
    var form = document.getElementById('chatComposerForm');
    var input = document.getElementById('chatComposerInput');
    var sendBtn = form.querySelector('.chat-composer-send');
    sendBtn.disabled = true;

    return window.DashboardUI.authFetch('/api/v1/group-chats/' + sharedGroupId + '/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content: content })
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Failed to send message.' : 'မက်ဆေ့ချ် ပို့၍မရပါ။');
          window.DashboardUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        input.value = '';
        loadMessages();
      })
      .catch(function () {
        window.DashboardUI.toast('မက်ဆေ့ချ် ပို့၍မရပါ။', 'Failed to send message.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        sendBtn.disabled = false;
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var session = window.DashboardUI && window.DashboardUI.getSession();
    if (!session || !session.user || (session.user.role !== 'PAID' && session.user.role !== 'ADMIN')) {
      showUpgradeNotice();
      return;
    }

    var composerForm = document.getElementById('chatComposerForm');
    var composerInput = document.getElementById('chatComposerInput');
    composerForm.addEventListener('submit', function (event) {
      event.preventDefault();
      var content = composerInput.value.trim();
      if (!content || sharedGroupId === null) return;
      sendMessage(content);
    });

    document.addEventListener('dashboardlangchange', function (e) {
      if (composerInput) {
        composerInput.placeholder = e.detail.lang === 'en'
          ? composerInput.getAttribute('data-en-ph')
          : composerInput.getAttribute('data-my-ph');
      }
    });

    loadSharedGroup();
  });
})();
