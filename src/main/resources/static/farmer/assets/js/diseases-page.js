/* ==========================================================================
   Broiler Farming System — User Dashboard
   Disease Reference (diseases.jsp) — read-only list from /api/v1/diseases.
   ========================================================================== */

(function () {
  var diseases = [];

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function thumb(disease) {
    if (disease.imageUrl) {
      return '<img src="' + disease.imageUrl + '" alt="" style="width:44px;height:44px;object-fit:cover;border-radius:8px;">';
    }
    return '<div style="width:44px;height:44px;border-radius:8px;background:var(--line);"></div>';
  }

  function severityBadge(severity) {
    var cls = 'badge-muted';
    if (severity === 'MILD') cls = 'badge-emerald';
    else if (severity === 'MODERATE') cls = 'badge-gold';
    else if (severity === 'SEVERE') cls = 'badge-danger-soft';
    return '<span class="badge ' + cls + '">' + escapeHtml(severity || '-') + '</span>';
  }

  function render() {
    var tbody = document.getElementById('diseasesTableBody');

    if (diseases.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No diseases yet.' : 'ရောဂါ မရှိသေးပါ။') + '</td></tr>';
      return;
    }

    tbody.innerHTML = diseases.map(function (d) {
      return '<tr data-id="' + d.id + '">' +
        '<td>' + thumb(d) + '</td>' +
        '<td style="font-weight:700;color:var(--emerald-900);">' + escapeHtml(d.name) + '</td>' +
        '<td style="color:var(--muted);">' + escapeHtml(d.keySymptoms || '-') + '</td>' +
        '<td>' + severityBadge(d.severity) + '</td>' +
        '<td style="color:var(--muted);">' + escapeHtml(d.notes || '-') + '</td>' +
        '</tr>';
    }).join('');
  }

  function loadDiseases() {
    window.DashboardUI.authFetch('/api/v1/diseases')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        diseases = data;
        render();
      })
      .catch(function () {
        window.DashboardUI.toast('ရောဂါစာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load diseases.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadDiseases();
    document.addEventListener('dashboardlangchange', render);
  });
})();
