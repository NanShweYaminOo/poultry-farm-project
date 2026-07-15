/* ==========================================================================
   Broiler Farming System — User Dashboard
   Marketplace (marketplace.jsp) — browse + create sales posts
   (/api/v1/sales-posts) and buy requests (/api/v1/buy-requests, restricted
   to FARMER/ADMIN accounts server-side).
   ========================================================================== */

(function () {
  var salesPosts = [];
  var buyRequests = [];

  var salesPostModalEl = document.getElementById('salesPostModal');
  var salesPostModal = salesPostModalEl ? new bootstrap.Modal(salesPostModalEl) : null;
  var buyRequestModalEl = document.getElementById('buyRequestModal');
  var buyRequestModal = buyRequestModalEl ? new bootstrap.Modal(buyRequestModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function initials(name) {
    return name ? name.trim().slice(0, 2).toUpperCase() : '?';
  }

  function timeAgo(dateString) {
    if (!dateString) return '-';
    var diffMs = Date.now() - new Date(dateString).getTime();
    var mins = Math.floor(diffMs / 60000);
    if (mins < 1) return lang() === 'en' ? 'just now' : 'ခုနက';
    if (mins < 60) return mins + (lang() === 'en' ? ' min ago' : ' မိနစ်အရင်');
    var hours = Math.floor(mins / 60);
    if (hours < 24) return hours + (lang() === 'en' ? ' hr ago' : ' နာရီအရင်');
    var days = Math.floor(hours / 24);
    return days + (lang() === 'en' ? ' day(s) ago' : ' ရက်အရင်');
  }

  function renderSalesPosts() {
    var grid = document.getElementById('salesPostsGrid');

    if (salesPosts.length === 0) {
      grid.innerHTML = '<div class="col-12 empty-state">' +
        '<i class="bi bi-tag-fill"></i>' +
        '<p>' + (lang() === 'en' ? 'No sales posts yet.' : 'ရောင်းရန်ပို့စ် မရှိသေးပါ။') + '</p>' +
        '</div>';
      return;
    }

    grid.innerHTML = salesPosts.map(function (p) {
      return '<div class="col-12 col-lg-6">' +
        '<article class="post-card">' +
        '<div class="post-card-head">' +
        '<div class="post-avatar">' + initials(p.creatorUsername) + '</div>' +
        '<div class="post-user"><div class="name">' + escapeHtml(p.creatorUsername || '-') + '</div><div class="time">' + timeAgo(p.createdDate) + '</div></div>' +
        '<span class="post-type-badge sale">' + (lang() === 'en' ? 'Sales Post' : 'ရောင်းရန်ပို့စ်') + '</span>' +
        '</div>' +
        '<p class="post-title">' + escapeHtml(p.title) + '</p>' +
        '<p class="post-content">' + escapeHtml(p.description || '') + '</p>' +
        '<div class="post-meta"><span class="post-chip"><i class="bi bi-tag-fill"></i> ' + (p.price != null ? p.price : '-') + '</span></div>' +
        '</article>' +
        '</div>';
    }).join('');
  }

  function renderBuyRequests() {
    var grid = document.getElementById('buyRequestsGrid');

    if (buyRequests.length === 0) {
      grid.innerHTML = '<div class="col-12 empty-state">' +
        '<i class="bi bi-boxes"></i>' +
        '<p>' + (lang() === 'en' ? 'No buy requests yet.' : 'ဝယ်လိုမှု မရှိသေးပါ။') + '</p>' +
        '</div>';
      return;
    }

    grid.innerHTML = buyRequests.map(function (r) {
      return '<div class="col-12 col-lg-6">' +
        '<article class="post-card">' +
        '<div class="post-card-head">' +
        '<div class="post-avatar">' + initials(r.creatorUsername) + '</div>' +
        '<div class="post-user"><div class="name">' + escapeHtml(r.creatorUsername || '-') + '</div><div class="time">' + timeAgo(r.createdDate) + '</div></div>' +
        '<span class="post-type-badge request">' + (lang() === 'en' ? 'Buy Request' : 'ဝယ်လိုမှု') + '</span>' +
        '</div>' +
        '<p class="post-title">' + escapeHtml(r.title) + '</p>' +
        '<p class="post-content">' + escapeHtml(r.description || '') + '</p>' +
        '<div class="post-meta"><span class="post-chip"><i class="bi bi-boxes"></i> ' + (lang() === 'en' ? 'Qty: ' : 'အရေအတွက်: ') + (r.quantity != null ? r.quantity : '-') + '</span></div>' +
        '</article>' +
        '</div>';
    }).join('');
  }

  function loadSalesPosts() {
    window.DashboardUI.authFetch('/api/v1/sales-posts')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        salesPosts = data;
        renderSalesPosts();
      })
      .catch(function () {
        window.DashboardUI.toast('ရောင်းရန်ပို့စ်များ တင်ရန် မအောင်မြင်ပါ။', 'Failed to load sales posts.', 'bi-exclamation-triangle-fill');
      });
  }

  function isFarmer() {
    var session = window.DashboardUI.getSession();
    return !!(session && session.user && session.user.accountType === 'FARMER');
  }

  function loadBuyRequests() {
    if (!isFarmer()) {
      var buyTabItem = document.getElementById('buyTabItem');
      if (buyTabItem) buyTabItem.style.display = 'none';
      return;
    }

    var grid = document.getElementById('buyRequestsGrid');
    window.DashboardUI.authFetch('/api/v1/buy-requests')
      .then(function (response) {
        if (response.status === 403) {
          buyRequests = null;
          grid.innerHTML = '<div class="col-12 empty-state">' +
            '<i class="bi bi-lock-fill"></i>' +
            '<p>' + (lang() === 'en' ? 'Buy requests are available to farmer accounts only.' : 'ဝယ်လိုမှုများကို တောင်သူအကောင့်များသာ ကြည့်ရှုနိုင်ပါသည်။') + '</p>' +
            '</div>';
          return null;
        }
        return response.json();
      })
      .then(function (data) {
        if (data == null) return;
        buyRequests = data;
        renderBuyRequests();
      })
      .catch(function () {
        window.DashboardUI.toast('ဝယ်လိုမှုများ တင်ရန် မအောင်မြင်ပါ။', 'Failed to load buy requests.', 'bi-exclamation-triangle-fill');
      });
  }

  function saveSalesPost() {
    var title = document.getElementById('salesPostTitleInput').value.trim();
    if (!title) {
      window.DashboardUI.toast('ခေါင်းစဉ် ဖြည့်စွက်ပါ။', 'Title is required.', 'bi-exclamation-triangle-fill');
      return;
    }
    var price = document.getElementById('salesPostPriceInput').value.trim();
    var body = {
      title: title,
      description: document.getElementById('salesPostDescriptionInput').value.trim(),
      price: price ? Number(price) : null
    };
    var saveBtn = document.getElementById('saveSalesPostBtn');
    saveBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/sales-posts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
      .then(function (response) {
        if (!response.ok) throw new Error('save failed');
        salesPostModal.hide();
        document.getElementById('salesPostForm').reset();
        loadSalesPosts();
        window.DashboardUI.toast('ပို့စ် တင်ပြီးပါပြီ။', 'Sales post created.');
      })
      .catch(function () {
        window.DashboardUI.toast('ပို့စ် တင်၍ မရပါ။', 'Could not create sales post.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function saveBuyRequest() {
    var title = document.getElementById('buyRequestTitleInput').value.trim();
    if (!title) {
      window.DashboardUI.toast('ခေါင်းစဉ် ဖြည့်စွက်ပါ။', 'Title is required.', 'bi-exclamation-triangle-fill');
      return;
    }
    var quantity = document.getElementById('buyRequestQuantityInput').value.trim();
    var body = {
      title: title,
      description: document.getElementById('buyRequestDescriptionInput').value.trim(),
      quantity: quantity ? Number(quantity) : null
    };
    var saveBtn = document.getElementById('saveBuyRequestBtn');
    saveBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/buy-requests', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
      .then(function (response) {
        if (!response.ok) throw new Error('save failed');
        buyRequestModal.hide();
        document.getElementById('buyRequestForm').reset();
        loadBuyRequests();
        window.DashboardUI.toast('ဝယ်လိုမှု တင်ပြီးပါပြီ။', 'Buy request created.');
      })
      .catch(function () {
        window.DashboardUI.toast('ဝယ်လိုမှု တင်၍ မရပါ။', 'Could not create buy request.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function switchTab(tab) {
    document.querySelectorAll('#marketplaceTabs .nav-link').forEach(function (btn) {
      btn.classList.toggle('active', btn.getAttribute('data-tab') === tab);
    });
    document.querySelectorAll('.marketplace-pane').forEach(function (pane) {
      pane.style.display = pane.getAttribute('data-pane') === tab ? '' : 'none';
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadSalesPosts();
    loadBuyRequests();

    document.getElementById('marketplaceTabs').addEventListener('click', function (e) {
      var btn = e.target.closest('button[data-tab]');
      if (!btn) return;
      switchTab(btn.getAttribute('data-tab'));
    });

    document.getElementById('addSalesPostBtn').addEventListener('click', function () {
      document.getElementById('salesPostForm').reset();
      if (salesPostModal) salesPostModal.show();
    });
    document.getElementById('saveSalesPostBtn').addEventListener('click', saveSalesPost);

    document.getElementById('addBuyRequestBtn').addEventListener('click', function () {
      document.getElementById('buyRequestForm').reset();
      if (buyRequestModal) buyRequestModal.show();
    });
    document.getElementById('saveBuyRequestBtn').addEventListener('click', saveBuyRequest);

    document.addEventListener('dashboardlangchange', function () {
      renderSalesPosts();
      if (buyRequests) renderBuyRequests();
    });
  });
})();
