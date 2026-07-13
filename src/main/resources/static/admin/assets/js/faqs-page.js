/* ==========================================================================
   Broiler Farming System — Admin Frontend
   FAQ Management (faqs.jsp) — full CRUD against /api/v1/faqs via
   AdminUI.authFetch. Renders as a Bootstrap accordion.
   ========================================================================== */

(function () {
  var faqs = [];
  var editingId = null;
  var deletingId = null;

  var faqModalEl = document.getElementById('faqModal');
  var faqModal = faqModalEl ? new bootstrap.Modal(faqModalEl) : null;
  var deleteModalEl = document.getElementById('deleteFaqModal');
  var deleteModal = deleteModalEl ? new bootstrap.Modal(deleteModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function render() {
    var container = document.getElementById('faqAccordion');

    if (faqs.length === 0) {
      container.innerHTML = '<div class="card text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No FAQs yet.' : 'မေးခွန်း မရှိသေးပါ။') + '</div>';
      return;
    }

    container.innerHTML = faqs.map(function (f, index) {
      var collapseId = 'faq' + f.id;
      return '<div class="accordion-item" data-id="' + f.id + '">' +
        '<h2 class="accordion-header">' +
        '<button class="accordion-button' + (index === 0 ? '' : ' collapsed') + '" type="button" data-bs-toggle="collapse" data-bs-target="#' + collapseId + '">' +
        escapeHtml(f.question) +
        '</button>' +
        '</h2>' +
        '<div id="' + collapseId + '" class="accordion-collapse collapse' + (index === 0 ? ' show' : '') + '" data-bs-parent="#faqAccordion">' +
        '<div class="accordion-body" style="font-size:13.5px;color:var(--muted);">' +
        escapeHtml(f.answer) +
        '<div class="mt-2 d-flex gap-2">' +
        '<button class="btn btn-outline btn-sm btn-edit" data-id="' + f.id + '"><i class="bi bi-pencil-fill"></i> ' + (lang() === 'en' ? 'Edit' : 'ပြင်ဆင်ရန်') + '</button>' +
        '<button class="btn btn-danger btn-sm btn-delete" data-id="' + f.id + '"><i class="bi bi-trash-fill"></i> ' + (lang() === 'en' ? 'Delete' : 'ဖျက်သိမ်းရန်') + '</button>' +
        '</div>' +
        '</div>' +
        '</div>' +
        '</div>';
    }).join('');
  }

  function loadFaqs() {
    window.AdminUI.authFetch('/api/v1/faqs')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        faqs = data;
        render();
      })
      .catch(function () {
        window.AdminUI.toast('မေးခွန်းစာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load FAQs.', 'bi-exclamation-triangle-fill');
      });
  }

  function resetForm() {
    editingId = null;
    document.getElementById('faqIdInput').value = '';
    document.getElementById('faqQuestionInput').value = '';
    document.getElementById('faqAnswerInput').value = '';
  }

  function openAddModal() {
    resetForm();
    document.getElementById('faqModalTitle').textContent =
      lang() === 'en' ? 'Add New FAQ' : 'မေးခွန်းအသစ်ထည့်ရန်';
    faqModal.show();
  }

  function openEditModal(faq) {
    resetForm();
    editingId = faq.id;
    document.getElementById('faqIdInput').value = faq.id;
    document.getElementById('faqQuestionInput').value = faq.question || '';
    document.getElementById('faqAnswerInput').value = faq.answer || '';
    document.getElementById('faqModalTitle').textContent =
      lang() === 'en' ? 'Edit FAQ' : 'မေးခွန်း ပြင်ဆင်ရန်';
    faqModal.show();
  }

  function saveFaq() {
    var question = document.getElementById('faqQuestionInput').value.trim();
    var answer = document.getElementById('faqAnswerInput').value.trim();
    if (!question || !answer) {
      window.AdminUI.toast('မေးခွန်းနှင့် အဖြေ ဖြည့်စွက်ပါ။', 'Question and answer are required.', 'bi-exclamation-triangle-fill');
      return;
    }

    var url = editingId ? '/api/v1/faqs/' + editingId : '/api/v1/faqs';
    var method = editingId ? 'PUT' : 'POST';
    var saveBtn = document.getElementById('saveFaqBtn');
    saveBtn.disabled = true;

    window.AdminUI.authFetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question: question, answer: answer })
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (body) {
          return { ok: response.ok, body: body };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not save FAQ.' : 'မေးခွန်း သိမ်းဆည်း၍ မရပါ။');
          window.AdminUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        faqModal.hide();
        loadFaqs();
        window.AdminUI.toast('မေးခွန်း သိမ်းဆည်းပြီးပါပြီ။', 'FAQ saved.');
      })
      .catch(function () {
        window.AdminUI.toast('မေးခွန်း သိမ်းဆည်း၍ မရပါ။', 'Could not save FAQ.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function confirmDelete() {
    if (deletingId == null) return;
    window.AdminUI.authFetch('/api/v1/faqs/' + deletingId, { method: 'DELETE' })
      .then(function (response) {
        if (!response.ok) throw new Error('delete failed');
        deleteModal.hide();
        loadFaqs();
        window.AdminUI.toast('မေးခွန်းကို ဖျက်သိမ်းပြီးပါပြီ။', 'FAQ deleted.', 'bi-trash-fill');
      })
      .catch(function () {
        window.AdminUI.toast('မေးခွန်း ဖျက်သိမ်း၍ မရပါ။', 'Could not delete FAQ.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadFaqs();

    document.getElementById('addFaqBtn').addEventListener('click', openAddModal);
    document.getElementById('saveFaqBtn').addEventListener('click', saveFaq);
    document.getElementById('confirmDeleteFaq').addEventListener('click', confirmDelete);

    document.getElementById('faqAccordion').addEventListener('click', function (e) {
      var editBtn = e.target.closest('.btn-edit');
      if (editBtn) {
        var faq = faqs.find(function (f) { return f.id === Number(editBtn.getAttribute('data-id')); });
        if (faq) openEditModal(faq);
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
