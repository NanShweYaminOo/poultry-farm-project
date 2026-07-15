/* ==========================================================================
   Broiler Farming System — User Dashboard
   Inventory (inventory.jsp) — per-batch stock list (/api/v1/inventory/batches/{id})
   and restock form (POST /api/v1/inventory/restock). Quantity can go negative
   (a "needs restock" signal) since completing a medicine alarm task
   auto-deducts the farmer's overridden amount -- see alarms-page.js's
   Mark-as-Done flow. Entire path is PAID/ADMIN-only.
   ========================================================================== */

(function () {
  var activeBatches = [];
  var selectedBatchId = null;
  var items = [];

  var restockModalEl = document.getElementById('restockModal');
  var restockModal = restockModalEl ? new bootstrap.Modal(restockModalEl) : null;

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
    var content = document.getElementById('inventoryContent');
    content.innerHTML = '<div class="card empty-state">' +
      '<i class="bi bi-lock-fill"></i>' +
      '<p>' + (lang() === 'en'
        ? 'Inventory is available once you have an approved, active batch.'
        : 'Batch တစ်ခု အတည်ပြုပြီး လည်ပတ်နေမှသာ လက်ကျန်ပစ္စည်းကို အသုံးပြုနိုင်ပါသည်။') + '</p>' +
      '</div>';
  }

  function showFormError(message) {
    var box = document.getElementById('restockFormAlert');
    box.querySelector('span').textContent = message;
    box.style.display = '';
  }

  function hideFormError() {
    document.getElementById('restockFormAlert').style.display = 'none';
  }

  function renderItems() {
    var tbody = document.getElementById('inventoryTableBody');
    if (items.length === 0) {
      tbody.innerHTML = '<tr><td colspan="4" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No inventory items yet.' : 'လက်ကျန်ပစ္စည်း မရှိသေးပါ။') + '</td></tr>';
      return;
    }
    tbody.innerHTML = items.map(function (item) {
      var low = Number(item.quantityInStock) < 0;
      return '<tr>' +
        '<td>' + escapeHtml(item.itemName) + '</td>' +
        '<td>' + escapeHtml(item.unit) + '</td>' +
        '<td>' + (low
          ? '<span class="badge badge-danger-soft">' + item.quantityInStock + ' &middot; ' + (lang() === 'en' ? 'needs restock' : 'ဖြည့်ရန်လိုသည်') + '</span>'
          : item.quantityInStock) + '</td>' +
        '<td>' + formatDateTime(item.updatedAt) + '</td>' +
        '</tr>';
    }).join('');
  }

  function loadItems() {
    if (!selectedBatchId) return;
    window.DashboardUI.authFetch('/api/v1/inventory/batches/' + selectedBatchId)
      .then(function (response) { return response.json(); })
      .then(function (data) {
        items = data;
        renderItems();
      })
      .catch(function () {
        window.DashboardUI.toast('လက်ကျန်ပစ္စည်း ရယူ၍မရပါ။', 'Could not load inventory.', 'bi-exclamation-triangle-fill');
      });
  }

  function populateBatchSelect() {
    var select = document.getElementById('inventoryBatchSelect');
    var notice = document.getElementById('inventoryNoBatchNotice');
    var tableWrap = document.getElementById('inventoryTableWrap');
    var addBtn = document.getElementById('addRestockBtn');

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
    loadItems();
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

  function saveRestock() {
    hideFormError();
    var itemName = document.getElementById('restockItemNameInput').value.trim();
    var unit = document.getElementById('restockUnitInput').value.trim();
    var quantityRaw = document.getElementById('restockQuantityInput').value.trim();

    if (!itemName) {
      showFormError(lang() === 'en' ? 'Item name is required.' : 'ပစ္စည်းအမည် ဖြည့်စွက်ပါ။');
      return;
    }
    if (!quantityRaw || Number(quantityRaw) <= 0) {
      showFormError(lang() === 'en' ? 'Quantity must be greater than zero.' : 'ပမာဏသည် သုညထက်ကြီးရမည်။');
      return;
    }

    var saveBtn = document.getElementById('saveRestockBtn');
    saveBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/inventory/restock', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        batchId: selectedBatchId,
        itemName: itemName,
        unit: unit || null,
        quantity: Number(quantityRaw)
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
            (lang() === 'en' ? 'Could not restock item.' : 'ပစ္စည်းဖြည့်၍ မရပါ။');
          showFormError(message);
          return;
        }
        if (restockModal) restockModal.hide();
        document.getElementById('restockForm').reset();
        loadItems();
        window.DashboardUI.toast('ပစ္စည်းဖြည့်ပြီးပါပြီ။', 'Item restocked.');
      })
      .catch(function () {
        showFormError(lang() === 'en' ? 'Could not restock item.' : 'ပစ္စည်းဖြည့်၍ မရပါ။');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var session = window.DashboardUI && window.DashboardUI.getSession();
    if (!session || !session.user || (session.user.role !== 'PAID' && session.user.role !== 'ADMIN')) {
      showUpgradeNotice();
      return;
    }

    loadActiveBatches();

    document.getElementById('inventoryBatchSelect').addEventListener('change', function (e) {
      selectedBatchId = Number(e.target.value);
      loadItems();
    });
    document.getElementById('addRestockBtn').addEventListener('click', function () {
      document.getElementById('restockForm').reset();
      hideFormError();
      if (restockModal) restockModal.show();
    });
    document.getElementById('saveRestockBtn').addEventListener('click', saveRestock);

    document.addEventListener('dashboardlangchange', function () {
      renderItems();
    });
  });
})();
