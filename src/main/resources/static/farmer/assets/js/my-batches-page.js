/* ==========================================================================
   Broiler Farming System — User Dashboard
   My Batches (my-batches.jsp) — full batch lifecycle: create
   (/api/v1/batches), start/stop (/api/v1/batches/{id}/start|stop). Approval
   state isn't on BatchResponse itself -- it's derived by cross-referencing
   the farmer's own BATCH_REGISTRATION payment transactions
   (/api/v1/payment-transactions/me), same rule PaymentTransactionServiceImpl
   uses server-side to set batch.adminApprovedAt.
   ========================================================================== */

(function () {
  var batches = [];
  var paymentsByBatchId = {}; // batchId -> latest BATCH_REGISTRATION PaymentTransactionResponse
  var stopTargetBatchId = null;

  var batchModalEl = document.getElementById('batchModal');
  var batchModal = batchModalEl ? new bootstrap.Modal(batchModalEl) : null;
  var stopBatchModalEl = document.getElementById('stopBatchModal');
  var stopBatchModal = stopBatchModalEl ? new bootstrap.Modal(stopBatchModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function daysBetween(fromIso, toDate) {
    var from = new Date(fromIso + 'T00:00:00');
    if (isNaN(from.getTime())) return null;
    var ms = toDate.setHours(0, 0, 0, 0) - from.setHours(0, 0, 0, 0);
    return Math.floor(ms / 86400000);
  }

  // Derives the lifecycle state a plain BatchResponse can't express on its
  // own: COMPLETED/CANCELLED win outright; otherwise isStarted vs the most
  // recent BATCH_REGISTRATION payment's status decides Running / Approved /
  // Awaiting Approval / Rejected / Not Submitted.
  function deriveState(batch) {
    if (batch.status === 'COMPLETED') return 'COMPLETED';
    if (batch.status === 'CANCELLED') return 'CANCELLED';
    if (batch.isStarted) return 'RUNNING';
    var payment = paymentsByBatchId[batch.id];
    if (payment && payment.status === 'APPROVED') return 'APPROVED';
    if (payment && payment.status === 'PENDING') return 'AWAITING_APPROVAL';
    if (payment && payment.status === 'REJECTED') return 'REJECTED';
    return 'NOT_SUBMITTED';
  }

  var STATE_BADGE = {
    RUNNING: 'badge-emerald',
    APPROVED: 'badge-gold',
    AWAITING_APPROVAL: 'badge-muted',
    REJECTED: 'badge-danger-soft',
    NOT_SUBMITTED: 'badge-muted',
    COMPLETED: 'badge-emerald',
    CANCELLED: 'badge-danger-soft'
  };

  var STATE_LABEL = {
    RUNNING: { my: 'လည်ပတ်နေသည်', en: 'Running' },
    APPROVED: { my: 'အတည်ပြုပြီး — စတင်နိုင်ပါပြီ', en: 'Approved — Ready to Start' },
    AWAITING_APPROVAL: { my: 'အတည်ပြုရန် စောင့်ဆိုင်းနေသည်', en: 'Awaiting Approval' },
    REJECTED: { my: 'ငွေပေးချေမှု ငြင်းပယ်ခံရသည်', en: 'Payment Rejected' },
    NOT_SUBMITTED: { my: 'ငွေပေးချေမှု မတင်ရသေးပါ', en: 'Payment Not Submitted' },
    COMPLETED: { my: 'ပြီးဆုံးပြီး', en: 'Completed' },
    CANCELLED: { my: 'ပယ်ဖျက်ပြီး', en: 'Cancelled' }
  };

  function statLabelText(state) {
    var l = STATE_LABEL[state] || { my: state, en: state };
    return lang() === 'en' ? l.en : l.my;
  }

  function renderStats() {
    var total = batches.length;
    var running = 0, awaiting = 0, finished = 0;
    batches.forEach(function (b) {
      var state = deriveState(b);
      if (state === 'RUNNING') running++;
      else if (state === 'APPROVED' || state === 'AWAITING_APPROVAL' || state === 'REJECTED' || state === 'NOT_SUBMITTED') awaiting++;
      else if (state === 'COMPLETED' || state === 'CANCELLED') finished++;
    });
    document.getElementById('statTotalBatches').textContent = total;
    document.getElementById('statRunningBatches').textContent = running;
    document.getElementById('statAwaitingBatches').textContent = awaiting;
    document.getElementById('statFinishedBatches').textContent = finished;
  }

  function renderCycleInfo(batch, state) {
    if (state !== 'RUNNING' || !batch.startDate || !batch.cycleDurationDays) return '';
    var elapsed = daysBetween(batch.startDate, new Date());
    if (elapsed === null) return '';
    var dayNumber = Math.max(1, elapsed + 1);
    var remaining = Math.max(0, batch.cycleDurationDays - elapsed);
    return '<div style="font-size:12.5px;color:var(--emerald-700);font-weight:600;margin-top:6px;">' +
      (lang() === 'en'
        ? 'Day ' + dayNumber + ' of ' + batch.cycleDurationDays + ' &middot; ' + remaining + ' day(s) remaining'
        : dayNumber + ' / ' + batch.cycleDurationDays + ' ရက်မြောက် &middot; ကျန် ' + remaining + ' ရက်') +
      '</div>';
  }

  function renderActionsAndNotice(batch, state) {
    if (state === 'APPROVED') {
      return '<div class="mt-2"><button class="btn btn-gold btn-sm" data-start-batch="' + batch.id + '">' +
        '<i class="bi bi-play-fill"></i> ' + (lang() === 'en' ? 'Start Batch' : 'Batch စတင်မည်') + '</button></div>';
    }
    if (state === 'RUNNING') {
      return '<div class="mt-2"><button class="btn btn-outline btn-sm" data-stop-batch="' + batch.id + '">' +
        '<i class="bi bi-stop-fill"></i> ' + (lang() === 'en' ? 'Stop Batch' : 'Batch ရပ်ဆိုင်းမည်') + '</button></div>';
    }
    if (state === 'AWAITING_APPROVAL') {
      return '<div class="mt-2" style="font-size:12px;color:var(--muted);">' +
        (lang() === 'en' ? 'Your payment is awaiting admin review.' : 'သင့်ငွေပေးချေမှုကို အက်ဒမင်စစ်ဆေးနေဆဲဖြစ်ပါသည်။') + '</div>';
    }
    if (state === 'REJECTED' || state === 'NOT_SUBMITTED') {
      var msg = state === 'REJECTED'
        ? (lang() === 'en' ? 'Resubmit your payment on the ' : 'ငွေပေးချေမှုကို ')
        : (lang() === 'en' ? 'Submit a payment on the ' : 'ငွေပေးချေမှုကို ');
      var linkText = lang() === 'en' ? 'Premium & Payments' : 'Premium နှင့် ငွေပေးချေမှု';
      var tail = state === 'REJECTED'
        ? (lang() === 'en' ? ' page.' : ' စာမျက်နှာတွင် ပြန်တင်ပါ။')
        : (lang() === 'en' ? ' page to unlock Start.' : ' စာမျက်နှာတွင် တင်ပါ။');
      return '<div class="mt-2" style="font-size:12px;color:var(--muted);">' + msg +
        '<a href="/farmer/premium">' + linkText + '</a>' + tail + '</div>';
    }
    return '';
  }

  function renderBatches() {
    var grid = document.getElementById('batchesGrid');

    if (batches.length === 0) {
      grid.innerHTML = '<div class="col-12 empty-state">' +
        '<i class="bi bi-clipboard2-pulse"></i>' +
        '<p>' + (lang() === 'en' ? 'No batches yet. Create one to get started.' : 'Batch မရှိသေးပါ။ စတင်ရန် Batch တစ်ခု ဖန်တီးပါ။') + '</p>' +
        '</div>';
      return;
    }

    grid.innerHTML = batches.map(function (b) {
      var state = deriveState(b);
      return '<div class="col-md-6 col-lg-4">' +
        '<div class="card" style="padding:16px;height:100%;">' +
        '<div style="font-weight:700;color:var(--emerald-900);">' + escapeHtml(b.batchName || ('Batch #' + b.id)) + '</div>' +
        '<div style="font-size:12.5px;color:var(--muted);margin-top:4px;">' +
        (lang() === 'en' ? 'Chickens: ' : 'ကြက်အရေအတွက်: ') + (b.initialChickenCount != null ? b.initialChickenCount : '-') +
        '</div>' +
        '<div style="font-size:12.5px;color:var(--muted);">' +
        (lang() === 'en' ? 'Cycle: ' : 'ကာလ: ') + (b.cycleDurationDays != null ? b.cycleDurationDays + (lang() === 'en' ? ' days' : ' ရက်') : '-') +
        '</div>' +
        '<div style="margin-top:8px;"><span class="badge ' + STATE_BADGE[state] + '">' + escapeHtml(statLabelText(state)) + '</span></div>' +
        renderCycleInfo(b, state) +
        renderActionsAndNotice(b, state) +
        '</div>' +
        '</div>';
    }).join('');

    grid.querySelectorAll('[data-start-batch]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        startBatch(Number(btn.getAttribute('data-start-batch')));
      });
    });
    grid.querySelectorAll('[data-stop-batch]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        stopTargetBatchId = Number(btn.getAttribute('data-stop-batch'));
        if (stopBatchModal) stopBatchModal.show();
      });
    });
  }

  function loadPaymentTransactions() {
    return window.DashboardUI.authFetch('/api/v1/payment-transactions/me')
      .then(function (response) { return response.ok ? response.json() : []; })
      .then(function (data) {
        paymentsByBatchId = {};
        (data || [])
          .filter(function (p) { return p.paymentType === 'BATCH_REGISTRATION'; })
          .forEach(function (p) {
            var existing = paymentsByBatchId[p.batchId];
            if (!existing || new Date(p.transactionTimestamp) > new Date(existing.transactionTimestamp)) {
              paymentsByBatchId[p.batchId] = p;
            }
          });
      })
      .catch(function () { paymentsByBatchId = {}; });
  }

  function loadBatches() {
    return window.DashboardUI.authFetch('/api/v1/batches')
      .then(function (response) { return response.json(); })
      .then(function (data) { batches = data; })
      .catch(function () {
        window.DashboardUI.toast('Batch စာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load batches.', 'bi-exclamation-triangle-fill');
      });
  }

  function refreshAll() {
    Promise.all([loadBatches(), loadPaymentTransactions()]).then(function () {
      renderStats();
      renderBatches();
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
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not create batch.' : 'Batch ဖန်တီး၍ မရပါ။');
          window.DashboardUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        if (batchModal) batchModal.hide();
        document.getElementById('batchForm').reset();
        refreshAll();
        window.DashboardUI.toast('Batch ဖန်တီးပြီးပါပြီ။', 'Batch created.');
      })
      .catch(function () {
        window.DashboardUI.toast('Batch ဖန်တီး၍ မရပါ။', 'Could not create batch.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function startBatch(batchId) {
    window.DashboardUI.authFetch('/api/v1/batches/' + batchId + '/start', { method: 'POST' })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not start batch.' : 'Batch စတင်၍ မရပါ။');
          window.DashboardUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        refreshAll();
        window.DashboardUI.toast('Batch စတင်ပြီးပါပြီ။', 'Batch started.');
      })
      .catch(function () {
        window.DashboardUI.toast('Batch စတင်၍ မရပါ။', 'Could not start batch.', 'bi-exclamation-triangle-fill');
      });
  }

  function stopBatch(finalStatus) {
    if (stopTargetBatchId === null) return;
    var batchId = stopTargetBatchId;

    window.DashboardUI.authFetch('/api/v1/batches/' + batchId + '/stop', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ finalStatus: finalStatus })
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not stop batch.' : 'Batch ရပ်ဆိုင်း၍ မရပါ။');
          window.DashboardUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        if (stopBatchModal) stopBatchModal.hide();
        stopTargetBatchId = null;
        refreshAll();
        window.DashboardUI.toast('Batch ရပ်ဆိုင်းပြီးပါပြီ။', 'Batch stopped.');
      })
      .catch(function () {
        window.DashboardUI.toast('Batch ရပ်ဆိုင်း၍ မရပါ။', 'Could not stop batch.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    refreshAll();

    document.getElementById('addBatchBtn').addEventListener('click', function () {
      document.getElementById('batchForm').reset();
      if (batchModal) batchModal.show();
    });
    document.getElementById('saveBatchBtn').addEventListener('click', saveBatch);
    document.getElementById('confirmCompleteBatchBtn').addEventListener('click', function () { stopBatch('COMPLETED'); });
    document.getElementById('confirmCancelBatchBtn').addEventListener('click', function () { stopBatch('CANCELLED'); });

    document.addEventListener('dashboardlangchange', function () {
      renderStats();
      renderBatches();
    });
  });
})();
