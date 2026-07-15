<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Dashboard | Broiler Admin" />
<c:set var="currentPage" value="dashboard" />
<c:set var="topbarTitleMy" value="ဒက်ရှ်ဘုတ်" />
<c:set var="topbarTitleEn" value="Dashboard" />
<c:set var="topbarSubMy" value="ကြိုဆိုပါတယ် — ယနေ့ စနစ်အခြေအနေနှင့် တောင်းဆိုချက်များ" />
<c:set var="topbarSubEn" value="Welcome back — today's overview and pending requests" />
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
      <div class="row g-3 mb-1">
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-people-fill"></i></div>
            <div class="stat-value" id="statTotalUsers">&hellip;</div>
            <div class="stat-label i18n" data-my="စုစုပေါင်း အသုံးပြုသူများ" data-en="Total Users">စုစုပေါင်း အသုံးပြုသူများ</div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-egg-fried"></i></div>
            <div class="stat-value" id="statActiveBatches">&mdash;</div>
            <div class="stat-label i18n" data-my="လက်ရှိ ကြက်အုပ်စုများ" data-en="Active Batches">လက်ရှိ ကြက်အုပ်စုများ</div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-megaphone-fill"></i></div>
            <div class="stat-value" id="statPostsRequests">&hellip;</div>
            <div class="stat-label i18n" data-my="ပို့စ် / တောင်းဆိုချက်များ" data-en="Posts / Requests">ပို့စ် / တောင်းဆိုချက်များ</div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-cash-coin"></i></div>
            <div class="stat-value" id="statPendingPayments">&hellip;</div>
            <div class="stat-label i18n" data-my="စောင့်ဆိုင်းနေသော ငွေပေးချေမှုများ" data-en="Pending Payments">စောင့်ဆိုင်းနေသော ငွေပေးချေမှုများ</div>
          </div>
        </div>
      </div>

      <div class="row g-3 mt-1">
        <div class="col-lg-7">
          <div class="card" style="padding:22px;height:100%;">
            <div class="section-heading">
              <h2 class="i18n" data-my="မကြာသေးမီက အသုံးပြုသူများ၏ ပို့စ်များ" data-en="Recent User Posts">မကြာသေးမီက အသုံးပြုသူများ၏ ပို့စ်များ</h2>
              <a class="view-all" href="<c:url value='/admin/posts'/>">
                <span class="i18n" data-my="အားလုံးကြည့်ရန်" data-en="View all">အားလုံးကြည့်ရန်</span> &rarr;
              </a>
            </div>
            <div class="posts-feed" id="dashboardPostsFeed">
              <p class="text-center i18n" style="padding:30px;color:var(--muted);" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</p>
            </div>
          </div>
        </div>

        <div class="col-lg-5">
          <div class="card" style="padding:22px;height:100%;">
            <div class="section-heading">
              <h2 class="i18n" data-my="အာရုံစိုက်ရန် တောင်းဆိုချက်များ" data-en="Requests Needing Attention">အာရုံစိုက်ရန် တောင်းဆိုချက်များ</h2>
            </div>
            <div class="d-flex flex-column gap-3" id="dashboardAttentionPanel">
              <p class="text-center i18n" style="padding:30px;color:var(--muted);" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/dashboard-page.js'/>"></script>
</body>
</html>
