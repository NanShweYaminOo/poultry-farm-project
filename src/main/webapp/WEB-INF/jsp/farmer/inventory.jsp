<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inventory | Broiler Farming System" />
<c:set var="currentPage" value="inventory" />
<c:set var="topbarTitleMy" value="လက်ကျန်ပစ္စည်း" />
<c:set var="topbarTitleEn" value="Inventory" />
<c:set var="topbarSubMy" value="Batch အလိုက် ဆေးဝါး/ပစ္စည်း လက်ကျန်စာရင်း" />
<c:set var="topbarSubEn" value="Per-batch medicine and supply stock" />
<!DOCTYPE html>
<html lang="my" data-lang="my">
<head>
<%@ include file="fragments/head.jspf" %>
</head>
<body>
<div class="admin-shell">
  <%@ include file="fragments/sidebar.jspf" %>

  <div class="main">
    <%@ include file="fragments/topbar.jspf" %>

    <main class="content" id="inventoryContent">
      <div class="card mb-4" style="padding:20px;">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div style="min-width:260px;">
            <label class="form-label i18n" data-my="Batch ရွေးပါ" data-en="Select Batch">Select Batch</label>
            <select class="form-select" id="inventoryBatchSelect" style="max-width:360px;"></select>
          </div>
          <button class="btn btn-gold" id="addRestockBtn">
            <i class="bi bi-plus-lg"></i>
            <span class="i18n" data-my="ပစ္စည်းဖြည့်မည်" data-en="Restock Item">Restock Item</span>
          </button>
        </div>
        <div id="inventoryNoBatchNotice" class="empty-state" style="display:none;">
          <i class="bi bi-clipboard2-pulse"></i>
          <p class="i18n" data-my="လက်ရှိလည်ပတ်နေသော Batch မရှိသေးပါ။" data-en="You have no active batch yet.">You have no active batch yet.</p>
        </div>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;" id="inventoryTableWrap">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="ပစ္စည်းအမည်" data-en="Item">Item</th>
              <th class="i18n" data-my="ယူနစ်" data-en="Unit">Unit</th>
              <th class="i18n" data-my="လက်ကျန်ပမာဏ" data-en="Quantity In Stock">Quantity In Stock</th>
              <th class="i18n" data-my="နောက်ဆုံးပြင်ဆင်ချိန်" data-en="Last Updated">Last Updated</th>
            </tr>
          </thead>
          <tbody id="inventoryTableBody">
            <tr>
              <td colspan="4" class="text-center" style="padding:30px;color:var(--muted);">
                <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<!-- Restock modal -->
<div class="modal fade" id="restockModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3 i18n" data-my="ပစ္စည်းဖြည့်မည်" data-en="Restock Item">Restock Item</h3>
        <div id="restockFormAlert" class="login-alert" style="display:none;margin-bottom:14px;">
          <i class="bi bi-exclamation-triangle-fill"></i><span></span>
        </div>
        <form id="restockForm">
          <div class="row g-3">
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ပစ္စည်းအမည်" data-en="Item Name">Item Name</label>
              <input type="text" class="form-control" id="restockItemNameInput" placeholder="e.g. Amoxicillin">
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="ယူနစ်" data-en="Unit">Unit</label>
              <input type="text" class="form-control" id="restockUnitInput" placeholder="e.g. bottle, kg, ml">
              <div style="font-size:11px;color:var(--muted);margin-top:4px;" class="i18n"
                   data-my="ပစ္စည်းအသစ်ဖန်တီးရာတွင်သာ အသုံးပြုသည်။" data-en="Only used the first time this item is created.">
                Only used the first time this item is created.
              </div>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="ဖြည့်မည့်ပမာဏ" data-en="Quantity to Add">Quantity to Add</label>
              <input type="number" min="0.01" step="0.01" class="form-control" id="restockQuantityInput">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">Cancel</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveRestockBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="ဖြည့်မည်" data-en="Restock">Restock</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/inventory-page.js'/>"></script>
</body>
</html>
