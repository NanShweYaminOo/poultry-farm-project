/* ==========================================================================
   Broiler Farming System — Admin Frontend
   Dashboard overview (dashboard.jsp): live stat tiles, recent posts feed,
   and a "needs attention" panel built from pending feedback tickets +
   flagged users. There is no admin-wide "list every batch" endpoint yet, so
   the Active Batches tile intentionally stays a static "—" rather than
   faking a number -- add a lightweight admin batch-count endpoint if that's
   wanted later.
   ========================================================================== */

(function () {
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

  function timeAgo(iso) {
    if (!iso) return '';
    var then = new Date(iso).getTime();
    if (isNaN(then)) return '';
    var minutes = Math.max(0, Math.round((Date.now() - then) / 60000));
    if (minutes < 60) {
      return lang() === 'en' ? minutes + ' min ago' : minutes + ' မိနစ်အရင်';
    }
    var hours = Math.round(minutes / 60);
    if (hours < 24) {
      return lang() === 'en' ? hours + ' hr ago' : hours + ' နာရီအရင်';
    }
    var days = Math.round(hours / 24);
    return lang() === 'en' ? days + ' day(s) ago' : days + ' ရက်အရင်';
  }

  function setStat(id, value) {
    var el = document.getElementById(id);
    if (el) el.textContent = value;
  }

  function loadUserCount() {
    window.AdminUI.authFetch('/api/v1/admin/users?size=1')
      .then(function (response) { return response.ok ? response.json() : null; })
      .then(function (page) {
        setStat('statTotalUsers', page ? page.totalElements : '—');
      })
      .catch(function () { setStat('statTotalUsers', '—'); });
  }

  function loadPendingPaymentCount() {
    window.AdminUI.authFetch('/api/v1/admin/payment-transactions')
      .then(function (response) { return response.ok ? response.json() : []; })
      .then(function (transactions) {
        var pending = transactions.filter(function (t) { return t.status === 'PENDING'; }).length;
        setStat('statPendingPayments', pending);
      })
      .catch(function () { setStat('statPendingPayments', '—'); });
  }

  function loadPostsFeed() {
    Promise.all([
      window.AdminUI.authFetch('/api/v1/admin/sales-posts').then(function (r) { return r.ok ? r.json() : []; }),
      window.AdminUI.authFetch('/api/v1/admin/buy-requests').then(function (r) { return r.ok ? r.json() : []; })
    ]).then(function (results) {
      var salesPosts = results[0].map(function (p) { return Object.assign({ postType: 'sale' }, p); });
      var buyRequests = results[1].map(function (p) { return Object.assign({ postType: 'request' }, p); });
      var merged = salesPosts.concat(buyRequests).sort(function (a, b) {
        return new Date(b.createdDate) - new Date(a.createdDate);
      });

      setStat('statPostsRequests', merged.length);
      renderPostsFeed(merged.slice(0, 5));
    }).catch(function () {
      setStat('statPostsRequests', '—');
      var feed = document.getElementById('dashboardPostsFeed');
      if (feed) {
        feed.innerHTML = '<p class="text-center" style="padding:30px;color:var(--muted);">' +
          (lang() === 'en' ? 'Failed to load recent posts.' : 'မကြာသေးမီက ပို့စ်များ ရယူ၍မရပါ။') + '</p>';
      }
    });
  }

  function renderPostsFeed(items) {
    var feed = document.getElementById('dashboardPostsFeed');
    if (!feed) return;

    if (!items.length) {
      feed.innerHTML = '<p class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No posts yet.' : 'ပို့စ်များ မရှိသေးပါ။') + '</p>';
      return;
    }

    feed.innerHTML = items.map(function (p) {
      var typeBadge = p.postType === 'sale'
        ? '<span class="post-type-badge sale">' + (lang() === 'en' ? 'Sales Post' : 'ရောင်းရန်ပို့စ်') + '</span>'
        : '<span class="post-type-badge request">' + (lang() === 'en' ? 'Chat / Buy Request' : 'ဝယ်လို/စကားပြောရန်') + '</span>';
      var meta = p.postType === 'sale'
        ? (p.price != null ? '<span class="post-chip"><i class="bi bi-tag-fill"></i> ' + escapeHtml(p.price) + '</span>' : '')
        : '<span class="post-chip"><i class="bi bi-boxes"></i> ' + (lang() === 'en' ? 'Qty: ' : 'အရေအတွက်: ') + escapeHtml(p.quantity) + '</span>';
      var flags = '';
      if (p.creatorIsBanned) {
        flags += ' <span class="badge badge-danger-soft">' + (lang() === 'en' ? 'Banned' : 'ပိတ်ပင်') + '</span>';
      } else if (p.creatorIsFlaggedForReview) {
        flags += ' <span class="badge badge-gold">' + (lang() === 'en' ? 'Flagged' : 'အမှတ်အသား') + '</span>';
      }

      return (
        '<div class="post-card">' +
        '<div class="post-card-head">' +
        '<div class="post-avatar">' + escapeHtml(initials(p.creatorUsername)) + '</div>' +
        '<div class="post-user"><div class="name">' + escapeHtml(p.creatorUsername) + flags + '</div><div class="time">' + timeAgo(p.createdDate) + '</div></div>' +
        typeBadge +
        '</div>' +
        '<p class="post-title">' + escapeHtml(p.title) + '</p>' +
        '<div class="post-meta">' + meta + '</div>' +
        '</div>'
      );
    }).join('');
  }

  function loadAttentionPanel() {
    Promise.all([
      window.AdminUI.authFetch('/api/v1/admin/feedback-tickets').then(function (r) { return r.ok ? r.json() : []; }),
      window.AdminUI.authFetch('/api/v1/admin/users?isFlaggedForReview=true&size=5').then(function (r) { return r.ok ? r.json() : { content: [] }; })
    ]).then(function (results) {
      var pendingTickets = results[0].filter(function (t) { return t.status === 'PENDING'; }).slice(0, 3);
      var flaggedUsers = (results[1].content || []).slice(0, 5 - pendingTickets.length);
      renderAttentionPanel(pendingTickets, flaggedUsers);
    }).catch(function () {
      var panel = document.getElementById('dashboardAttentionPanel');
      if (panel) {
        panel.innerHTML = '<p class="text-center" style="padding:20px;color:var(--muted);">' +
          (lang() === 'en' ? 'Failed to load.' : 'ရယူ၍မရပါ။') + '</p>';
      }
    });
  }

  function renderAttentionPanel(tickets, flaggedUsers) {
    var panel = document.getElementById('dashboardAttentionPanel');
    if (!panel) return;

    var items = tickets.map(function (t) {
      return '<div class="d-flex align-items-start gap-3 pb-3" style="border-bottom:1px dashed var(--line);">' +
        '<span class="badge badge-gold"><i class="bi bi-life-preserver"></i></span>' +
        '<div class="flex-grow-1">' +
        '<div style="font-weight:700;font-size:13.5px;">' + escapeHtml(t.submittedByFullName) + '</div>' +
        '<p class="mb-1" style="font-size:12.5px;color:var(--muted);">&quot;' + escapeHtml(t.content) + '&quot;</p>' +
        '<span class="badge badge-muted">' + (lang() === 'en' ? 'Pending' : 'စောင့်ဆိုင်းဆဲ') + '</span>' +
        '</div></div>';
    }).concat(flaggedUsers.map(function (u) {
      return '<div class="d-flex align-items-start gap-3 pb-3" style="border-bottom:1px dashed var(--line);">' +
        '<span class="badge badge-danger-soft"><i class="bi bi-flag-fill"></i></span>' +
        '<div class="flex-grow-1">' +
        '<div style="font-weight:700;font-size:13.5px;">' + escapeHtml(u.fullName) + '</div>' +
        '<p class="mb-1" style="font-size:12.5px;color:var(--muted);">' +
        (lang() === 'en' ? 'Flagged for review' : 'ပြန်လည်စစ်ဆေးရန် အမှတ်အသားပြုထားသည်') + '</p>' +
        '<a class="badge badge-danger-soft" href="/admin/users" style="text-decoration:none;">' +
        (lang() === 'en' ? 'Needs Review' : 'ပြန်လည်စစ်ဆေးရန်') + '</a>' +
        '</div></div>';
    }));

    if (!items.length) {
      panel.innerHTML = '<p class="text-center" style="padding:20px;color:var(--muted);">' +
        (lang() === 'en' ? 'Nothing needs attention right now.' : 'လောလောဆယ် အာရုံစိုက်ရန် မလိုအပ်ပါ။') + '</p>';
      return;
    }

    panel.innerHTML = items.join('');
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadUserCount();
    loadPendingPaymentCount();
    loadPostsFeed();
    loadAttentionPanel();

    document.addEventListener('adminlangchange', function () {
      loadPostsFeed();
      loadAttentionPanel();
    });
  });
})();
