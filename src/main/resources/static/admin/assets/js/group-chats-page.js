(function () {
  var groups = [];
  var currentSearch = '';
  var activeGroupId = null;

  function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text == null ? '' : String(text);
    return div.innerHTML;
  }

  function initials(text) {
    return text ? text.trim().slice(0, 2).toUpperCase() : '?';
  }

  function formatListTime(iso) {
    if (!iso) return '';
    var d = new Date(iso);
    if (isNaN(d.getTime())) return '';
    var now = new Date();
    var sameDay = d.toDateString() === now.toDateString();
    return sameDay
      ? d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      : d.toLocaleDateString();
  }

  function formatBubbleTime(iso) {
    if (!iso) return '';
    var d = new Date(iso);
    return isNaN(d.getTime()) ? '' : d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  function matchesSearch(group) {
    if (!currentSearch) return true;
    return group.groupName.toLowerCase().indexOf(currentSearch) !== -1;
  }

  function renderList() {
    var listEl = document.getElementById('groupChatList');
    var visible = groups.filter(matchesSearch);

    if (!visible.length) {
      listEl.innerHTML = '<div class="chat-list-empty i18n" data-my="အဖွဲ့စကားပြော မတွေ့ပါ" data-en="No group chats found">No group chats found</div>';
      return;
    }

    listEl.innerHTML = visible.map(function (group) {
      var preview = group.lastMessagePreview
        ? escapeHtml(group.lastMessagePreview)
        : '<span class="i18n" data-my="မက်ဆေ့ချ်မရှိသေးပါ" data-en="No messages yet">No messages yet</span>';
      return '<div class="chat-list-item' + (group.id === activeGroupId ? ' active' : '') + '" data-group-id="' + group.id + '">' +
        '<div class="avatar">' + escapeHtml(initials(group.groupName)) + '</div>' +
        '<div class="meta">' +
          '<div class="top-row">' +
            '<span class="name">' + escapeHtml(group.groupName) + '</span>' +
            '<span class="time">' + formatListTime(group.lastMessageAt || group.createdDate) + '</span>' +
          '</div>' +
          '<div class="preview">' + preview + '</div>' +
          '<div class="stats">' + group.memberCount + ' <span class="i18n" data-my="ဝင်" data-en="members">members</span> &middot; ' + group.messageCount + ' <span class="i18n" data-my="မက်ဆေ့ချ်" data-en="msgs">msgs</span></div>' +
        '</div>' +
      '</div>';
    }).join('');

    listEl.querySelectorAll('[data-group-id]').forEach(function (item) {
      item.addEventListener('click', function () {
        selectGroup(Number(item.getAttribute('data-group-id')));
      });
    });
  }

  function loadGroups(preserveActive) {
    window.AdminUI.authFetch('/api/v1/admin/group-chats')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load group chats');
        return response.json();
      })
      .then(function (data) {
        groups = data;
        renderList();
        if (!preserveActive && activeGroupId === null && groups.length) {
          selectGroup(groups[0].id);
        }
      })
      .catch(function () {
        window.AdminUI.toast('အဖွဲ့စကားပြောများ ရယူ၍မရပါ။', 'Failed to load group chats.', 'bi-exclamation-triangle-fill');
      });
  }

  function renderBubble(groupChatId, message, currentUserId) {
    var isOwn = currentUserId != null && message.senderId === currentUserId;
    var rowClass = isOwn ? 'own' : 'other';
    var senderLabel = isOwn ? '' : '<div class="chat-bubble-sender">' + escapeHtml(message.senderUsername) + '</div>';
    return '<div class="chat-bubble-row ' + rowClass + '">' +
      senderLabel +
      '<div class="chat-bubble-wrap">' +
        '<div class="chat-bubble">' + escapeHtml(message.content) + '</div>' +
        '<button class="chat-bubble-delete" title="Delete message" data-delete-message="' + message.id + '" data-group-id="' + groupChatId + '">' +
          '<i class="bi bi-trash3"></i>' +
        '</button>' +
      '</div>' +
      '<div class="chat-bubble-time">' + formatBubbleTime(message.sentAt) + '</div>' +
    '</div>';
  }

  function selectGroup(groupChatId) {
    activeGroupId = groupChatId;
    renderList();

    var shell = document.getElementById('chatShell');
    shell.classList.add('thread-open');

    window.AdminUI.authFetch('/api/v1/admin/group-chats/' + groupChatId)
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load group chat detail');
        return response.json();
      })
      .then(function (detail) {
        document.getElementById('chatThreadEmpty').classList.add('d-none');
        document.getElementById('chatThreadActive').classList.remove('d-none');

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
          ? detail.messages.map(function (message) { return renderBubble(groupChatId, message, currentUserId); }).join('')
          : '<div class="chat-list-empty i18n" data-my="မက်ဆေ့ချ်မရှိသေးပါ" data-en="No messages yet">No messages yet</div>';
        messagesEl.scrollTop = messagesEl.scrollHeight;

        messagesEl.querySelectorAll('[data-delete-message]').forEach(function (btn) {
          btn.addEventListener('click', function () {
            deleteMessage(Number(btn.getAttribute('data-group-id')), Number(btn.getAttribute('data-delete-message')));
          });
        });
      })
      .catch(function () {
        window.AdminUI.toast('အသေးစိတ်အချက်အလက် ရယူ၍မရပါ။', 'Failed to load group chat details.', 'bi-exclamation-triangle-fill');
      });
  }

  function sendMessage(groupChatId, content) {
    var form = document.getElementById('chatComposerForm');
    var input = document.getElementById('chatComposerInput');
    var sendBtn = form.querySelector('.chat-composer-send');
    sendBtn.disabled = true;

    return window.AdminUI.authFetch('/api/v1/group-chats/' + groupChatId + '/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content: content })
    })
      .then(function (response) {
        if (!response.ok) {
          if (response.status === 403) {
            window.AdminUI.toast(
              'ဤအဖွဲ့တွင် အဖွဲ့ဝင်ဖြစ်မှသာ မက်ဆေ့ချ်ပို့နိုင်ပါသည်။',
              'You must be a member of this group to send messages.',
              'bi-exclamation-triangle-fill');
          } else {
            window.AdminUI.toast('မက်ဆေ့ချ် ပို့၍မရပါ။', 'Failed to send message.', 'bi-exclamation-triangle-fill');
          }
          return;
        }
        input.value = '';
        selectGroup(groupChatId);
        loadGroups(true);
      })
      .catch(function () {
        window.AdminUI.toast('မက်ဆေ့ချ် ပို့၍မရပါ။', 'Failed to send message.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        sendBtn.disabled = false;
      });
  }

  function deleteMessage(groupChatId, messageId) {
    window.AdminUI.authFetch('/api/v1/admin/group-chats/' + groupChatId + '/messages/' + messageId, { method: 'DELETE' })
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to delete message');
        window.AdminUI.toast('မက်ဆေ့ချ် ဖျက်ပြီးပါပြီ။', 'Message deleted.', 'bi-check-circle-fill');
        selectGroup(groupChatId);
        loadGroups(true);
      })
      .catch(function () {
        window.AdminUI.toast('မက်ဆေ့ချ် ဖျက်၍မရပါ။', 'Failed to delete message.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var searchInput = document.getElementById('groupChatSearchInput');
    if (searchInput) {
      searchInput.addEventListener('input', function () {
        currentSearch = searchInput.value.trim().toLowerCase();
        renderList();
      });
    }

    var backBtn = document.getElementById('chatBackBtn');
    if (backBtn) {
      backBtn.addEventListener('click', function () {
        document.getElementById('chatShell').classList.remove('thread-open');
      });
    }

    var composerForm = document.getElementById('chatComposerForm');
    var composerInput = document.getElementById('chatComposerInput');
    if (composerForm) {
      composerForm.addEventListener('submit', function (event) {
        event.preventDefault();
        var content = composerInput.value.trim();
        if (!content || activeGroupId === null) return;
        sendMessage(activeGroupId, content);
      });
    }

    document.addEventListener('adminlangchange', function (e) {
      [searchInput, composerInput].forEach(function (input) {
        if (!input) return;
        input.placeholder = e.detail.lang === 'en' ? input.getAttribute('data-en-ph') : input.getAttribute('data-my-ph');
      });
    });

    loadGroups(false);
  });
})();
