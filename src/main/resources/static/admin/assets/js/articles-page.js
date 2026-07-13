/* ==========================================================================
   Broiler Farming System — Admin Frontend
   Knowledge Articles (articles.jsp) — full CRUD against
   /api/v1/knowledge-posts, including photo + PDF upload, via
   AdminUI.authFetch.
   ========================================================================== */

(function () {
  var articles = [];
  var editingId = null;
  var deletingId = null;

  var articleModalEl = document.getElementById('articleModal');
  var articleModal = articleModalEl ? new bootstrap.Modal(articleModalEl) : null;
  var deleteModalEl = document.getElementById('deleteArticleModal');
  var deleteModal = deleteModalEl ? new bootstrap.Modal(deleteModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function typeBadge(postType) {
    var classes = {
      ARTICLE: 'badge-emerald',
      DISEASE_GUIDE: 'badge-danger-soft',
      TIP: 'badge-gold',
      ANNOUNCEMENT: 'badge-muted'
    };
    var cls = classes[postType] || 'badge-muted';
    return '<span class="badge ' + cls + '">' + escapeHtml(postType) + '</span>';
  }

  function statusBadge(status) {
    if (status === 'DRAFT') {
      return '<span class="badge badge-muted">' + (lang() === 'en' ? 'Draft' : 'မူကြမ်း') + '</span>';
    }
    return '<span class="badge badge-emerald">' + (lang() === 'en' ? 'Published' : 'ထုတ်ဝေပြီး') + '</span>';
  }

  function thumb(article) {
    if (article.imageUrl) {
      return '<img src="' + article.imageUrl + '" alt="" style="width:44px;height:44px;object-fit:cover;border-radius:8px;">';
    }
    return '<div style="width:44px;height:44px;border-radius:8px;background:var(--line);"></div>';
  }

  function titleCell(article) {
    var title = lang() === 'en' ? article.titleEn : article.titleMy;
    var html = escapeHtml(title || '-');
    if (article.documentUrl) {
      html += ' <a href="' + article.documentUrl + '" target="_blank" rel="noopener" title="PDF"><i class="bi bi-file-earmark-pdf-fill" style="color:var(--danger);"></i></a>';
    }
    return html;
  }

  function formatDate(isoDateTime) {
    return isoDateTime ? isoDateTime.slice(0, 10) : '-';
  }

  function render() {
    var tbody = document.getElementById('articlesTableBody');

    if (articles.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No articles yet.' : 'ဆောင်းပါး မရှိသေးပါ။') + '</td></tr>';
      return;
    }

    tbody.innerHTML = articles.map(function (a) {
      return '<tr data-id="' + a.id + '">' +
        '<td>' + thumb(a) + '</td>' +
        '<td style="font-weight:700;color:var(--emerald-900);">' + titleCell(a) + '</td>' +
        '<td>' + typeBadge(a.postType) + '</td>' +
        '<td style="color:var(--muted);">' + escapeHtml(a.authorUsername || '-') + '</td>' +
        '<td style="color:var(--muted);">' + formatDate(a.createdDate) + '</td>' +
        '<td>' + statusBadge(a.status) + '</td>' +
        '<td class="text-end">' +
        '<button class="btn btn-outline btn-sm btn-edit" data-id="' + a.id + '"><i class="bi bi-pencil-fill"></i></button> ' +
        '<button class="btn btn-danger btn-sm btn-delete" data-id="' + a.id + '"><i class="bi bi-trash-fill"></i></button>' +
        '</td>' +
        '</tr>';
    }).join('');
  }

  function loadArticles() {
    window.AdminUI.authFetch('/api/v1/knowledge-posts')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        articles = data;
        render();
      })
      .catch(function () {
        window.AdminUI.toast('ဆောင်းပါးစာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load articles.', 'bi-exclamation-triangle-fill');
      });
  }

  function resetForm() {
    editingId = null;
    document.getElementById('articleIdInput').value = '';
    document.getElementById('articleTitleMyInput').value = '';
    document.getElementById('articleTitleEnInput').value = '';
    document.getElementById('articleTypeInput').value = 'ARTICLE';
    document.getElementById('articleStatusInput').value = 'PUBLISHED';
    document.getElementById('articleContentMyInput').value = '';
    document.getElementById('articleContentEnInput').value = '';
    document.getElementById('articleImageInput').value = '';
    document.getElementById('articleImagePreviewWrap').style.display = 'none';
    document.getElementById('articleDocumentInput').value = '';
    document.getElementById('articleDocumentCurrentWrap').style.display = 'none';
  }

  function openAddModal() {
    resetForm();
    document.getElementById('articleModalTitle').textContent =
      lang() === 'en' ? 'Write New Article' : 'ဆောင်းပါးအသစ်ရေးရန်';
    articleModal.show();
  }

  function openEditModal(article) {
    resetForm();
    editingId = article.id;
    document.getElementById('articleIdInput').value = article.id;
    document.getElementById('articleTitleMyInput').value = article.titleMy || '';
    document.getElementById('articleTitleEnInput').value = article.titleEn || '';
    document.getElementById('articleTypeInput').value = article.postType || 'ARTICLE';
    document.getElementById('articleStatusInput').value = article.status || 'PUBLISHED';
    document.getElementById('articleContentMyInput').value = article.contentMy || '';
    document.getElementById('articleContentEnInput').value = article.contentEn || '';
    if (article.imageUrl) {
      document.getElementById('articleImagePreview').src = article.imageUrl;
      document.getElementById('articleImagePreviewWrap').style.display = 'block';
    }
    if (article.documentUrl) {
      document.getElementById('articleDocumentCurrentLink').href = article.documentUrl;
      document.getElementById('articleDocumentCurrentWrap').style.display = 'block';
    }
    document.getElementById('articleModalTitle').textContent =
      lang() === 'en' ? 'Edit Article' : 'ဆောင်းပါး ပြင်ဆင်ရန်';
    articleModal.show();
  }

  function saveArticle() {
    var titleMy = document.getElementById('articleTitleMyInput').value.trim();
    var titleEn = document.getElementById('articleTitleEnInput').value.trim();
    var contentMy = document.getElementById('articleContentMyInput').value.trim();
    var contentEn = document.getElementById('articleContentEnInput').value.trim();
    if (!titleMy || !titleEn || !contentMy || !contentEn) {
      window.AdminUI.toast('ခေါင်းစဉ်နှင့် အကြောင်းအရာ (နှစ်ဘာသာစလုံး) ဖြည့်စွက်ပါ။', 'Title and content are required in both languages.', 'bi-exclamation-triangle-fill');
      return;
    }

    var formData = new FormData();
    formData.append('titleMy', titleMy);
    formData.append('titleEn', titleEn);
    formData.append('contentMy', contentMy);
    formData.append('contentEn', contentEn);
    formData.append('postType', document.getElementById('articleTypeInput').value);
    formData.append('status', document.getElementById('articleStatusInput').value);
    var imageFile = document.getElementById('articleImageInput').files[0];
    if (imageFile) formData.append('image', imageFile);
    var documentFile = document.getElementById('articleDocumentInput').files[0];
    if (documentFile) formData.append('document', documentFile);

    var url = editingId ? '/api/v1/knowledge-posts/' + editingId : '/api/v1/knowledge-posts';
    var method = editingId ? 'PUT' : 'POST';
    var saveBtn = document.getElementById('saveArticleBtn');
    saveBtn.disabled = true;

    window.AdminUI.authFetch(url, { method: method, body: formData })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (body) {
          return { ok: response.ok, body: body };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not save article.' : 'ဆောင်းပါး သိမ်းဆည်း၍ မရပါ။');
          window.AdminUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        articleModal.hide();
        loadArticles();
        window.AdminUI.toast('ဆောင်းပါး သိမ်းဆည်းပြီးပါပြီ။', 'Article saved.');
      })
      .catch(function () {
        window.AdminUI.toast('ဆောင်းပါး သိမ်းဆည်း၍ မရပါ။', 'Could not save article.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function confirmDelete() {
    if (deletingId == null) return;
    window.AdminUI.authFetch('/api/v1/knowledge-posts/' + deletingId, { method: 'DELETE' })
      .then(function (response) {
        if (!response.ok) throw new Error('delete failed');
        deleteModal.hide();
        loadArticles();
        window.AdminUI.toast('ဆောင်းပါးကို ဖျက်သိမ်းပြီးပါပြီ။', 'Article deleted.', 'bi-trash-fill');
      })
      .catch(function () {
        window.AdminUI.toast('ဆောင်းပါး ဖျက်သိမ်း၍ မရပါ။', 'Could not delete article.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadArticles();

    document.getElementById('addArticleBtn').addEventListener('click', openAddModal);
    document.getElementById('saveArticleBtn').addEventListener('click', saveArticle);
    document.getElementById('confirmDeleteArticle').addEventListener('click', confirmDelete);

    document.getElementById('articlesTableBody').addEventListener('click', function (e) {
      var editBtn = e.target.closest('.btn-edit');
      if (editBtn) {
        var article = articles.find(function (a) { return a.id === Number(editBtn.getAttribute('data-id')); });
        if (article) openEditModal(article);
        return;
      }
      var delBtn = e.target.closest('.btn-delete');
      if (delBtn && deleteModal) {
        deletingId = Number(delBtn.getAttribute('data-id'));
        deleteModal.show();
      }
    });

    document.addEventListener('adminlangchange', render);
  });
})();
