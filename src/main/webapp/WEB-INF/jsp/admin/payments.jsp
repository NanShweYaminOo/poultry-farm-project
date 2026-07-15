<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="ငွေပေးချေမှုများ | Broiler Admin" />
<c:set var="currentPage" value="payments" />
<c:set var="topbarTitleMy" value="ငွေပေးချေမှုများ" />
<c:set var="topbarTitleEn" value="Payments" />
<c:set var="topbarSubMy" value="Premium အဆင့်တင်ရန်နှင့် ပို့စ်တင်ချိန်တိုးရန် ငွေပေးချေမှု တောင်းဆိုချက်များကို သုံးသပ်ရန်" />
<c:set var="topbarSubEn" value="Review Premium upgrade and posting-extension payment requests" />
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
      <div class="toolbar">
        <div class="filter-tabs" id="paymentsFilterTabs">
          <button class="active" data-filter="all"><span class="i18n" data-my="အားလုံး" data-en="All">All</span></button>
          <button data-filter="PENDING"><span class="i18n" data-my="ဆိုင်းငံ့ထား" data-en="Pending">Pending</span></button>
          <button data-filter="APPROVED"><span class="i18n" data-my="အတည်ပြုပြီး" data-en="Approved">Approved</span></button>
          <button data-filter="REJECTED"><span class="i18n" data-my="ငြင်းပယ်ပြီး" data-en="Rejected">Rejected</span></button>
        </div>
        <span class="results-count" id="paymentsResultsCount"></span>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="တောင်သူ" data-en="Farmer">Farmer</th>
              <th class="i18n" data-my="Batch" data-en="Batch">Batch</th>
              <th class="i18n" data-my="အမျိုးအစား" data-en="Type">Type</th>
              <th class="i18n" data-my="ဓာတ်ပုံ" data-en="Screenshot">Screenshot</th>
              <th class="i18n" data-my="အခြေအနေ" data-en="Status">Status</th>
              <th class="i18n" data-my="တင်ပြသည့်ရက်" data-en="Submitted">Submitted</th>
              <th class="text-end i18n" data-my="လုပ်ဆောင်ချက်" data-en="Actions">Actions</th>
            </tr>
          </thead>
          <tbody id="paymentsTableBody"></tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/payments-page.js'/>"></script>
</body>
</html>
