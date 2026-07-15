/* ==========================================================================
   Broiler Farming System — Admin Frontend
   Payments (payments.jsp) — reviews Premium/posting-extension payment
   requests via /api/v1/admin/payment-transactions (list) and
   /api/v1/payment-transactions/{id}/review (approve/reject).
   ========================================================================== */

(function () {
  var transactions = [];
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

  var TYPE_LABELS = {
    BATCH_REGISTRATION: { my: 'Premium အဆင့်တင်ရန်', en: 'Premium Upgrade' },
    POSTING_EXTENSION: { my: 'ပို့စ်တင်ချိန် တိုးရန် ကြေး', en: 'Posting Extension Fee' }
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

  function typeLabel(type) {
    var label = TYPE_LABELS[type] || { my: type, en: type };
    return escapeHtml(lang() === 'en' ? label.en : label.my);
  }

  function matches(t) {
    return currentFilter === 'all' || t.status === currentFilter;
  }

  function render() {
    var tbody = document.getElementById('paymentsTableBody');
    var visible = transactions.filter(matches);

    if (visible.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No payment requests found.' : 'ငွေပေးချေမှု တောင်းဆိုချက် မတွေ့ပါ။') + '</td></tr>';
    } else {
      tbody.innerHTML = visible.map(function (t) {
        var actions = t.status === 'PENDING'
          ? '<button class="btn btn-outline btn-sm btn-approve" data-id="' + t.id + '"><i class="bi bi-check-lg"></i> ' + (lang() === 'en' ? 'Approve' : 'အတည်ပြု') + '</button> ' +
            '<button class="btn btn-danger btn-sm btn-reject" data-id="' + t.id + '"><i class="bi bi-x-lg"></i> ' + (lang() === 'en' ? 'Reject' : 'ငြင်းပယ်') + '</button>'
          : '-';
        return '<tr data-id="' + t.id + '">' +
          '<td style="font-weight:600;">' + escapeHtml(t.userUsername || ('User #' + t.userId)) + '</td>' +
          '<td>' + escapeHtml(t.batchName || ('Batch #' + t.batchId)) + '</td>' +
          '<td>' + typeLabel(t.paymentType) + '</td>' +
          '<td><a href="' + escapeHtml(t.screenshotUrl) + '" target="_blank" rel="noopener">' + (lang() === 'en' ? 'View' : 'ကြည့်ရန်') + '</a></td>' +
          '<td>' + statusBadge(t.status) + '</td>' +
          '<td>' + formatDate(t.transactionTimestamp) + '</td>' +
          '<td class="text-end">' + actions + '</td>' +
          '</tr>';
      }).join('');
    }

    var countEl = document.getElementById('paymentsResultsCount');
    if (countEl) countEl.textContent = visible.length + ' / ' + transactions.length;
  }

  function loadTransactions() {
    window.AdminUI.authFetch('/api/v1/admin/payment-transactions')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load payment transactions');
        return response.json();
      })
      .then(function (data) {
        transactions = data;
        render();
      })
      .catch(function () {
        window.AdminUI.toast('ငွေပေးချေမှုများ ရယူ၍မရပါ။', 'Failed to load payment requests.', 'bi-exclamation-triangle-fill');
      });
  }

  function review(id, decision) {
    window.AdminUI.authFetch('/api/v1/payment-transactions/' + id + '/review', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ decision: decision })
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
        loadTransactions();
        window.AdminUI.toast('ငွေပေးချေမှု သုံးသပ်ပြီးပါပြီ။', 'Payment request reviewed.', 'bi-check-circle-fill');
      })
      .catch(function () {
        window.AdminUI.toast('ငွေပေးချေမှု သုံးသပ်၍ မရပါ။', 'Could not review this request.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var tabs = document.getElementById('paymentsFilterTabs');
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

    document.getElementById('paymentsTableBody').addEventListener('click', function (e) {
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

    loadTransactions();
  });
})();
