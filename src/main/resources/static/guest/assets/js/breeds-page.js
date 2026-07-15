/* ==========================================================================
   Broiler Farming System — User Dashboard
   Breed Reference (breeds.jsp) — read-only list from /api/v1/breeds.
   ========================================================================== */

(function () {
  var breeds = [];

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function thumb(breed) {
    if (breed.imageUrl) {
      return '<img src="' + breed.imageUrl + '" alt="" style="width:44px;height:44px;object-fit:cover;border-radius:8px;">';
    }
    return '<div style="width:44px;height:44px;border-radius:8px;background:var(--line);"></div>';
  }

  function render() {
    var tbody = document.getElementById('breedsTableBody');

    if (breeds.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No breeds yet.' : 'မျိုးရင်း မရှိသေးပါ။') + '</td></tr>';
      return;
    }

    tbody.innerHTML = breeds.map(function (b) {
      return '<tr data-id="' + b.id + '">' +
        '<td>' + thumb(b) + '</td>' +
        '<td style="font-weight:700;color:var(--emerald-900);">' + escapeHtml(b.name) + '</td>' +
        '<td style="color:var(--muted);">' + escapeHtml(b.origin || '-') + '</td>' +
        '<td>' + (b.avgMarketWeightKg != null ? b.avgMarketWeightKg + ' kg' : '-') + '</td>' +
        '<td>' + (b.growthPeriodDays != null ? b.growthPeriodDays : '-') + '</td>' +
        '<td>' + (b.fcr != null ? b.fcr : '-') + '</td>' +
        '<td style="color:var(--muted);">' + escapeHtml(b.description || '-') + '</td>' +
        '</tr>';
    }).join('');
  }

  function loadBreeds() {
    window.GuestUI.authFetch('/api/v1/breeds')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        breeds = data;
        render();
      })
      .catch(function () {
        window.GuestUI.toast('မျိုးရင်းစာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load breeds.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadBreeds();
    document.addEventListener('dashboardlangchange', render);
  });
})();
