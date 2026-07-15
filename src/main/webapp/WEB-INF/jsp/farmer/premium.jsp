<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Premium &amp; Payments | Broiler Farming System" />
<c:set var="currentPage" value="premium" />
<c:set var="topbarTitleMy" value="Premium နှင့် ငွေပေးချေမှု" />
<c:set var="topbarTitleEn" value="Premium &amp; Payments" />
<c:set var="topbarSubMy" value="Batch များနှင့် Premium/ပို့စ်ကြေး တောင်းဆိုမှုများ" />
<c:set var="topbarSubEn" value="Manage batches and Premium / posting-fee requests" />
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

    <main class="content" id="premiumContent">
      <!-- My Batches -->
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h3 class="h5 mb-0 i18n" data-my="ကျွန်ုပ်၏ Batch များ" data-en="My Batches">My Batches</h3>
        <button class="btn btn-gold" id="addBatchBtn">
          <i class="bi bi-plus-lg"></i>
          <span class="i18n" data-my="Batch အသစ်" data-en="New Batch">New Batch</span>
        </button>
      </div>
      <div class="row g-3 mb-4" id="batchesGrid">
        <div class="col-12 card text-center" style="padding:30px;color:var(--muted);">
          <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
        </div>
      </div>

      <!-- Request Payment -->
      <div class="card" style="padding:20px;margin-bottom:24px;">
        <h3 class="h5 mb-3 i18n" data-my="Premium / ပို့စ်ကြေး တောင်းဆိုမည်" data-en="Request Premium / Posting Fee">Request Premium / Posting Fee</h3>
        <form id="paymentRequestForm">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label i18n" data-my="Batch ရွေးပါ" data-en="Select Batch">Select Batch</label>
              <select class="form-select" id="paymentBatchSelect"></select>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="အမျိုးအစား" data-en="Request Type">Request Type</label>
              <select class="form-select" id="paymentTypeSelect">
                <option value="BATCH_REGISTRATION" class="i18n" data-my="Premium အဆင့်တင်ရန်" data-en="Premium Upgrade">Premium Upgrade</option>
                <option value="POSTING_EXTENSION" class="i18n" data-my="ပို့စ်တင်ချိန် တိုးရန် ကြေး" data-en="Posting Extension Fee">Posting Extension Fee</option>
              </select>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ငွေပေးချေမှု ဓာတ်ပုံ" data-en="Payment Screenshot">Payment Screenshot</label>
              <input type="file" class="form-control" id="paymentScreenshotInput" accept="image/*" required>
            </div>
          </div>
          <div id="paymentNoBatchNotice" class="empty-state" style="display:none;">
            <i class="bi bi-inbox"></i>
            <p class="i18n" data-my="ငွေပေးချေမှု တောင်းဆိုရန် အရင်ဆုံး Batch တစ်ခု ဖန်တီးပါ။" data-en="Create a batch first before requesting a payment.">Create a batch first before requesting a payment.</p>
          </div>
          <button type="button" class="btn btn-gold mt-3" id="submitPaymentRequestBtn">
            <i class="bi bi-send-fill"></i>
            <span class="i18n" data-my="တောင်းဆိုမည်" data-en="Submit Request">Submit Request</span>
          </button>
        </form>
      </div>

      <!-- My Payment Requests -->
      <h3 class="h5 mb-3 i18n" data-my="ကျွန်ုပ်၏ ငွေပေးချေမှု တောင်းဆိုချက်များ" data-en="My Payment Requests">My Payment Requests</h3>
      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="Batch" data-en="Batch">Batch</th>
              <th class="i18n" data-my="အမျိုးအစား" data-en="Type">Type</th>
              <th class="i18n" data-my="ဓာတ်ပုံ" data-en="Screenshot">Screenshot</th>
              <th class="i18n" data-my="အခြေအနေ" data-en="Status">Status</th>
              <th class="i18n" data-my="တင်ပြသည့်ရက်" data-en="Submitted">Submitted</th>
            </tr>
          </thead>
          <tbody id="paymentRequestsTableBody">
            <tr>
              <td colspan="5" class="text-center" style="padding:30px;color:var(--muted);">
                <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<!-- New batch modal -->
<div class="modal fade" id="batchModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3 i18n" data-my="Batch အသစ်" data-en="New Batch">New Batch</h3>
        <form id="batchForm">
          <div class="row g-3">
            <div class="col-md-12">
              <label class="form-label i18n" data-my="Batch အမည်" data-en="Batch Name">Batch Name</label>
              <input type="text" class="form-control" id="batchNameInput" placeholder="Coop A">
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="ကြက်အရေအတွက်" data-en="Initial Chicken Count">Initial Chicken Count</label>
              <input type="number" min="1" step="1" class="form-control" id="batchChickenCountInput">
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="ကာလ (ရက်)" data-en="Cycle Duration (days)">Cycle Duration (days)</label>
              <input type="number" min="1" step="1" class="form-control" id="batchCycleDurationInput">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">Cancel</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveBatchBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="ဖန်တီးမည်" data-en="Create">Create</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/premium-page.js'/>"></script>
</body>
</html>
