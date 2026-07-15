/* ==========================================================================
   Broiler Farming System — Admin Frontend
   User Management & Flag Review (users.jsp), backed by
   GET/POST /api/v1/admin/users/**.
   Loads a large single page (?size=200) since this UI has no pagination
   controls -- fine at this project's scale; add real pager controls if the
   user base grows past that.
   ========================================================================== */

(function () {
  var PAGE_SIZE = 200;
  var users = [];
  var currentFilter = 'all';
  var currentSearch = '';
  var reviewUserId = null;
  var reviewModalEl = document.getElementById('reviewModal');
  var reviewModal = reviewModalEl ? new bootstrap.Modal(reviewModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text == null ? '' : String(text);
    return div.innerHTML;
  }

  function formatDate(iso) {
    if (!iso) return '-';
    var d = new Date(iso);
    return isNaN(d.getTime()) ? '-' : d.toLocaleDateString();
  }

  function roleBadge(role) {
    if (role === 'ADMIN') return '<span class="badge badge-gold">ADMIN</span>';
    if (role === 'PAID') return '<span class="badge badge-emerald">PAID</span>';
    return '<span class="badge badge-muted">FREE</span>';
  }

  function statusBadges(u) {
    var html = u.isBanned
      ? '<span class="badge badge-danger-soft"><i class="bi bi-slash-circle-fill"></i> ' + (lang() === 'en' ? 'Banned' : 'ပိတ်ပင်ထားသည်') + '</span>'
      : '<span class="badge badge-emerald"><i class="bi bi-check-circle-fill"></i> ' + (lang() === 'en' ? 'Active' : 'အသက်ဝင်နေသည်') + '</span>';
    if (u.isFlaggedForReview) {
      html += ' <span class="badge badge-gold"><i class="bi bi-flag-fill"></i> ' + (lang() === 'en' ? 'Flagged' : 'အမှတ်အသား') + '</span>';
    }
    return html;
  }

  function actionButtons(u) {
    var btns = '';
    if (u.role !== 'ADMIN') {
      if (u.isFlaggedForReview) {
        btns += '<button class="btn btn-outline btn-sm btn-review" data-id="' + u.id + '"><i class="bi bi-search"></i> ' +
          (lang() === 'en' ? 'Review' : 'စစ်ဆေးရန်') + '</button> ';
      }
      btns += u.isBanned
        ? '<button class="btn btn-gold btn-sm btn-toggle-ban" data-id="' + u.id + '" data-banned="true"><i class="bi bi-unlock-fill"></i> ' + (lang() === 'en' ? 'Unban' : 'ပြန်ဖွင့်မည်') + '</button>'
        : '<button class="btn btn-danger btn-sm btn-toggle-ban" data-id="' + u.id + '" data-banned="false"><i class="bi bi-slash-circle"></i> ' + (lang() === 'en' ? 'Ban' : 'ပိတ်ပင်မည်') + '</button>';
    } else {
      btns = '<span style="color:var(--muted);font-size:12px;">' + (lang() === 'en' ? 'Admin account' : 'အက်ဒမင်အကောင့်') + '</span>';
    }
    return btns;
  }

  function loadUsers() {
    var body = document.getElementById('usersTableBody');
    body.innerHTML = '<tr><td colspan="6" class="text-center" style="padding:30px;color:var(--muted);">' +
      (lang() === 'en' ? 'Loading...' : 'တင်နေသည်...') + '</td></tr>';

    window.AdminUI.authFetch('/api/v1/admin/users?size=' + PAGE_SIZE)
      .then(function (response) { return response.ok ? response.json() : { content: [] }; })
      .then(function (page) {
        users = page.content || [];
        renderStats();
        render();
      })
      .catch(function () {
        body.innerHTML = '<tr><td colspan="6" class="text-center" style="padding:30px;color:var(--muted);">' +
          (lang() === 'en' ? 'Failed to load users.' : 'အသုံးပြုသူများ ရယူ၍မရပါ။') + '</td></tr>';
      });
  }

  function renderStats() {
    var setStat = function (id, value) {
      var el = document.getElementById(id);
      if (el) el.textContent = value;
    };
    setStat('statTotalUsersCount', users.length);
    setStat('statPaidUsersCount', users.filter(function (u) { return u.role === 'PAID'; }).length);
    setStat('statFlaggedUsersCount', users.filter(function (u) { return u.isFlaggedForReview; }).length);
    setStat('statBannedUsersCount', users.filter(function (u) { return u.isBanned; }).length);
  }

  function render() {
    var body = document.getElementById('usersTableBody');
    var filtered = users.filter(function (u) {
      var matchesFilter = currentFilter === 'all' ||
        (currentFilter === 'flagged' && u.isFlaggedForReview) ||
        (currentFilter === 'banned' && u.isBanned);
      var haystack = (u.fullName + ' ' + u.email).toLowerCase();
      var matchesSearch = haystack.indexOf(currentSearch.toLowerCase()) !== -1;
      return matchesFilter && matchesSearch;
    });

    document.getElementById('userResultsCount').textContent =
      filtered.length + (lang() === 'en' ? ' result(s)' : ' ဦး တွေ့ရှိသည်');

    if (filtered.length === 0) {
      body.innerHTML = '<tr><td colspan="6"><div class="empty-state"><i class="bi bi-people"></i><h3>' +
        (lang() === 'en' ? 'No users found' : 'အသုံးပြုသူများ မတွေ့ရှိပါ') + '</h3></div></td></tr>';
      return;
    }

    body.innerHTML = filtered.map(function (u) {
      var initials = (u.fullName || u.username || '?').charAt(0);
      return (
        '<tr data-id="' + u.id + '">' +
        '<td><div class="d-flex align-items-center gap-2">' +
        '<div class="post-avatar" style="width:34px;height:34px;font-size:12px;">' + escapeHtml(initials) + '</div>' +
        '<span style="font-weight:600;">' + escapeHtml(u.fullName || u.username) + '</span></div></td>' +
        '<td style="color:var(--muted);">' + escapeHtml(u.email) + '</td>' +
        '<td>' + roleBadge(u.role) + '</td>' +
        '<td>' + statusBadges(u) + '</td>' +
        '<td style="color:var(--muted);">' + formatDate(u.createdDate) + '</td>' +
        '<td class="text-end">' + actionButtons(u) + '</td>' +
        '</tr>'
      );
    }).join('');
  }

  function openReviewModal(userId) {
    reviewUserId = userId;
    var u = users.find(function (x) { return x.id === userId; });
    if (!u) return;

    document.getElementById('reviewUserName').textContent = u.fullName || u.username;
    document.getElementById('reviewContent').innerHTML = '<span style="color:var(--muted);">' +
      (lang() === 'en' ? 'Loading details...' : 'အသေးစိတ်အချက်အလက် တင်နေသည်...') + '</span>';
    reviewModal.show();

    window.AdminUI.authFetch('/api/v1/admin/users/' + userId)
      .then(function (response) { return response.ok ? response.json() : null; })
      .then(function (detail) {
        if (!detail || reviewUserId !== userId) return;
        var warningsHtml = detail.warnings && detail.warnings.length
          ? '<ul class="mb-0 ps-3">' + detail.warnings.map(function (w) {
              return '<li>' + escapeHtml(w.reason) + ' <span style="color:var(--muted);">(' + formatDate(w.createdAt) + ')</span></li>';
            }).join('') + '</ul>'
          : '<span style="color:var(--muted);">' + (lang() === 'en' ? 'No prior warnings.' : 'ယခင် သတိပေးချက် မရှိသေးပါ။') + '</span>';

        document.getElementById('reviewContent').innerHTML =
          '<div class="mb-2"><strong>' + (lang() === 'en' ? 'Batches: ' : 'ကြက်အုပ်စု: ') + '</strong>' + detail.batchCount +
          ' &middot; <strong>' + (lang() === 'en' ? 'Sales posts: ' : 'ရောင်းရန်ပို့စ်: ') + '</strong>' + detail.salesPostCount +
          ' &middot; <strong>' + (lang() === 'en' ? 'Buy requests: ' : 'ဝယ်လိုချက်: ') + '</strong>' + detail.buyRequestCount + '</div>' +
          '<div class="mb-2"><strong>' + (lang() === 'en' ? 'Payments: ' : 'ငွေပေးချေမှု: ') + '</strong>' +
          detail.paymentHistory.totalCount + ' (' + detail.paymentHistory.pendingCount + ' ' + (lang() === 'en' ? 'pending' : 'စောင့်ဆိုင်း') + ')</div>' +
          '<div><strong>' + (lang() === 'en' ? 'Warnings:' : 'သတိပေးချက်များ:') + '</strong> ' + warningsHtml + '</div>';
      })
      .catch(function () {
        document.getElementById('reviewContent').innerHTML = '<span style="color:var(--muted);">' +
          (lang() === 'en' ? 'Failed to load details.' : 'အသေးစိတ်အချက်အလက် ရယူ၍မရပါ။') + '</span>';
      });
  }

  function toggleBan(userId, isCurrentlyBanned) {
    var endpoint = '/api/v1/admin/users/' + userId + (isCurrentlyBanned ? '/unban' : '/ban');
    window.AdminUI.authFetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: isCurrentlyBanned ? undefined : JSON.stringify({})
    })
      .then(function (response) {
        if (!response.ok) {
          window.AdminUI.toast('လုပ်ဆောင်ချက် မအောင်မြင်ပါ။', 'Action failed.', 'bi-exclamation-triangle-fill');
          return;
        }
        window.AdminUI.toast(
          isCurrentlyBanned ? 'အသုံးပြုသူကို ပြန်ဖွင့်ပေးလိုက်ပါပြီ' : 'အသုံးပြုသူကို ပိတ်ပင်လိုက်ပါပြီ',
          isCurrentlyBanned ? 'User has been unbanned' : 'User has been banned',
          isCurrentlyBanned ? 'bi-unlock-fill' : 'bi-slash-circle-fill'
        );
        loadUsers();
      })
      .catch(function () {
        window.AdminUI.toast('လုပ်ဆောင်ချက် မအောင်မြင်ပါ။', 'Action failed.', 'bi-exclamation-triangle-fill');
      });
  }

  function dismissFlag(userId) {
    window.AdminUI.authFetch('/api/v1/admin/users/' + userId + '/dismiss-flag', { method: 'POST' })
      .then(function (response) {
        if (!response.ok) {
          window.AdminUI.toast('အမှတ်အသား ဖယ်ရှား၍မရပါ။', 'Failed to clear flag.', 'bi-exclamation-triangle-fill');
          return;
        }
        window.AdminUI.toast('အမှတ်အသားကို ဖယ်ရှားပြီးပါပြီ', 'Flag cleared', 'bi-check-circle-fill');
        reviewModal.hide();
        loadUsers();
      })
      .catch(function () {
        window.AdminUI.toast('အမှတ်အသား ဖယ်ရှား၍မရပါ။', 'Failed to clear flag.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadUsers();

    document.getElementById('userFilterTabs').addEventListener('click', function (e) {
      var btn = e.target.closest('button[data-filter]');
      if (!btn) return;
      currentFilter = btn.getAttribute('data-filter');
      document.querySelectorAll('#userFilterTabs button').forEach(function (b) {
        b.classList.toggle('active', b === btn);
      });
      render();
    });

    document.getElementById('userSearchInput').addEventListener('input', function (e) {
      currentSearch = e.target.value;
      render();
    });

    document.getElementById('usersTableBody').addEventListener('click', function (e) {
      var reviewBtn = e.target.closest('.btn-review');
      var banBtn = e.target.closest('.btn-toggle-ban');

      if (reviewBtn) {
        openReviewModal(Number(reviewBtn.getAttribute('data-id')));
      } else if (banBtn) {
        var id = Number(banBtn.getAttribute('data-id'));
        var isCurrentlyBanned = banBtn.getAttribute('data-banned') === 'true';
        toggleBan(id, isCurrentlyBanned);
      }
    });

    document.getElementById('clearFlagBtn').addEventListener('click', function () {
      if (reviewUserId !== null) dismissFlag(reviewUserId);
    });

    document.getElementById('banFromReviewBtn').addEventListener('click', function () {
      if (reviewUserId === null) return;
      reviewModal.hide();
      toggleBan(reviewUserId, false);
    });

    document.addEventListener('adminlangchange', function (e) {
      var input = document.getElementById('userSearchInput');
      input.placeholder = e.detail.lang === 'en' ? input.getAttribute('data-en-ph') : input.getAttribute('data-my-ph');
      render();
    });
  });
})();
