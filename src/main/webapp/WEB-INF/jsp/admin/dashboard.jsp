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
            <div class="stat-value">1,284</div>
            <div class="stat-label i18n" data-my="စုစုပေါင်း အသုံးပြုသူများ" data-en="Total Users">စုစုပေါင်း အသုံးပြုသူများ</div>
            <div class="stat-trend"><i class="bi bi-graph-up-arrow"></i> +38 <span class="i18n" data-my="ဒီအပတ်" data-en="this week">ဒီအပတ်</span></div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-egg-fried"></i></div>
            <div class="stat-value">96</div>
            <div class="stat-label i18n" data-my="လက်ရှိ ကြက်အုပ်စုများ" data-en="Active Batches">လက်ရှိ ကြက်အုပ်စုများ</div>
            <div class="stat-trend"><i class="bi bi-graph-up-arrow"></i> +6 <span class="i18n" data-my="ဒီလ" data-en="this month">ဒီလ</span></div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-megaphone-fill"></i></div>
            <div class="stat-value">57</div>
            <div class="stat-label i18n" data-my="ပို့စ် / တောင်းဆိုချက်များ" data-en="Posts / Requests">ပို့စ် / တောင်းဆိုချက်များ</div>
            <div class="stat-trend"><i class="bi bi-graph-up-arrow"></i> +12 <span class="i18n" data-my="ဒီအပတ်" data-en="this week">ဒီအပတ်</span></div>
          </div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="stat-card">
            <div class="stat-icon"><i class="bi bi-cash-coin"></i></div>
            <div class="stat-value">14</div>
            <div class="stat-label i18n" data-my="စောင့်ဆိုင်းနေသော ငွေပေးချေမှုများ" data-en="Pending Payments">စောင့်ဆိုင်းနေသော ငွေပေးချေမှုများ</div>
            <div class="stat-trend" style="color:var(--danger);"><i class="bi bi-alarm"></i> <span class="i18n" data-my="ပြန်လည်စစ်ဆေးရန်" data-en="needs review">ပြန်လည်စစ်ဆေးရန်</span></div>
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
            <div class="posts-feed">
              <div class="post-card">
                <div class="post-card-head">
                  <div class="post-avatar">အက</div>
                  <div class="post-user"><div class="name">ဦးအောင်ကျော်</div><div class="time">၂ နာရီအရင်</div></div>
                  <span class="post-type-badge sale i18n" data-my="ရောင်းရန်ပို့စ်" data-en="Sales Post">ရောင်းရန်ပို့စ်</span>
                </div>
                <p class="post-title">ဘရွိုင်လာကြက် ၅၀၀ ရောင်းရန်ရှိသည်</p>
                <div class="post-meta"><span class="post-chip">၈,၅၀၀ ကျပ်/ကီလို</span></div>
              </div>
              <div class="post-card">
                <div class="post-card-head">
                  <div class="post-avatar">သဝ</div>
                  <div class="post-user"><div class="name">မသီတာဝင်း</div><div class="time">၅ နာရီအရင်</div></div>
                  <span class="post-type-badge request i18n" data-my="ဝယ်လို/စကားပြောရန်" data-en="Chat / Buy Request">ဝယ်လို/စကားပြောရန်</span>
                </div>
                <p class="post-title">ကြက်ကုန်ကျစရိတ် ဆွေးနွေးလိုပါသည်</p>
                <div class="post-meta"><span class="post-chip i18n" data-my="အရေအတွက်: ၁၀၀" data-en="Qty: 100">အရေအတွက်: ၁၀၀</span></div>
              </div>
              <div class="post-card">
                <div class="post-card-head">
                  <div class="post-avatar">ကမ</div>
                  <div class="post-user"><div class="name">ကိုမင်းသူ</div><div class="time">၁ ရက်အရင်</div></div>
                  <span class="post-type-badge sale i18n" data-my="ရောင်းရန်ပို့စ်" data-en="Sales Post">ရောင်းရန်ပို့စ်</span>
                </div>
                <p class="post-title">အသက် ၄၅ ရက်သား ကြက်များ ရောင်းမည်</p>
                <div class="post-meta"><span class="post-chip">၉,၀၀၀ ကျပ်/ကီလို</span></div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-5">
          <div class="card" style="padding:22px;height:100%;">
            <div class="section-heading">
              <h2 class="i18n" data-my="အာရုံစိုက်ရန် တောင်းဆိုချက်များ" data-en="Requests Needing Attention">အာရုံစိုက်ရန် တောင်းဆိုချက်များ</h2>
            </div>
            <div class="d-flex flex-column gap-3">
              <div class="d-flex align-items-start gap-3 pb-3" style="border-bottom:1px dashed var(--line);">
                <span class="badge badge-gold"><i class="bi bi-life-preserver"></i></span>
                <div class="flex-grow-1">
                  <div style="font-weight:700;font-size:13.5px;">ဒေါ်ခင်မာလေး</div>
                  <p class="mb-1" style="font-size:12.5px;color:var(--muted);">"ငွေပေးချေမှု အတည်ပြုချက် မရသေးပါ၊ ကူညီပေးပါ"</p>
                  <span class="badge badge-muted i18n" data-my="စောင့်ဆိုင်းဆဲ" data-en="Pending">စောင့်ဆိုင်းဆဲ</span>
                </div>
              </div>
              <div class="d-flex align-items-start gap-3 pb-3" style="border-bottom:1px dashed var(--line);">
                <span class="badge badge-gold"><i class="bi bi-life-preserver"></i></span>
                <div class="flex-grow-1">
                  <div style="font-weight:700;font-size:13.5px;">ကိုစိုးမင်းထွန်း</div>
                  <p class="mb-1" style="font-size:12.5px;color:var(--muted);">"ကျွန်ုပ်၏အကောင့်ကို အမှားအယွင်းဖြင့် ပိတ်ထားသည်ဟု ထင်ပါသည်"</p>
                  <span class="badge badge-muted i18n" data-my="စောင့်ဆိုင်းဆဲ" data-en="Pending">စောင့်ဆိုင်းဆဲ</span>
                </div>
              </div>
              <div class="d-flex align-items-start gap-3">
                <span class="badge badge-danger-soft"><i class="bi bi-flag-fill"></i></span>
                <div class="flex-grow-1">
                  <div style="font-weight:700;font-size:13.5px;">မနှင်းဆီ</div>
                  <p class="mb-1" style="font-size:12.5px;color:var(--muted);">အသုံးပြုသူတစ်ဦးမှ ပို့စ်တစ်ခုကို မသင့်လျော်ဟု တိုင်ကြားထားသည်</p>
                  <span class="badge badge-danger-soft i18n" data-my="ပြန်လည်စစ်ဆေးရန်" data-en="Needs Review">ပြန်လည်စစ်ဆေးရန်</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
</body>
</html>
