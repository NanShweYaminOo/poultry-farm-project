<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="အဆင့်တင်ခြင်း တောင်းဆိုချက်များ | Broiler Admin" />
<c:set var="currentPage" value="account-upgrades" />
<c:set var="topbarTitleMy" value="အဆင့်တင်ခြင်း တောင်းဆိုချက်များ" />
<c:set var="topbarTitleEn" value="Upgrade Requests" />
<c:set var="topbarSubMy" value="ဧည့်သည်များမှ တောင်သူအဖြစ် အဆင့်တင်ရန် တောင်းဆိုချက်များကို သုံးသပ်ရန်" />
<c:set var="topbarSubEn" value="Review Guest-to-Farmer upgrade requests" />
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
        <div class="filter-tabs" id="upgradesFilterTabs">
          <button class="active" data-filter="all"><span class="i18n" data-my="အားလုံး" data-en="All">All</span></button>
          <button data-filter="PENDING"><span class="i18n" data-my="ဆိုင်းငံ့ထား" data-en="Pending">Pending</span></button>
          <button data-filter="APPROVED"><span class="i18n" data-my="အတည်ပြုပြီး" data-en="Approved">Approved</span></button>
          <button data-filter="REJECTED"><span class="i18n" data-my="ငြင်းပယ်ပြီး" data-en="Rejected">Rejected</span></button>
        </div>
        <span class="results-count" id="upgradesResultsCount"></span>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="အသုံးပြုသူ" data-en="User">User</th>
              <th class="i18n" data-my="အကြောင်းရင်း" data-en="Reason">Reason</th>
              <th class="i18n" data-my="အခြေအနေ" data-en="Status">Status</th>
              <th class="i18n" data-my="တောင်းဆိုသည့်ရက်" data-en="Requested">Requested</th>
              <th class="text-end i18n" data-my="လုပ်ဆောင်ချက်" data-en="Actions">Actions</th>
            </tr>
          </thead>
          <tbody id="upgradesTableBody"></tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/account-upgrades-page.js'/>"></script>
</body>
</html>
