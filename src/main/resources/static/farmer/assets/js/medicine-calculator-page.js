/* ==========================================================================
   Broiler Farming System — User Dashboard
   Medicine Calculator (medicine-calculator.jsp) — ad-hoc estimate via
   POST /api/v1/medicine-estimation/estimate (not tied to any alarm, persists
   nothing server-side). The price-per-unit box recalculates the total cost
   purely client-side on every keystroke for instant feedback; only the
   initial "Calculate" click hits the server (to get calculatedQuantity and
   the admin's default price). Entire path is PAID/ADMIN-only.
   ========================================================================== */

(function () {
  var activeBatches = [];
  var lastEstimate = null; // holds calculatedQuantity/unit from the last server call

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function showUpgradeNotice() {
    var content = document.getElementById('medCalcContent');
    content.innerHTML = '<div class="card empty-state">' +
      '<i class="bi bi-lock-fill"></i>' +
      '<p>' + (lang() === 'en'
        ? 'The medicine calculator is available once you have an approved, active batch.'
        : 'Batch တစ်ခု အတည်ပြုပြီး လည်ပတ်နေမှသာ ဆေးဝါးတွက်ချက်စက်ကို အသုံးပြုနိုင်ပါသည်။') + '</p>' +
      '</div>';
  }

  function showFormError(message) {
    var box = document.getElementById('medCalcFormAlert');
    box.querySelector('span').textContent = message;
    box.style.display = '';
  }

  function hideFormError() {
    document.getElementById('medCalcFormAlert').style.display = 'none';
  }

  function recomputeTotalCost() {
    if (!lastEstimate) return;
    var price = Number(document.getElementById('medCalcPriceInput').value || 0);
    var total = lastEstimate.calculatedQuantity * price;
    document.getElementById('medCalcTotalCost').textContent =
      (isFinite(total) ? total.toFixed(2) : '0.00');
  }

  function populateBatchSelect() {
    var select = document.getElementById('medCalcBatchSelect');
    var notice = document.getElementById('medCalcNoBatchNotice');

    if (activeBatches.length === 0) {
      select.style.display = 'none';
      notice.style.display = '';
      return;
    }
    select.style.display = '';
    notice.style.display = 'none';
    select.innerHTML = activeBatches.map(function (b) {
      return '<option value="' + b.id + '">' + escapeHtml(b.batchName || ('Batch #' + b.id)) + '</option>';
    }).join('');
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

  function calculate() {
    hideFormError();
    document.getElementById('medCalcResultCard').style.display = 'none';
    lastEstimate = null;

    var batchId = Number(document.getElementById('medCalcBatchSelect').value);
    var medicineName = document.getElementById('medCalcMedicineNameInput').value.trim();
    if (!batchId) {
      showFormError(lang() === 'en' ? 'Select a batch first.' : 'Batch တစ်ခု ရွေးချယ်ပါ။');
      return;
    }
    if (!medicineName) {
      showFormError(lang() === 'en' ? 'Medicine name is required.' : 'ဆေးဝါးအမည် ဖြည့်စွက်ပါ။');
      return;
    }

    var calcBtn = document.getElementById('medCalcCalculateBtn');
    calcBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/medicine-estimation/estimate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ batchId: batchId, medicineName: medicineName })
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (respBody) {
          return { ok: response.ok, body: respBody };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not calculate an estimate.' : 'ခန့်မှန်းချက် တွက်ချက်၍ မရပါ။');
          showFormError(message);
          return;
        }
        lastEstimate = result.body;
        document.getElementById('medCalcRemainingCount').textContent = lastEstimate.remainingChickenCount;
        document.getElementById('medCalcDosagePerBird').textContent = lastEstimate.dosagePerBird + ' ' + lastEstimate.unit;
        document.getElementById('medCalcCalculatedQuantity').textContent = lastEstimate.calculatedQuantity + ' ' + lastEstimate.unit;
        document.getElementById('medCalcPriceInput').value = lastEstimate.pricePerUnitUsed;
        document.getElementById('medCalcDefaultPriceNote').textContent =
          (lang() === 'en' ? "Admin's default price: " : 'အက်ဒမင်၏ မူလစျေးနှုန်း: ') + lastEstimate.pricePerUnitUsed;
        document.getElementById('medCalcResultCard').style.display = '';
        recomputeTotalCost();
      })
      .catch(function () {
        showFormError(lang() === 'en' ? 'Could not calculate an estimate.' : 'ခန့်မှန်းချက် တွက်ချက်၍ မရပါ။');
      })
      .finally(function () {
        calcBtn.disabled = false;
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var session = window.DashboardUI && window.DashboardUI.getSession();
    if (!session || !session.user || (session.user.role !== 'PAID' && session.user.role !== 'ADMIN')) {
      showUpgradeNotice();
      return;
    }

    loadActiveBatches();

    document.getElementById('medCalcCalculateBtn').addEventListener('click', calculate);
    document.getElementById('medCalcPriceInput').addEventListener('input', recomputeTotalCost);
  });
})();
