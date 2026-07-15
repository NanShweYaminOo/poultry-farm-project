<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Daily Log | Broiler Farming System" />
<c:set var="currentPage" value="daily-log" />
<c:set var="topbarTitleMy" value="နေ့စဉ်မှတ်တမ်း" />
<c:set var="topbarTitleEn" value="Daily Log" />
<c:set var="topbarSubMy" value="နေ့စဉ် သေဆုံးအရေအတွက်နှင့် ကျန်ရှိကြက်အရေအတွက် မှတ်တမ်းတင်ပါ" />
<c:set var="topbarSubEn" value="Record today's mortality and remaining chicken count" />
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

    <main class="content" id="dailyLogContent">
      <div class="card mb-4" style="padding:20px;">
        <label class="form-label i18n" data-my="Batch ရွေးပါ" data-en="Select Batch">Select Batch</label>
        <select class="form-select" id="dailyLogBatchSelect" style="max-width:360px;"></select>
        <div id="dailyLogNoBatchNotice" class="empty-state" style="display:none;">
          <i class="bi bi-clipboard2-pulse"></i>
          <p class="i18n" data-my="လက်ရှိလည်ပတ်နေသော Batch မရှိသေးပါ။ Batch တစ်ခုစတင်ပြီးမှ နေ့စဉ်မှတ်တမ်းတင်နိုင်ပါသည်။" data-en="You have no running batch yet. Start a batch before recording daily logs.">
            You have no running batch yet. Start a batch before recording daily logs.
          </p>
        </div>
      </div>

      <div id="dailyLogBody" style="display:none;">
        <!-- Outstanding-today warning -->
        <div class="card mb-4" id="todayStatusBanner" style="padding:18px 20px;"></div>

        <div class="card mb-4" style="padding:20px;">
          <h3 class="h5 mb-3 i18n" data-my="ယနေ့မှတ်တမ်း တင်ရန်" data-en="Submit Today's Log">Submit Today's Log</h3>
          <div id="dailyLogFormAlert" class="login-alert" style="display:none;margin-bottom:14px;">
            <i class="bi bi-exclamation-triangle-fill"></i><span></span>
          </div>
          <form id="dailyLogForm">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label i18n" data-my="ယနေ့ သေဆုံးအရေအတွက်" data-en="Today's Mortality Count">Today's Mortality Count</label>
                <input type="number" min="0" step="1" class="form-control" id="dailyMortalityInput">
              </div>
              <div class="col-md-6">
                <label class="form-label i18n" data-my="ကျန်ရှိကြက် စုစုပေါင်း" data-en="Total Remaining Chicken Count">Total Remaining Chicken Count</label>
                <input type="number" min="0" step="1" class="form-control" id="dailyRemainingInput">
              </div>
            </div>
            <button type="button" class="btn btn-gold mt-3" id="submitDailyLogBtn">
              <i class="bi bi-check-lg"></i>
              <span class="i18n" data-my="မှတ်တမ်းတင်မည်" data-en="Submit Log">Submit Log</span>
            </button>
          </form>
        </div>

        <h3 class="h5 mb-3 i18n" data-my="မှတ်တမ်းသမိုင်း" data-en="Log History">Log History</h3>
        <div class="card" style="padding:6px 4px;overflow-x:auto;">
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th class="i18n" data-my="ရက်စွဲ" data-en="Date">Date</th>
                <th class="i18n" data-my="သေဆုံးအရေအတွက်" data-en="Mortality">Mortality</th>
                <th class="i18n" data-my="ကျန်ရှိကြက်" data-en="Remaining">Remaining</th>
              </tr>
            </thead>
            <tbody id="dailyLogHistoryBody">
              <tr>
                <td colspan="3" class="text-center" style="padding:30px;color:var(--muted);">
                  <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/daily-log-page.js'/>"></script>
</body>
</html>
