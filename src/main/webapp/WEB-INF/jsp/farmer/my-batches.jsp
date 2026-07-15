<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Batches | Broiler Farming System" />
<c:set var="currentPage" value="my-batches" />
<c:set var="topbarTitleMy" value="ကျွန်ုပ်၏ Batch များ" />
<c:set var="topbarTitleEn" value="My Batches" />
<c:set var="topbarSubMy" value="Batch သက်တမ်းစီမံခန့်ခွဲမှု — ဖန်တီး၊ စတင်၊ ရပ်ဆိုင်းမည်" />
<c:set var="topbarSubEn" value="Manage your batch lifecycle — create, start, and stop" />
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

    <main class="content">
      <div class="stats-grid mb-4" id="batchStatsGrid">
        <div class="stat-card">
          <div class="stat-icon"><i class="bi bi-clipboard2-pulse"></i></div>
          <div class="stat-value" id="statTotalBatches">-</div>
          <div class="stat-label i18n" data-my="Batch စုစုပေါင်း" data-en="Total Batches">Total Batches</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon"><i class="bi bi-play-circle"></i></div>
          <div class="stat-value" id="statRunningBatches">-</div>
          <div class="stat-label i18n" data-my="လက်ရှိလည်ပတ်နေသည်" data-en="Running">Running</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon"><i class="bi bi-hourglass-split"></i></div>
          <div class="stat-value" id="statAwaitingBatches">-</div>
          <div class="stat-label i18n" data-my="အတည်ပြုရန်စောင့်ဆိုင်း" data-en="Awaiting Approval">Awaiting Approval</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon"><i class="bi bi-check2-circle"></i></div>
          <div class="stat-value" id="statFinishedBatches">-</div>
          <div class="stat-label i18n" data-my="ပြီးဆုံးသည်" data-en="Finished">Finished</div>
        </div>
      </div>

      <div class="d-flex justify-content-between align-items-center mb-3">
        <h3 class="h5 mb-0 i18n" data-my="Batch စာရင်း" data-en="Batches">Batches</h3>
        <button class="btn btn-gold" id="addBatchBtn">
          <i class="bi bi-plus-lg"></i>
          <span class="i18n" data-my="Batch အသစ်" data-en="New Batch">New Batch</span>
        </button>
      </div>
      <div class="row g-3" id="batchesGrid">
        <div class="col-12 card text-center" style="padding:30px;color:var(--muted);">
          <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
        </div>
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
              <input type="number" min="1" step="1" class="form-control" id="batchCycleDurationInput"
                     placeholder="" data-my-ph="မထည့်လျှင် စံပုံသေသုံးမည်" data-en-ph="Leave blank to use the default">
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

<!-- Stop batch modal -->
<div class="modal fade" id="stopBatchModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body p-4 text-center">
        <div class="modal-icon"><i class="bi bi-stop-circle"></i></div>
        <h3 class="h5 mb-2 i18n" data-my="Batch ရပ်ဆိုင်းမည်" data-en="Stop Batch">Stop Batch</h3>
        <p class="i18n" data-my="ဤ Batch ကို ပြီးဆုံးအောင် အောင်မြင်စွာ ပြီးဆုံးကြောင်း သတ်မှတ်လိုပါသလား၊ သို့မဟုတ် ပယ်ဖျက်လိုပါသလား။" data-en="Do you want to finish this batch successfully, or cancel it?">
          Do you want to finish this batch successfully, or cancel it?
        </p>
      </div>
      <div class="modal-footer justify-content-center">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Never mind">Never mind</span>
        </button>
        <button type="button" class="btn btn-outline" id="confirmCancelBatchBtn">
          <i class="bi bi-x-lg"></i>
          <span class="i18n" data-my="ပယ်ဖျက်မည်" data-en="Cancel Batch">Cancel Batch</span>
        </button>
        <button type="button" class="btn btn-gold" id="confirmCompleteBatchBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="ပြီးဆုံးကြောင်းသတ်မှတ်မည်" data-en="Mark Completed">Mark Completed</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/my-batches-page.js'/>"></script>
</body>
</html>
