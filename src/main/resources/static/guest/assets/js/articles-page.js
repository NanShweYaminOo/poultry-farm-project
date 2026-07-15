/* ==========================================================================
   Broiler Farming System — User Dashboard
   Articles (articles.jsp) — read-only, published-only list from
   /api/v1/knowledge-posts.
   ========================================================================== */

(function () {
  var articles = [];
  var viewModalEl = document.getElementById('articleViewModal');
  var viewModal = viewModalEl ? new bootstrap.Modal(viewModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function title(a) {
    return lang() === 'en' ? (a.titleEn || a.titleMy) : (a.titleMy || a.titleEn);
  }

  function content(a) {
    return lang() === 'en' ? (a.contentEn || a.contentMy) : (a.contentMy || a.contentEn);
  }

  function typeBadge(type) {
    return '<span class="badge badge-gold">' + escapeHtml(type || '-') + '</span>';
  }

  function render() {
    var grid = document.getElementById('articlesGrid');
    var published = articles.filter(function (a) { return a.status === 'PUBLISHED'; });

    if (published.length === 0) {
      grid.innerHTML = '<div class="col-12 empty-state">' +
        '<i class="bi bi-journal-richtext"></i>' +
        '<p>' + (lang() === 'en' ? 'No articles yet.' : 'ဆောင်းပါး မရှိသေးပါ။') + '</p>' +
        '</div>';
      return;
    }

    grid.innerHTML = published.map(function (a) {
      return '<div class="col-6 col-lg-4">' +
        '<a href="#" class="card article-card" data-id="' + a.id + '" style="display:block;height:100%;padding:16px;text-decoration:none;">' +
        (a.imageUrl ? '<img src="' + a.imageUrl + '" alt="" style="width:100%;height:120px;object-fit:cover;border-radius:8px;margin-bottom:10px;">' : '') +
        typeBadge(a.postType) +
        '<div style="font-weight:700;color:var(--emerald-900);margin-top:8px;">' + escapeHtml(title(a)) + '</div>' +
        '<div style="font-size:12.5px;color:var(--muted);margin-top:4px;">' + (a.authorUsername ? escapeHtml(a.authorUsername) : '') + '</div>' +
        '</a>' +
        '</div>';
    }).join('');
  }

  function openArticle(article) {
    var imageEl = document.getElementById('articleViewImage');
    if (article.imageUrl) {
      imageEl.src = article.imageUrl;
      imageEl.style.display = 'block';
    } else {
      imageEl.style.display = 'none';
    }
    document.getElementById('articleViewTitle').textContent = title(article);
    document.getElementById('articleViewContent').textContent = content(article);

    var documentLink = document.getElementById('articleViewDocumentLink');
    if (article.documentUrl) {
      documentLink.href = article.documentUrl;
      documentLink.style.display = '';
    } else {
      documentLink.style.display = 'none';
    }

    if (viewModal) viewModal.show();
  }

  function loadArticles() {
    window.GuestUI.authFetch('/api/v1/knowledge-posts')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        articles = data;
        render();
      })
      .catch(function () {
        window.GuestUI.toast('ဆောင်းပါးစာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load articles.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadArticles();

    document.getElementById('articlesGrid').addEventListener('click', function (e) {
      var card = e.target.closest('.article-card');
      if (!card) return;
      e.preventDefault();
      var article = articles.find(function (a) { return a.id === Number(card.getAttribute('data-id')); });
      if (article) openArticle(article);
    });

    document.addEventListener('dashboardlangchange', render);
  });
})();
