<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="အသုံးပြုသူများ | Broiler Admin" />
<c:set var="currentPage" value="users" />
<c:set var="topbarTitleMy" value="အသုံးပြုသူများ" />
<c:set var="topbarTitleEn" value="User Management" />
<c:set var="topbarSubMy" value="အသုံးပြုသူများနှင့် အကြောင်းအရာ မသင့်လျော်မှု ပြန်လည်စစ်ဆေးခြင်း" />
<c:set var="topbarSubEn" value="Manage user accounts and review flagged (toxic) content" />
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
      <div class="row g-3 mb-3">
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-people-fill"></i></div>
            <div class="stat-value" id="statTotalUsersCount">&hellip;</div>
            <div class="stat-label i18n" data-my="စုစုပေါင်း အသုံးပြုသူများ" data-en="Total Users">စုစုပေါင်း အသုံးပြုသူများ</div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon" style="background:var(--gold-100);color:var(--gold-700);"><i class="bi bi-star-fill"></i></div>
            <div class="stat-value" id="statPaidUsersCount">&hellip;</div>
            <div class="stat-label i18n" data-my="ငွေပေးဆောင်ထားသူများ" data-en="Paid Users">ငွေပေးဆောင်ထားသူများ</div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon" style="background:var(--danger-100);color:var(--danger);"><i class="bi bi-flag-fill"></i></div>
            <div class="stat-value" id="statFlaggedUsersCount">&hellip;</div>
            <div class="stat-label i18n" data-my="ပြန်လည်စစ်ဆေးရန် အမှတ်အသားပြုထားသည်" data-en="Flagged for Review">ပြန်လည်စစ်ဆေးရန် အမှတ်အသားပြုထားသည်</div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon" style="background:#eef0ec;color:var(--muted);"><i class="bi bi-slash-circle-fill"></i></div>
            <div class="stat-value" id="statBannedUsersCount">&hellip;</div>
            <div class="stat-label i18n" data-my="ပိတ်ပင်ခံထားရသူများ" data-en="Banned Users">ပိတ်ပင်ခံထားရသူများ</div>
          </div>
        </div>
      </div>

      <div class="toolbar">
        <div class="filter-tabs" id="userFilterTabs">
          <button class="active" data-filter="all"><span class="i18n" data-my="အားလုံး" data-en="All">အားလုံး</span></button>
          <button data-filter="flagged"><span class="i18n" data-my="အမှတ်အသားပြုထားသည်" data-en="Flagged">အမှတ်အသားပြုထားသည်</span></button>
          <button data-filter="banned"><span class="i18n" data-my="ပိတ်ပင်ထားသည်" data-en="Banned">ပိတ်ပင်ထားသည်</span></button>
        </div>
        <div class="search-box">
          <i class="bi bi-search"></i>
          <input type="text" id="userSearchInput" placeholder="အမည် သို့မဟုတ် အီးမေးလ်ဖြင့် ရှာဖွေရန်..."
                 data-my-ph="အမည် သို့မဟုတ် အီးမေးလ်ဖြင့် ရှာဖွေရန်..." data-en-ph="Search by name or email...">
        </div>
        <span class="results-count" id="userResultsCount"></span>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="အသုံးပြုသူ" data-en="User">အသုံးပြုသူ</th>
              <th class="i18n" data-my="အီးမေးလ်" data-en="Email">အီးမေးလ်</th>
              <th class="i18n" data-my="အခန်းကဏ္ဍ" data-en="Role">အခန်းကဏ္ဍ</th>
              <th class="i18n" data-my="အခြေအနေ" data-en="Status">အခြေအနေ</th>
              <th class="i18n" data-my="ပါဝင်သည့်ရက်" data-en="Joined">ပါဝင်သည့်ရက်</th>
              <th class="text-end i18n" data-my="လုပ်ဆောင်ချက်" data-en="Actions">လုပ်ဆောင်ချက်</th>
            </tr>
          </thead>
          <tbody id="usersTableBody"></tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<!-- Toxicity review modal -->
<div class="modal fade" id="reviewModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <div class="modal-icon" style="background:var(--gold-100);color:var(--gold-700);"><i class="bi bi-flag-fill"></i></div>
        <h3 class="h5 mb-2 modal-title i18n" data-my="အကြောင်းအရာ ပြန်လည်စစ်ဆေးခြင်း" data-en="Content Review">အကြောင်းအရာ ပြန်လည်စစ်ဆေးခြင်း</h3>
        <p class="mb-2" style="font-size:12.5px;color:var(--muted);">
          <span class="i18n" data-my="အသုံးပြုသူ" data-en="User">အသုံးပြုသူ</span>: <strong id="reviewUserName"></strong>
        </p>
        <div class="p-3" style="background:var(--cream);border-radius:var(--radius-sm);font-size:13px;color:var(--ink);" id="reviewContent"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="ပိတ်မည်" data-en="Close">ပိတ်မည်</span>
        </button>
        <button type="button" class="btn btn-outline" id="clearFlagBtn">
          <i class="bi bi-check-circle"></i>
          <span class="i18n" data-my="အမှတ်အသားဖယ်ရှားမည်" data-en="Clear Flag">အမှတ်အသားဖယ်ရှားမည်</span>
        </button>
        <button type="button" class="btn btn-danger" id="banFromReviewBtn">
          <i class="bi bi-slash-circle"></i>
          <span class="i18n" data-my="အကောင့်ပိတ်မည်" data-en="Ban User">အကောင့်ပိတ်မည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/users-page.js'/>"></script>
</body>
</html>
