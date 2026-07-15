/* ==========================================================================
   Broiler Farming System — Admin Frontend
   Upgrade Requests (account-upgrades.jsp) — reviews Guest-to-Farmer upgrade
   requests via /api/v1/admin/account-upgrade-requests (list) and
   /api/v1/admin/account-upgrade-requests/{id}/review (approve/reject).
   Mirrors admin/assets/js/payments-page.js.
   ========================================================================== */

(function () {
  var requests = [];
  var currentFilter = 'all';

  var STATUS_LABELS = {
    PENDING: { my: 'ဆိုင်းငံ့ထား', en: 'Pending' },
    APPROVED: { my: 'အတည်ပြုပြီး', en: 'Approved' },
    REJECTED: { my: 'ငြင်းပယ်ပြီး', en: 'Rejected' }
  };

  var STATUS_BADGE_CLASS = {
    PENDING: 'badge-gold',
    APPROVED: 'badge-emerald',
    REJECTED: 'badge-danger-soft'
  };

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function formatDate(iso) {
    if (!iso) return '-';
    var d = new Date(iso);
    return isNaN(d.getTime()) ? '-' : d.toLocaleDateString();
  }

  function statusBadge(status) {
    var label = STATUS_LABELS[status] || { my: status, en: status };
    var cls = STATUS_BADGE_CLASS[status] || 'badge-muted';
    return '<span class="badge ' + cls + '">' + escapeHtml(lang() === 'en' ? label.en : label.my) + '</span>';
  }

  function matches(r) {
    return currentFilter === 'all' || r.status === currentFilter;
  }

  function render() {
    var tbody = document.getElementById('upgradesTableBody');
    var visible = requests.filter(matches);

    if (visible.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No upgrade requests found.' : 'အဆင့်တင်ခြင်း တောင်းဆိုချက် မတွေ့ပါ။') + '</td></tr>';
    } else {
      tbody.innerHTML = visible.map(function (r) {
        var actions = r.status === 'PENDING'
          ? '<button class="btn btn-outline btn-sm btn-approve" data-id="' + r.id + '"><i class="bi bi-check-lg"></i> ' + (lang() === 'en' ? 'Approve' : 'အတည်ပြု') + '</button> ' +
            '<button class="btn btn-danger btn-sm btn-reject" data-id="' + r.id + '"><i class="bi bi-x-lg"></i> ' + (lang() === 'en' ? 'Reject' : 'ငြင်းပယ်') + '</button>'
          : '-';
        return '<tr data-id="' + r.id + '">' +
          '<td style="font-weight:600;">' + escapeHtml(r.username || ('User #' + r.userId)) + '</td>' +
          '<td>' + escapeHtml(r.reason || '-') + '</td>' +
          '<td>' + statusBadge(r.status) + '</td>' +
          '<td>' + formatDate(r.requestedAt) + '</td>' +
          '<td class="text-end">' + actions + '</td>' +
          '</tr>';
      }).join('');
    }

    var countEl = document.getElementById('upgradesResultsCount');
    if (countEl) countEl.textContent = visible.length + ' / ' + requests.length;
  }

  function loadRequests() {
    window.AdminUI.authFetch('/api/v1/admin/account-upgrade-requests')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load upgrade requests');
        return response.json();
      })
      .then(function (data) {
        requests = data;
        render();
      })
      .catch(function () {
        window.AdminUI.toast('တောင်းဆိုချက်များ ရယူ၍မရပါ။', 'Failed to load upgrade requests.', 'bi-exclamation-triangle-fill');
      });
  }

  function review(id, decision) {
    var adminNote = null;
    if (decision === 'REJECTED') {
      adminNote = window.prompt(lang() === 'en'
        ? 'Optional note to show the user (leave blank to skip):'
        : 'အသုံးပြုသူအတွက် မှတ်ချက် (ချန်ထားနိုင်သည်):');
      if (adminNote === null) return; // user cancelled the prompt
    }

    window.AdminUI.authFetch('/api/v1/admin/account-upgrade-requests/' + id + '/review', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ decision: decision, adminNote: adminNote || null })
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (body) {
          return { ok: response.ok, body: body };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not review this request.' : 'ဤတောင်းဆိုချက်ကို သုံးသပ်၍ မရပါ။');
          window.AdminUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        loadRequests();
        window.AdminUI.toast('တောင်းဆိုချက် သုံးသပ်ပြီးပါပြီ။', 'Upgrade request reviewed.', 'bi-check-circle-fill');
      })
      .catch(function () {
        window.AdminUI.toast('တောင်းဆိုချက် သုံးသပ်၍ မရပါ။', 'Could not review this request.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var tabs = document.getElementById('upgradesFilterTabs');
    if (tabs) {
      tabs.querySelectorAll('button').forEach(function (btn) {
        btn.addEventListener('click', function () {
          tabs.querySelectorAll('button').forEach(function (b) { b.classList.remove('active'); });
          btn.classList.add('active');
          currentFilter = btn.getAttribute('data-filter');
          render();
        });
      });
    }

    document.getElementById('upgradesTableBody').addEventListener('click', function (e) {
      var approveBtn = e.target.closest('.btn-approve');
      if (approveBtn) {
        review(Number(approveBtn.getAttribute('data-id')), 'APPROVED');
        return;
      }
      var rejectBtn = e.target.closest('.btn-reject');
      if (rejectBtn) {
        review(Number(rejectBtn.getAttribute('data-id')), 'REJECTED');
      }
    });

    document.addEventListener('adminlangchange', render);

    loadRequests();
  });
})();
