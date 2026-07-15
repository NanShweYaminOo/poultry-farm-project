/* ==========================================================================
   Broiler Farming System — Guest Marketplace (view only)
   Read-only variant of farmer/assets/js/marketplace-page.js: browses
   sales posts (/api/v1/sales-posts) but has no create UI at all -- posting
   is gated server-side to FARMER accounts (see
   SalesPostServiceImpl.requirePostingPrivilege) and there is deliberately
   no buy-requests tab here either, matching the existing farmer marketplace
   behavior of hiding buy requests from non-Farmer accounts.
   ========================================================================== */

(function () {
  var salesPosts = [];

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

  function loadSalesPosts() {
    window.GuestUI.authFetch('/api/v1/sales-posts')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        salesPosts = data;
        renderSalesPosts();
      })
      .catch(function () {
        window.GuestUI.toast('ရောင်းရန်ပို့စ်များ တင်ရန် မအောင်မြင်ပါ။', 'Failed to load sales posts.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadSalesPosts();
    document.addEventListener('dashboardlangchange', renderSalesPosts);
  });
})();
