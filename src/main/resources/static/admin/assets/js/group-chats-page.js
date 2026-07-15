/* ==========================================================================
   Broiler Farming System — Admin Frontend
   Group Chat (group-chats.jsp) — moderation view of the single, system-wide
   group chat shared by Premium Farmers and Admins (see GroupChatService's
   Javadoc on the backend). GET /api/v1/admin/group-chats returns that one
   room's detail directly (auto-provisioning it if it doesn't exist yet) --
   no room list, search, or selection UI.
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

  function renderBubble(message, currentUserId) {
    var isOwn = currentUserId != null && message.senderId === currentUserId;
    var rowClass = isOwn ? 'own' : 'other';
    var senderLabel = isOwn ? '' : '<div class="chat-bubble-sender">' + escapeHtml(message.senderUsername) + '</div>';
    return '<div class="chat-bubble-row ' + rowClass + '">' +
      senderLabel +
      '<div class="chat-bubble-wrap">' +
        '<div class="chat-bubble">' + escapeHtml(message.content) + '</div>' +
        '<button class="chat-bubble-delete" title="Delete message" data-delete-message="' + message.id + '">' +
          '<i class="bi bi-trash3"></i>' +
        '</button>' +
      '</div>' +
      '<div class="chat-bubble-time">' + formatBubbleTime(message.sentAt) + '</div>' +
    '</div>';
  }

  function loadSharedGroup() {
    window.AdminUI.authFetch('/api/v1/admin/group-chats')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load group chat');
        return response.json();
      })
      .then(function (detail) {
        sharedGroupId = detail.id;

        document.getElementById('chatThreadAvatar').textContent = initials(detail.groupName);
        document.getElementById('chatThreadName').textContent = detail.groupName;
        document.getElementById('chatThreadSub').textContent = detail.members.length + ' members';

        document.getElementById('chatMembersStrip').innerHTML = detail.members.map(function (member) {
          return '<span class="badge">' + escapeHtml(member.fullName || member.username) + '</span>';
        }).join('');

        var session = window.AdminUI.getSession();
        var currentUserId = session && session.user ? session.user.userId : null;

        var messagesEl = document.getElementById('chatThreadMessages');
        messagesEl.innerHTML = detail.messages.length
          ? detail.messages.map(function (message) { return renderBubble(message, currentUserId); }).join('')
          : '<div class="chat-list-empty i18n" data-my="မက်ဆေ့ချ်မရှိသေးပါ" data-en="No messages yet">No messages yet</div>';
        messagesEl.scrollTop = messagesEl.scrollHeight;

        messagesEl.querySelectorAll('[data-delete-message]').forEach(function (btn) {
          btn.addEventListener('click', function () {
            deleteMessage(Number(btn.getAttribute('data-delete-message')));
          });
        });
      })
      .catch(function () {
        window.AdminUI.toast('အဖွဲ့စကားပြော ရယူ၍မရပါ။', 'Failed to load group chat.', 'bi-exclamation-triangle-fill');
      });
  }

  function sendMessage(content) {
    var form = document.getElementById('chatComposerForm');
    var input = document.getElementById('chatComposerInput');
    var sendBtn = form.querySelector('.chat-composer-send');
    sendBtn.disabled = true;

    return window.AdminUI.authFetch('/api/v1/group-chats/' + sharedGroupId + '/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content: content })
    })
      .then(function (response) {
        if (!response.ok) {
          window.AdminUI.toast('မက်ဆေ့ချ် ပို့၍မရပါ။', 'Failed to send message.', 'bi-exclamation-triangle-fill');
          return;
        }
        input.value = '';
        loadSharedGroup();
      })
      .catch(function () {
        window.AdminUI.toast('မက်ဆေ့ချ် ပို့၍မရပါ။', 'Failed to send message.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        sendBtn.disabled = false;
      });
  }

  function deleteMessage(messageId) {
    window.AdminUI.authFetch('/api/v1/admin/group-chats/messages/' + messageId, { method: 'DELETE' })
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to delete message');
        window.AdminUI.toast('မက်ဆေ့ချ် ဖျက်ပြီးပါပြီ။', 'Message deleted.', 'bi-check-circle-fill');
        loadSharedGroup();
      })
      .catch(function () {
        window.AdminUI.toast('မက်ဆေ့ချ် ဖျက်၍မရပါ။', 'Failed to delete message.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var composerForm = document.getElementById('chatComposerForm');
    var composerInput = document.getElementById('chatComposerInput');
    if (composerForm) {
      composerForm.addEventListener('submit', function (event) {
        event.preventDefault();
        var content = composerInput.value.trim();
        if (!content || sharedGroupId === null) return;
        sendMessage(content);
      });
    }

    document.addEventListener('adminlangchange', function (e) {
      if (composerInput) {
        composerInput.placeholder = e.detail.lang === 'en'
          ? composerInput.getAttribute('data-en-ph')
          : composerInput.getAttribute('data-my-ph');
      }
    });

    loadSharedGroup();
  });
})();
