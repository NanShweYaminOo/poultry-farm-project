/* ==========================================================================
   Broiler Farming System — User Dashboard
   FAQs (faqs.jsp) — read-only accordion from /api/v1/faqs.
   ========================================================================== */

(function () {
  var faqs = [];

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
        '</div>' +
        '</div>' +
        '</div>';
    }).join('');
  }

  function loadFaqs() {
    window.DashboardUI.authFetch('/api/v1/faqs')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        faqs = data;
        render();
      })
      .catch(function () {
        window.DashboardUI.toast('မေးခွန်းစာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load FAQs.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadFaqs();
    document.addEventListener('dashboardlangchange', render);
  });
})();
