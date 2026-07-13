<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="အကြံပြုချက်များ | Broiler Admin" />
<c:set var="currentPage" value="feedback" />
<c:set var="topbarTitleMy" value="အကြံပြုချက်များ" />
<c:set var="topbarTitleEn" value="Feedback" />
<c:set var="topbarSubMy" value="အသုံးပြုသူများ၏ အကြံပြုချက် တင်ပြချက်များကို ကြည့်ရှုခြင်း" />
<c:set var="topbarSubEn" value="View feedback tickets submitted by users" />
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
        <div class="filter-tabs" id="feedbackFilterTabs">
          <button class="active" data-filter="all"><span class="i18n" data-my="အားလုံး" data-en="All">အားလုံး</span></button>
          <button data-filter="PENDING"><span class="i18n" data-my="ဆိုင်းငံ့ထား" data-en="Pending">ဆိုင်းငံ့ထား</span></button>
          <button data-filter="RESOLVED"><span class="i18n" data-my="ဖြေရှင်းပြီး" data-en="Resolved">ဖြေရှင်းပြီး</span></button>
        </div>
        <div class="search-box">
          <i class="bi bi-search"></i>
          <input type="text" id="feedbackSearchInput" placeholder="အကြောင်းအရာဖြင့် ရှာဖွေရန်..."
                 data-my-ph="အကြောင်းအရာဖြင့် ရှာဖွေရန်..." data-en-ph="Search by content...">
        </div>
        <span class="results-count" id="feedbackResultsCount"></span>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="တင်ပြသူ" data-en="Submitted By">တင်ပြသူ</th>
              <th class="i18n" data-my="အကြောင်းအရာ" data-en="Content">အကြောင်းအရာ</th>
              <th class="i18n" data-my="အခြေအနေ" data-en="Status">အခြေအနေ</th>
              <th class="i18n" data-my="တင်ပြသည့်ရက်" data-en="Created">တင်ပြသည့်ရက်</th>
              <th class="i18n" data-my="ဖြေရှင်းသည့်ရက်" data-en="Resolved">ဖြေရှင်းသည့်ရက်</th>
              <th class="text-end i18n" data-my="လုပ်ဆောင်ချက်" data-en="Actions">လုပ်ဆောင်ချက်</th>
            </tr>
          </thead>
          <tbody id="feedbackTableBody"></tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<!-- Update ticket status modal -->
<div class="modal fade" id="updateStatusModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3 i18n" data-my="အခြေအနေ ပြောင်းလဲရန်" data-en="Update Ticket Status">အခြေအနေ ပြောင်းလဲရန်</h3>
        <select class="form-select" id="updateStatusSelect">
          <option value="PENDING" class="i18n" data-my="ဆိုင်းငံ့ထား" data-en="Pending">ဆိုင်းငံ့ထား</option>
          <option value="RESOLVED" class="i18n" data-my="ဖြေရှင်းပြီး" data-en="Resolved">ဖြေရှင်းပြီး</option>
        </select>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">မလုပ်တော့ပါ</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveStatusBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="သိမ်းမည်" data-en="Save">သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/feedback-page.js'/>"></script>
</body>
</html>
