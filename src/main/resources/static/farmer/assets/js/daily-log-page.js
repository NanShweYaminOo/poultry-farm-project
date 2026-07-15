/* ==========================================================================
   Broiler Farming System — User Dashboard
   Daily Log (daily-log.jsp) — the single most important farmer screen.
   Submits today's mortality/remaining count (/api/v1/daily-logs), shows
   history (/api/v1/daily-logs/batch/{id}), and a prominent outstanding-today
   warning (/api/v1/daily-logs/batch/{id}/today/status) ahead of the 18:00
   SMS reminder. Entire /api/v1/daily-logs path is PAID/ADMIN-only.
   ========================================================================== */

(function () {
  var startedBatches = [];
  var selectedBatchId = null;
  var history = [];

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function formatDate(isoDate) {
    if (!isoDate) return '-';
    var d = new Date(isoDate + 'T00:00:00');
    return isNaN(d.getTime()) ? isoDate : d.toLocaleDateString();
  }

  function showFormError(message) {
    var box = document.getElementById('dailyLogFormAlert');
    box.querySelector('span').textContent = message;
    box.style.display = '';
  }

  function hideFormError() {
    document.getElementById('dailyLogFormAlert').style.display = 'none';
  }

  function showUpgradeNotice() {
    var content = document.getElementById('dailyLogContent');
    content.innerHTML = '<div class="card empty-state">' +
      '<i class="bi bi-lock-fill"></i>' +
      '<p>' + (lang() === 'en'
        ? 'Daily Log is available once you have an approved, active batch.'
        : 'Batch တစ်ခု အတည်ပြုပြီး လည်ပတ်နေမှသာ နေ့စဉ်မှတ်တမ်းကို အသုံးပြုနိုင်ပါသည်။') + '</p>' +
      '</div>';
  }

  function renderTodayBanner(status) {
    var banner = document.getElementById('todayStatusBanner');
    if (status.submitted) {
      banner.innerHTML =
        '<div style="display:flex;align-items:center;gap:12px;">' +
        '<i class="bi bi-check-circle-fill" style="font-size:22px;color:var(--emerald-600);"></i>' +
        '<div>' +
        '<div style="font-weight:700;color:var(--emerald-900);">' +
        (lang() === 'en' ? "Today's log has been filed." : 'ယနေ့မှတ်တမ်း တင်ပြီးပါပြီ။') + '</div>' +
        '<div style="font-size:12.5px;color:var(--muted);">' +
        (lang() === 'en' ? 'Mortality: ' : 'သေဆုံး: ') + status.log.dailyMortalityCount + ' &middot; ' +
        (lang() === 'en' ? 'Remaining: ' : 'ကျန်ရှိ: ') + status.log.totalRemainingChickenCount +
        '</div></div></div>';
      banner.style.borderLeft = '4px solid var(--emerald-600)';
    } else {
      banner.innerHTML =
        '<div style="display:flex;align-items:center;gap:12px;">' +
        '<i class="bi bi-exclamation-triangle-fill" style="font-size:22px;color:var(--gold-700);"></i>' +
        '<div>' +
        '<div style="font-weight:700;color:var(--gold-700);">' +
        (lang() === 'en' ? "Today's log is still outstanding!" : 'ယနေ့မှတ်တမ်း မတင်ရသေးပါ!') + '</div>' +
        '<div style="font-size:12.5px;color:var(--muted);">' +
        (lang() === 'en'
          ? 'An SMS reminder will be sent at 18:00 if this is not filed.'
          : 'ဤမှတ်တမ်းကို မတင်ပါက ညနေ ၆ နာရီတွင် SMS သတိပေးချက် ပို့မည်ဖြစ်သည်။') +
        '</div></div></div>';
      banner.style.borderLeft = '4px solid var(--gold-500)';
    }
  }

  function renderHistory() {
    var tbody = document.getElementById('dailyLogHistoryBody');
    if (history.length === 0) {
      tbody.innerHTML = '<tr><td colspan="3" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No daily logs yet.' : 'နေ့စဉ်မှတ်တမ်း မရှိသေးပါ။') + '</td></tr>';
      return;
    }
    tbody.innerHTML = history.map(function (log) {
      return '<tr>' +
        '<td>' + formatDate(log.logDate) + '</td>' +
        '<td>' + escapeHtml(log.dailyMortalityCount) + '</td>' +
        '<td>' + escapeHtml(log.totalRemainingChickenCount) + '</td>' +
        '</tr>';
    }).join('');
  }

  function loadTodayStatus() {
    return window.DashboardUI.authFetch('/api/v1/daily-logs/batch/' + selectedBatchId + '/today/status')
      .then(function (response) { return response.json(); })
      .then(function (status) {
        renderTodayBanner(status);
        var submitBtn = document.getElementById('submitDailyLogBtn');
        submitBtn.disabled = status.submitted;
        submitBtn.style.opacity = status.submitted ? '0.5' : '1';
      })
      .catch(function () {
        window.DashboardUI.toast('ယနေ့အခြေအနေ ရယူ၍မရပါ။', "Could not load today's status.", 'bi-exclamation-triangle-fill');
      });
  }

  function loadHistory() {
    return window.DashboardUI.authFetch('/api/v1/daily-logs/batch/' + selectedBatchId)
      .then(function (response) { return response.json(); })
      .then(function (data) {
        history = data;
        renderHistory();
      })
      .catch(function () {
        window.DashboardUI.toast('မှတ်တမ်းသမိုင်း ရယူ၍မရပါ။', 'Could not load log history.', 'bi-exclamation-triangle-fill');
      });
  }

  function refreshSelectedBatch() {
    if (!selectedBatchId) return;
    hideFormError();
    Promise.all([loadTodayStatus(), loadHistory()]);
  }

  function submitDailyLog() {
    hideFormError();
    var mortalityRaw = document.getElementById('dailyMortalityInput').value.trim();
    var remainingRaw = document.getElementById('dailyRemainingInput').value.trim();

    if (mortalityRaw === '' || remainingRaw === '') {
      showFormError(lang() === 'en'
        ? 'Both mortality count and remaining count are required.'
        : 'သေဆုံးအရေအတွက်နှင့် ကျန်ရှိကြက်အရေအတွက် နှစ်ခုစလုံး ဖြည့်စွက်ပါ။');
      return;
    }

    var body = {
      batchId: selectedBatchId,
      dailyMortalityCount: Number(mortalityRaw),
      totalRemainingChickenCount: Number(remainingRaw)
    };

    var submitBtn = document.getElementById('submitDailyLogBtn');
    submitBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/daily-logs', {
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
          // Surfaces the API's own message verbatim -- count-integrity
          // errors ("cannot exceed the previous recorded count of X"),
          // duplicate-date, and inactive-batch errors are all meaningful
          // as-is, no need to re-word them client-side.
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not submit log.' : 'မှတ်တမ်းတင်၍ မရပါ။');
          showFormError(message);
          submitBtn.disabled = false;
          return;
        }
        document.getElementById('dailyLogForm').reset();
        refreshSelectedBatch();
        window.DashboardUI.toast('ယနေ့မှတ်တမ်း တင်ပြီးပါပြီ။', "Today's log submitted.");
      })
      .catch(function () {
        showFormError(lang() === 'en' ? 'Could not submit log.' : 'မှတ်တမ်းတင်၍ မရပါ။');
        submitBtn.disabled = false;
      });
  }

  function populateBatchSelect() {
    var select = document.getElementById('dailyLogBatchSelect');
    var notice = document.getElementById('dailyLogNoBatchNotice');
    var body = document.getElementById('dailyLogBody');

    if (startedBatches.length === 0) {
      select.style.display = 'none';
      notice.style.display = '';
      body.style.display = 'none';
      return;
    }

    select.style.display = '';
    notice.style.display = 'none';
    body.style.display = '';

    select.innerHTML = startedBatches.map(function (b) {
      return '<option value="' + b.id + '">' + escapeHtml(b.batchName || ('Batch #' + b.id)) + '</option>';
    }).join('');

    selectedBatchId = Number(select.value);
    refreshSelectedBatch();
  }

  function loadStartedBatches() {
    window.DashboardUI.authFetch('/api/v1/batches')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        startedBatches = (data || []).filter(function (b) { return b.isStarted && b.status === 'ACTIVE'; });
        populateBatchSelect();
      })
      .catch(function () {
        window.DashboardUI.toast('Batch စာရင်း ရယူ၍မရပါ။', 'Could not load batches.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var session = window.DashboardUI && window.DashboardUI.getSession();
    if (!session || !session.user || (session.user.role !== 'PAID' && session.user.role !== 'ADMIN')) {
      showUpgradeNotice();
      return;
    }

    loadStartedBatches();

    document.getElementById('dailyLogBatchSelect').addEventListener('change', function (e) {
      selectedBatchId = Number(e.target.value);
      refreshSelectedBatch();
    });
    document.getElementById('submitDailyLogBtn').addEventListener('click', submitDailyLog);

    document.addEventListener('dashboardlangchange', function () {
      renderHistory();
      if (selectedBatchId) loadTodayStatus();
    });
  });
})();
