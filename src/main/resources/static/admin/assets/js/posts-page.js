/* ==========================================================================
   Broiler Farming System — Admin Frontend
   User Posts Management (posts.jsp)
   Feed of public sales posts + buy requests with moderation delete, backed
   by GET/DELETE /api/v1/admin/sales-posts and /api/v1/admin/buy-requests.
   ========================================================================== */

(function () {
  var posts = [];
  var deletedId = null;
  var deletedType = null;
  var currentFilter = 'all';
  var currentSearch = '';
  var deleteModalEl = document.getElementById('deleteModal');
  var deleteModal = deleteModalEl ? new bootstrap.Modal(deleteModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text == null ? '' : String(text);
    return div.innerHTML;
  }

  function timeAgo(iso) {
    if (!iso) return '';
    var then = new Date(iso).getTime();
    if (isNaN(then)) return '';
    var minutes = Math.max(0, Math.round((Date.now() - then) / 60000));
    if (minutes < 60) return lang() === 'en' ? minutes + ' min ago' : minutes + ' မိနစ်အရင်';
    var hours = Math.round(minutes / 60);
    if (hours < 24) return lang() === 'en' ? hours + ' hr ago' : hours + ' နာရီအရင်';
    var days = Math.round(hours / 24);
    return lang() === 'en' ? days + ' day(s) ago' : days + ' ရက်အရင်';
  }

  function typeBadge(type) {
    if (type === 'sale') {
      return '<span class="post-type-badge sale">' + (lang() === 'en' ? 'Sales Post' : 'ရောင်းရန်ပို့စ်') + '</span>';
    }
    return '<span class="post-type-badge request">' + (lang() === 'en' ? 'Chat / Buy Request' : 'ဝယ်လို/စကားပြောရန်') + '</span>';
  }

  function metaChip(post) {
    if (post.type === 'sale') {
      var price = post.price != null ? post.price : '-';
      return '<span class="post-chip"><i class="bi bi-tag-fill"></i> ' + escapeHtml(price) + '</span>';
    }
    return '<span class="post-chip"><i class="bi bi-boxes"></i> ' + (lang() === 'en' ? 'Qty: ' : 'အရေအတွက်: ') + escapeHtml(post.quantity) + '</span>';
  }

  function authorBadges(post) {
    var html = '';
    if (post.creatorIsBanned) {
      html += ' <span class="badge badge-danger-soft">' + (lang() === 'en' ? 'Banned' : 'ပိတ်ပင်') + '</span>';
    }
    if (post.creatorIsFlaggedForReview) {
      html += ' <span class="badge badge-gold">' + (lang() === 'en' ? 'Flagged' : 'အမှတ်အသား') + '</span>';
    }
    return html;
  }

  function loadPosts() {
    var feed = document.getElementById('postsFeed');
    feed.innerHTML = '<p class="text-center i18n" style="padding:30px;color:var(--muted);" data-my="တင်နေသည်..." data-en="Loading...">' +
      (lang() === 'en' ? 'Loading...' : 'တင်နေသည်...') + '</p>';

    Promise.all([
      window.AdminUI.authFetch('/api/v1/admin/sales-posts').then(function (r) { return r.ok ? r.json() : []; }),
      window.AdminUI.authFetch('/api/v1/admin/buy-requests').then(function (r) { return r.ok ? r.json() : []; })
    ]).then(function (results) {
      var salesPosts = results[0].map(function (p) {
        return {
          id: p.id, type: 'sale', name: p.creatorUsername,
          creatorIsBanned: p.creatorIsBanned, creatorIsFlaggedForReview: p.creatorIsFlaggedForReview,
          time: p.createdDate, title: p.title, content: p.description, price: p.price
        };
      });
      var buyRequests = results[1].map(function (p) {
        return {
          id: p.id, type: 'request', name: p.creatorUsername,
          creatorIsBanned: p.creatorIsBanned, creatorIsFlaggedForReview: p.creatorIsFlaggedForReview,
          time: p.createdDate, title: p.title, content: p.description, quantity: p.quantity
        };
      });
      posts = salesPosts.concat(buyRequests).sort(function (a, b) {
        return new Date(b.time) - new Date(a.time);
      });
      render();
    }).catch(function () {
      feed.innerHTML = '<p class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'Failed to load posts.' : 'ပို့စ်များ ရယူ၍မရပါ။') + '</p>';
    });
  }

  function render() {
    var feed = document.getElementById('postsFeed');
    var filtered = posts.filter(function (p) {
      var matchesFilter = currentFilter === 'all' || p.type === currentFilter;
      var haystack = (p.name + ' ' + p.title + ' ' + (p.content || '')).toLowerCase();
      var matchesSearch = haystack.indexOf(currentSearch.toLowerCase()) !== -1;
      return matchesFilter && matchesSearch;
    });

    document.getElementById('resultsCount').textContent =
      filtered.length + (lang() === 'en' ? ' result(s)' : ' ခု တွေ့ရှိသည်');

    if (filtered.length === 0) {
      feed.innerHTML =
        '<div class="empty-state">' +
        '<i class="bi bi-inbox"></i>' +
        '<h3>' + (lang() === 'en' ? 'No posts found' : 'ပို့စ်များ မတွေ့ရှိပါ') + '</h3>' +
        '<p>' + (lang() === 'en' ? 'Try a different filter or search term.' : 'အခြား ဇစ်ခွဲ သို့မဟုတ် ရှာဖွေမှုစကားလုံးဖြင့် ထပ်မံစမ်းကြည့်ပါ။') + '</p>' +
        '</div>';
      return;
    }

    feed.innerHTML = filtered.map(function (p) {
      return (
        '<article class="post-card" data-id="' + p.id + '" data-type="' + p.type + '">' +
        '<div class="post-card-head">' +
        '<div class="post-avatar">' + escapeHtml((p.name || '?').slice(0, 2).toUpperCase()) + '</div>' +
        '<div class="post-user"><div class="name">' + escapeHtml(p.name) + authorBadges(p) + '</div><div class="time">' + timeAgo(p.time) + '</div></div>' +
        typeBadge(p.type) +
        '</div>' +
        '<p class="post-title">' + escapeHtml(p.title) + '</p>' +
        '<p class="post-content">' + escapeHtml(p.content) + '</p>' +
        '<div class="post-meta">' + metaChip(p) + '</div>' +
        '<div class="post-card-footer">' +
        '<button class="btn btn-danger btn-delete" data-id="' + p.id + '" data-type="' + p.type + '"><i class="bi bi-trash-fill"></i> ' + (lang() === 'en' ? 'Delete Post' : 'အရေးယူ/ဖျက်သိမ်းရန်') + '</button>' +
        '</div>' +
        '</article>'
      );
    }).join('');
  }

  function deleteEndpointFor(type, id) {
    return type === 'sale'
      ? '/api/v1/admin/sales-posts/' + id
      : '/api/v1/admin/buy-requests/' + id;
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadPosts();

    document.getElementById('filterTabs').addEventListener('click', function (e) {
      var btn = e.target.closest('button[data-filter]');
      if (!btn) return;
      currentFilter = btn.getAttribute('data-filter');
      document.querySelectorAll('#filterTabs button').forEach(function (b) {
        b.classList.toggle('active', b === btn);
      });
      render();
    });

    document.getElementById('searchInput').addEventListener('input', function (e) {
      currentSearch = e.target.value;
      render();
    });

    document.getElementById('postsFeed').addEventListener('click', function (e) {
      var delBtn = e.target.closest('.btn-delete');
      if (delBtn && deleteModal) {
        deletedId = Number(delBtn.getAttribute('data-id'));
        deletedType = delBtn.getAttribute('data-type');
        deleteModal.show();
      }
    });

    document.getElementById('confirmDelete').addEventListener('click', function () {
      window.AdminUI.authFetch(deleteEndpointFor(deletedType, deletedId), { method: 'DELETE' })
        .then(function (response) {
          deleteModal.hide();
          if (!response.ok && response.status !== 404) {
            window.AdminUI.toast('ပို့စ်ကို ဖျက်၍မရပါ။', 'Failed to delete post.', 'bi-exclamation-triangle-fill');
            return;
          }
          window.AdminUI.toast('ပို့စ်ကို ဖျက်သိမ်းပြီးပါပြီ', 'Post deleted successfully', 'bi-trash-fill');
          loadPosts();
        })
        .catch(function () {
          deleteModal.hide();
          window.AdminUI.toast('ပို့စ်ကို ဖျက်၍မရပါ။', 'Failed to delete post.', 'bi-exclamation-triangle-fill');
        });
    });

    document.addEventListener('adminlangchange', function (e) {
      var searchInput = document.getElementById('searchInput');
      searchInput.placeholder = e.detail.lang === 'en'
        ? searchInput.getAttribute('data-en-ph')
        : searchInput.getAttribute('data-my-ph');
      render();
    });
  });
})();
