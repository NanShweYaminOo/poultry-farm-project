/* ==========================================================================
   Broiler Farming System — User Dashboard
   Medicine & Vaccination Alarms (alarms.jsp) — create (/api/v1/batch-alarms)
   with a friendly schedule picker that builds the Quartz 6-field cron
   expression the backend actually requires (seconds field included, e.g.
   "0 0 8 * * ?"), list/cancel per batch, and the "Mark as Done" flow which
   lives under /api/v1/medicine-estimation (estimate, then complete) since
   that's what the backend contract actually wires it to. Entire
   /api/v1/batch-alarms and /api/v1/medicine-estimation paths are
   PAID/ADMIN-only.
   ========================================================================== */

(function () {
  var activeBatches = [];
  var selectedBatchId = null;
  var alarms = [];
  var cancelTargetAlarmId = null;
  var completeTargetAlarmId = null;

  var alarmModalEl = document.getElementById('alarmModal');
  var alarmModal = alarmModalEl ? new bootstrap.Modal(alarmModalEl) : null;
  var cancelAlarmModalEl = document.getElementById('cancelAlarmModal');
  var cancelAlarmModal = cancelAlarmModalEl ? new bootstrap.Modal(cancelAlarmModalEl) : null;
  var completeAlarmModalEl = document.getElementById('completeAlarmModal');
  var completeAlarmModal = completeAlarmModalEl ? new bootstrap.Modal(completeAlarmModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function formatDateTime(iso) {
    if (!iso) return '-';
    var d = new Date(iso);
    return isNaN(d.getTime()) ? '-' : d.toLocaleString();
  }

  function showUpgradeNotice() {
    var content = document.getElementById('alarmsContent');
    content.innerHTML = '<div class="card empty-state">' +
      '<i class="bi bi-lock-fill"></i>' +
      '<p>' + (lang() === 'en'
        ? 'Alarms are available once you have an approved, active batch.'
        : 'Batch တစ်ခု အတည်ပြုပြီး လည်ပတ်နေမှသာ သတိပေးချက်များကို အသုံးပြုနိုင်ပါသည်။') + '</p>' +
      '</div>';
  }

  // Builds Quartz's 6/7-field cron syntax (seconds first) -- NOT standard
  // 5-field Unix cron. See CreateBatchAlarmRequest.cronExpression.
  function buildCronExpression() {
    var type = document.getElementById('alarmScheduleType').value;
    var time = document.getElementById('alarmTimeInput').value || '08:00';
    var parts = time.split(':');
    var hh = String(Number(parts[0]));
    var mm = String(Number(parts[1]));

    if (type === 'DAILY') {
      return '0 ' + mm + ' ' + hh + ' * * ?';
    }
    if (type === 'WEEKLY') {
      var weekday = document.getElementById('alarmWeekdaySelect').value;
      return '0 ' + mm + ' ' + hh + ' ? * ' + weekday;
    }
    if (type === 'INTERVAL') {
      var n = Number(document.getElementById('alarmIntervalInput').value || 3);
      return '0 ' + mm + ' ' + hh + ' */' + n + ' * ?';
    }
    if (type === 'ONCE') {
      var dateStr = document.getElementById('alarmDateInput').value;
      if (!dateStr) return null;
      var dateParts = dateStr.split('-');
      var year = dateParts[0], month = String(Number(dateParts[1])), day = String(Number(dateParts[2]));
      return '0 ' + mm + ' ' + hh + ' ' + day + ' ' + month + ' ? ' + year;
    }
    return null;
  }

  function updateScheduleFieldVisibility() {
    var type = document.getElementById('alarmScheduleType').value;
    document.getElementById('alarmWeekdayField').style.display = type === 'WEEKLY' ? '' : 'none';
    document.getElementById('alarmIntervalField').style.display = type === 'INTERVAL' ? '' : 'none';
    document.getElementById('alarmDateField').style.display = type === 'ONCE' ? '' : 'none';
  }

  function showAlarmFormError(message) {
    var box = document.getElementById('alarmFormAlert');
    box.querySelector('span').textContent = message;
    box.style.display = '';
  }

  function hideAlarmFormError() {
    document.getElementById('alarmFormAlert').style.display = 'none';
  }

  function renderAlarms() {
    var tbody = document.getElementById('alarmsTableBody');

    if (alarms.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No alarms for this batch yet.' : 'ဤ Batch အတွက် သတိပေးချက် မရှိသေးပါ။') + '</td></tr>';
      return;
    }

    tbody.innerHTML = alarms.map(function (a) {
      var statusBadge = a.isCompleted
        ? '<span class="badge badge-emerald">' + (lang() === 'en' ? 'Done' : 'ပြီးစီး') + '</span>'
        : '<span class="badge badge-gold">' + (lang() === 'en' ? 'Active' : 'လက်ရှိ') + '</span>';

      var actions;
      if (a.isCompleted) {
        actions = '<div style="font-size:11.5px;color:var(--muted);">' +
          (lang() === 'en' ? 'Used: ' : 'သုံးစွဲ: ') + (a.overriddenQuantity != null ? a.overriddenQuantity : '-') + ' &middot; ' +
          (lang() === 'en' ? 'Cost: ' : 'ကုန်ကျ: ') + (a.finalCostIncurred != null ? a.finalCostIncurred : '-') +
          '</div>';
      } else {
        actions = '<button class="btn btn-gold btn-sm" data-complete-alarm="' + a.id + '">' +
          '<i class="bi bi-check-lg"></i> ' + (lang() === 'en' ? 'Mark as Done' : 'ပြီးစီးကြောင်းသတ်မှတ်မည်') + '</button> ' +
          '<button class="btn btn-outline btn-sm" data-cancel-alarm="' + a.id + '">' +
          '<i class="bi bi-x-lg"></i> ' + (lang() === 'en' ? 'Cancel' : 'ပယ်ဖျက်မည်') + '</button>';
      }

      return '<tr>' +
        '<td>' + escapeHtml(a.medicineName) + '</td>' +
        '<td><code style="font-size:11.5px;">' + escapeHtml(a.cronExpression) + '</code></td>' +
        '<td>' + formatDateTime(a.scheduledTime) + '</td>' +
        '<td>' + statusBadge + '</td>' +
        '<td>' + actions + '</td>' +
        '</tr>';
    }).join('');

    tbody.querySelectorAll('[data-cancel-alarm]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        cancelTargetAlarmId = Number(btn.getAttribute('data-cancel-alarm'));
        if (cancelAlarmModal) cancelAlarmModal.show();
      });
    });
    tbody.querySelectorAll('[data-complete-alarm]').forEach(function (btn) {
      btn.addEventListener('click', function () {
        openCompleteModal(Number(btn.getAttribute('data-complete-alarm')));
      });
    });
  }

  function loadAlarms() {
    if (!selectedBatchId) return;
    window.DashboardUI.authFetch('/api/v1/batch-alarms/batches/' + selectedBatchId)
      .then(function (response) { return response.json(); })
      .then(function (data) {
        alarms = data;
        renderAlarms();
      })
      .catch(function () {
        window.DashboardUI.toast('သတိပေးချက်များ ရယူ၍မရပါ။', 'Could not load alarms.', 'bi-exclamation-triangle-fill');
      });
  }

  function populateBatchSelect() {
    var select = document.getElementById('alarmBatchSelect');
    var notice = document.getElementById('alarmsNoBatchNotice');
    var tableWrap = document.getElementById('alarmsTableWrap');
    var addBtn = document.getElementById('addAlarmBtn');

    if (activeBatches.length === 0) {
      select.style.display = 'none';
      addBtn.style.display = 'none';
      notice.style.display = '';
      tableWrap.style.display = 'none';
      return;
    }

    select.style.display = '';
    addBtn.style.display = '';
    notice.style.display = 'none';
    tableWrap.style.display = '';

    select.innerHTML = activeBatches.map(function (b) {
      return '<option value="' + b.id + '">' + escapeHtml(b.batchName || ('Batch #' + b.id)) + '</option>';
    }).join('');

    selectedBatchId = Number(select.value);
    loadAlarms();
  }

  function loadActiveBatches() {
    window.DashboardUI.authFetch('/api/v1/batches')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        activeBatches = (data || []).filter(function (b) { return b.status === 'ACTIVE'; });
        populateBatchSelect();
      })
      .catch(function () {
        window.DashboardUI.toast('Batch စာရင်း ရယူ၍မရပါ။', 'Could not load batches.', 'bi-exclamation-triangle-fill');
      });
  }

  function saveAlarm() {
    hideAlarmFormError();
    var medicineName = document.getElementById('alarmMedicineNameInput').value.trim();
    if (!medicineName) {
      showAlarmFormError(lang() === 'en' ? 'Medicine name is required.' : 'ဆေးဝါးအမည် ဖြည့်စွက်ပါ။');
      return;
    }
    var cronExpression = buildCronExpression();
    if (!cronExpression) {
      showAlarmFormError(lang() === 'en' ? 'Please choose a valid date.' : 'မှန်ကန်သော ရက်စွဲကို ရွေးချယ်ပါ။');
      return;
    }

    var saveBtn = document.getElementById('saveAlarmBtn');
    saveBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/batch-alarms', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ batchId: selectedBatchId, medicineName: medicineName, cronExpression: cronExpression })
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not create alarm.' : 'သတိပေးချက် ဖန်တီး၍ မရပါ။');
          showAlarmFormError(message);
          return;
        }
        if (alarmModal) alarmModal.hide();
        document.getElementById('alarmForm').reset();
        updateScheduleFieldVisibility();
        loadAlarms();
        window.DashboardUI.toast('သတိပေးချက် ဖန်တီးပြီးပါပြီ။', 'Alarm created.');
      })
      .catch(function () {
        showAlarmFormError(lang() === 'en' ? 'Could not create alarm.' : 'သတိပေးချက် ဖန်တီး၍ မရပါ။');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function cancelAlarm() {
    if (cancelTargetAlarmId === null) return;
    window.DashboardUI.authFetch('/api/v1/batch-alarms/' + cancelTargetAlarmId, { method: 'DELETE' })
      .then(function (response) {
        if (!response.ok) throw new Error('cancel failed');
        if (cancelAlarmModal) cancelAlarmModal.hide();
        cancelTargetAlarmId = null;
        loadAlarms();
        window.DashboardUI.toast('သတိပေးချက် ပယ်ဖျက်ပြီးပါပြီ။', 'Alarm cancelled.');
      })
      .catch(function () {
        window.DashboardUI.toast('သတိပေးချက် ပယ်ဖျက်၍မရပါ။', 'Could not cancel alarm.', 'bi-exclamation-triangle-fill');
      });
  }

  function showCompleteFormError(message) {
    var box = document.getElementById('completeAlarmFormAlert');
    box.querySelector('span').textContent = message;
    box.style.display = '';
  }

  function hideCompleteFormError() {
    document.getElementById('completeAlarmFormAlert').style.display = 'none';
  }

  function openCompleteModal(alarmId) {
    completeTargetAlarmId = alarmId;
    hideCompleteFormError();
    document.getElementById('completeAlarmForm').reset();
    document.getElementById('completeAlarmEstimateBox').innerHTML =
      '<span class="i18n" data-my="တွက်ချက်နေသည်..." data-en="Calculating...">' +
      (lang() === 'en' ? 'Calculating...' : 'တွက်ချက်နေသည်...') + '</span>';
    if (completeAlarmModal) completeAlarmModal.show();

    window.DashboardUI.authFetch('/api/v1/medicine-estimation/alarms/' + alarmId + '/estimate', { method: 'POST' })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not calculate an estimate.' : 'ခန့်မှန်းချက် တွက်ချက်၍ မရပါ။');
          document.getElementById('completeAlarmEstimateBox').innerHTML =
            '<span style="color:var(--danger);">' + escapeHtml(message) + '</span>';
          return;
        }
        var est = result.body;
        document.getElementById('completeAlarmEstimateBox').innerHTML =
          '<div><strong>' + escapeHtml(est.medicineName) + '</strong></div>' +
          '<div>' + (lang() === 'en' ? 'Calculated quantity: ' : 'တွက်ချက်ထားသောပမာဏ: ') +
          est.calculatedQuantity + ' ' + escapeHtml(est.unit) + '</div>' +
          '<div>' + (lang() === 'en' ? 'Estimated cost: ' : 'ခန့်မှန်းကုန်ကျငွေ: ') + est.estimatedCost + '</div>';
        document.getElementById('completeOverriddenQuantityInput').value = est.calculatedQuantity;
        document.getElementById('completeFinalCostInput').value = est.estimatedCost;
      })
      .catch(function () {
        document.getElementById('completeAlarmEstimateBox').innerHTML =
          '<span style="color:var(--danger);">' + (lang() === 'en' ? 'Could not calculate an estimate.' : 'ခန့်မှန်းချက် တွက်ချက်၍ မရပါ။') + '</span>';
      });
  }

  function confirmCompleteAlarm() {
    hideCompleteFormError();
    var quantityRaw = document.getElementById('completeOverriddenQuantityInput').value.trim();
    var costRaw = document.getElementById('completeFinalCostInput').value.trim();
    if (!quantityRaw || !costRaw) {
      showCompleteFormError(lang() === 'en'
        ? 'Actual quantity and cost are both required.'
        : 'အမှန်တကယ်ပမာဏနှင့် ကုန်ကျငွေ နှစ်ခုစလုံး ဖြည့်စွက်ပါ။');
      return;
    }

    var confirmBtn = document.getElementById('confirmCompleteAlarmBtn');
    confirmBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/medicine-estimation/alarms/complete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        batchAlarmId: completeTargetAlarmId,
        overriddenQuantity: Number(quantityRaw),
        finalCostIncurred: Number(costRaw),
        notes: document.getElementById('completeNotesInput').value.trim() || null
      })
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not complete this task.' : 'ဤလုပ်ငန်းကို ပြီးစီးကြောင်း သတ်မှတ်၍ မရပါ။');
          showCompleteFormError(message);
          return;
        }
        if (completeAlarmModal) completeAlarmModal.hide();
        completeTargetAlarmId = null;
        loadAlarms();
        window.DashboardUI.toast(
          'ပြီးစီးကြောင်း သတ်မှတ်ပြီးပါပြီ။ လက်ကျန်ပစ္စည်းမှ နုတ်ယူပြီးပါပြီ။',
          'Marked as done. Inventory has been deducted.');
      })
      .catch(function () {
        showCompleteFormError(lang() === 'en' ? 'Could not complete this task.' : 'ဤလုပ်ငန်းကို ပြီးစီးကြောင်း သတ်မှတ်၍ မရပါ။');
      })
      .finally(function () {
        confirmBtn.disabled = false;
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var session = window.DashboardUI && window.DashboardUI.getSession();
    if (!session || !session.user || (session.user.role !== 'PAID' && session.user.role !== 'ADMIN')) {
      showUpgradeNotice();
      return;
    }

    loadActiveBatches();

    document.getElementById('alarmBatchSelect').addEventListener('change', function (e) {
      selectedBatchId = Number(e.target.value);
      loadAlarms();
    });
    document.getElementById('addAlarmBtn').addEventListener('click', function () {
      document.getElementById('alarmForm').reset();
      hideAlarmFormError();
      updateScheduleFieldVisibility();
      if (alarmModal) alarmModal.show();
    });
    document.getElementById('alarmScheduleType').addEventListener('change', updateScheduleFieldVisibility);
    document.getElementById('saveAlarmBtn').addEventListener('click', saveAlarm);
    document.getElementById('confirmCancelAlarmBtn').addEventListener('click', cancelAlarm);
    document.getElementById('confirmCompleteAlarmBtn').addEventListener('click', confirmCompleteAlarm);

    document.addEventListener('dashboardlangchange', function () {
      renderAlarms();
    });
  });
})();
