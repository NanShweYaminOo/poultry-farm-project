/* ==========================================================================
   Broiler Farming System — User Dashboard
   Premium & Payments (premium.jsp) — FARMER-only. Lists/creates batches
   (/api/v1/batches), submits Premium/Post-Fee payment requests
   (/api/v1/payment-transactions) and shows the farmer's own request history
   (/api/v1/payment-transactions/me).
   ========================================================================== */

(function () {
  var batches = [];
  var paymentRequests = [];

  var batchModalEl = document.getElementById('batchModal');
  var batchModal = batchModalEl ? new bootstrap.Modal(batchModalEl) : null;

  var PAYMENT_STATUS_BADGE_CLASS = {
    PENDING: 'badge-gold',
    APPROVED: 'badge-emerald',
    REJECTED: 'badge-danger-soft'
  };

  var PAYMENT_TYPE_LABEL = {
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

  // BatchResponse never actually returns adminApprovedAt -- approval has to
  // be derived from the farmer's own BATCH_REGISTRATION payment transactions
  // instead (same rule PaymentTransactionServiceImpl uses server-side to set
  // batch.adminApprovedAt in the first place). See loadPaymentRequests().
  var approvedBatchIds = {};

  function renderBatches() {
    var grid = document.getElementById('batchesGrid');

    if (batches.length === 0) {
      grid.innerHTML = '<div class="col-12 empty-state">' +
        '<i class="bi bi-egg-fried"></i>' +
        '<p>' + (lang() === 'en' ? 'No batches yet. Create one to request Premium.' : 'Batch မရှိသေးပါ။ Premium တောင်းဆိုရန် Batch တစ်ခု ဖန်တီးပါ။') + '</p>' +
        '</div>';
      return;
    }

    grid.innerHTML = batches.map(function (b) {
      var approved = b.status === 'ACTIVE' && approvedBatchIds[b.id];
      return '<div class="col-6 col-lg-4">' +
        '<div class="card" style="padding:16px;height:100%;">' +
        '<div style="font-weight:700;color:var(--emerald-900);">' + escapeHtml(b.batchName || ('Batch #' + b.id)) + '</div>' +
        '<div style="font-size:12.5px;color:var(--muted);margin-top:4px;">' +
        (lang() === 'en' ? 'Chickens: ' : 'ကြက်အရေအတွက်: ') + (b.initialChickenCount != null ? b.initialChickenCount : '-') +
        '</div>' +
        '<div style="font-size:12.5px;color:var(--muted);">' +
        (lang() === 'en' ? 'Cycle: ' : 'ကာလ: ') + (b.cycleDurationDays != null ? b.cycleDurationDays + (lang() === 'en' ? ' days' : ' ရက်') : '-') +
        '</div>' +
        '<div style="margin-top:8px;">' +
        '<span class="badge ' + (b.isStarted ? 'badge-emerald' : (approved ? 'badge-gold' : 'badge-muted')) + '">' +
        (b.isStarted ? (lang() === 'en' ? 'Started' : 'စတင်ပြီး') : (approved ? (lang() === 'en' ? 'Approved' : 'အတည်ပြုပြီး') : (lang() === 'en' ? 'Awaiting Approval' : 'အတည်ပြုရန် စောင့်ဆိုင်းနေသည်'))) +
        '</span>' +
        '</div>' +
        '</div>' +
        '</div>';
    }).join('');
  }

  function populateBatchSelect() {
    var select = document.getElementById('paymentBatchSelect');
    var notice = document.getElementById('paymentNoBatchNotice');
    var submitBtn = document.getElementById('submitPaymentRequestBtn');

    select.innerHTML = batches.map(function (b) {
      return '<option value="' + b.id + '">' + escapeHtml(b.batchName || ('Batch #' + b.id)) + '</option>';
    }).join('');

    var hasBatches = batches.length > 0;
    select.parentElement.style.display = hasBatches ? '' : 'none';
    document.getElementById('paymentTypeSelect').parentElement.style.display = hasBatches ? '' : 'none';
    document.getElementById('paymentScreenshotInput').parentElement.style.display = hasBatches ? '' : 'none';
    notice.style.display = hasBatches ? 'none' : '';
    submitBtn.style.display = hasBatches ? '' : 'none';
  }

  function renderPaymentRequests() {
    var tbody = document.getElementById('paymentRequestsTableBody');

    if (paymentRequests.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No payment requests yet.' : 'ငွေပေးချေမှု တောင်းဆိုချက် မရှိသေးပါ။') + '</td></tr>';
      return;
    }

    tbody.innerHTML = paymentRequests.map(function (p) {
      var typeLabel = PAYMENT_TYPE_LABEL[p.paymentType] || { my: p.paymentType, en: p.paymentType };
      var badgeClass = PAYMENT_STATUS_BADGE_CLASS[p.status] || 'badge-muted';
      return '<tr>' +
        '<td>' + escapeHtml(p.batchName || ('Batch #' + p.batchId)) + '</td>' +
        '<td>' + escapeHtml(lang() === 'en' ? typeLabel.en : typeLabel.my) + '</td>' +
        '<td><a href="' + escapeHtml(p.screenshotUrl) + '" target="_blank" rel="noopener">' + (lang() === 'en' ? 'View' : 'ကြည့်ရန်') + '</a></td>' +
        '<td><span class="badge ' + badgeClass + '">' + escapeHtml(p.status) + '</span></td>' +
        '<td>' + formatDate(p.transactionTimestamp) + '</td>' +
        '</tr>';
    }).join('');
  }

  function loadBatches() {
    return window.DashboardUI.authFetch('/api/v1/batches')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        batches = data;
        renderBatches();
        populateBatchSelect();
        renderPaymentRequests();
      })
      .catch(function () {
        window.DashboardUI.toast('Batch စာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load batches.', 'bi-exclamation-triangle-fill');
      });
  }

  function loadPaymentRequests() {
    return window.DashboardUI.authFetch('/api/v1/payment-transactions/me')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        paymentRequests = data;
        approvedBatchIds = {};
        paymentRequests
          .filter(function (p) { return p.paymentType === 'BATCH_REGISTRATION' && p.status === 'APPROVED'; })
          .forEach(function (p) { approvedBatchIds[p.batchId] = true; });
        renderPaymentRequests();
        renderBatches();
      })
      .catch(function () {
        window.DashboardUI.toast('ငွေပေးချေမှု စာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load payment requests.', 'bi-exclamation-triangle-fill');
      });
  }

  function saveBatch() {
    var body = {
      batchName: document.getElementById('batchNameInput').value.trim() || null,
      initialChickenCount: document.getElementById('batchChickenCountInput').value.trim() || null,
      cycleDurationDays: document.getElementById('batchCycleDurationInput').value.trim() || null
    };
    if (body.initialChickenCount) body.initialChickenCount = Number(body.initialChickenCount);
    if (body.cycleDurationDays) body.cycleDurationDays = Number(body.cycleDurationDays);

    var saveBtn = document.getElementById('saveBatchBtn');
    saveBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/batches', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
      .then(function (response) {
        if (!response.ok) throw new Error('save failed');
        if (batchModal) batchModal.hide();
        document.getElementById('batchForm').reset();
        loadBatches();
        window.DashboardUI.toast('Batch ဖန်တီးပြီးပါပြီ။', 'Batch created.');
      })
      .catch(function () {
        window.DashboardUI.toast('Batch ဖန်တီး၍ မရပါ။', 'Could not create batch.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function submitPaymentRequest() {
    var batchId = document.getElementById('paymentBatchSelect').value;
    var screenshotFile = document.getElementById('paymentScreenshotInput').files[0];
    if (!batchId || !screenshotFile) {
      window.DashboardUI.toast('Batch နှင့် ဓာတ်ပုံ ဖြည့်စွက်ပါ။', 'Batch and screenshot image are required.', 'bi-exclamation-triangle-fill');
      return;
    }

    var formData = new FormData();
    formData.append('batchId', batchId);
    formData.append('paymentType', document.getElementById('paymentTypeSelect').value);
    formData.append('screenshot', screenshotFile);

    var submitBtn = document.getElementById('submitPaymentRequestBtn');
    submitBtn.disabled = true;

    // No Content-Type header here -- the browser sets the multipart
    // boundary itself when the body is a FormData instance.
    window.DashboardUI.authFetch('/api/v1/payment-transactions', {
      method: 'POST',
      body: formData
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not submit request.' : 'တောင်းဆိုမှု တင်ပြ၍ မရပါ။');
          window.DashboardUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        document.getElementById('paymentScreenshotInput').value = '';
        loadPaymentRequests();
        window.DashboardUI.toast('တောင်းဆိုမှု တင်ပြပြီးပါပြီ။', 'Payment request submitted.');
      })
      .catch(function () {
        window.DashboardUI.toast('တောင်းဆိုမှု တင်ပြ၍ မရပါ။', 'Could not submit request.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        submitBtn.disabled = false;
      });
  }

  function showFarmerOnlyNotice() {
    var content = document.getElementById('premiumContent');
    content.innerHTML = '<div class="card empty-state">' +
      '<i class="bi bi-lock-fill"></i>' +
      '<p>' + (lang() === 'en' ? 'Premium & Payments is available to farmer accounts only.' : 'Premium နှင့် ငွေပေးချေမှုကို တောင်သူအကောင့်များသာ အသုံးပြုနိုင်ပါသည်။') + '</p>' +
      '</div>';
  }

  document.addEventListener('DOMContentLoaded', function () {
    var session = window.DashboardUI && window.DashboardUI.getSession();
    if (!session || !session.user || session.user.accountType !== 'FARMER') {
      showFarmerOnlyNotice();
      return;
    }

    loadBatches().then(loadPaymentRequests);

    document.getElementById('addBatchBtn').addEventListener('click', function () {
      document.getElementById('batchForm').reset();
      if (batchModal) batchModal.show();
    });
    document.getElementById('saveBatchBtn').addEventListener('click', saveBatch);
    document.getElementById('submitPaymentRequestBtn').addEventListener('click', submitPaymentRequest);

    document.addEventListener('dashboardlangchange', function () {
      renderBatches();
      renderPaymentRequests();
    });
  });
})();
