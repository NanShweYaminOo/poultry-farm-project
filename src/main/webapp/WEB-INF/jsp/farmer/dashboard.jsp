<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Dashboard | Broiler Farming System" />
<c:set var="currentPage" value="dashboard" />
<c:set var="topbarTitleMy" value="ဒက်ရှ်ဘုတ်" />
<c:set var="topbarTitleEn" value="Dashboard" />
<c:set var="topbarSubMy" value="ဘရွိုင်လာ ကြက်မွေးမြူရေး စနစ်သို့ ကြိုဆိုပါသည်" />
<c:set var="topbarSubEn" value="Welcome to the Broiler Farming System" />
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
      <div class="welcome-banner">
        <h1 id="dashboardWelcomeName" class="i18n" data-my="ကြိုဆိုပါသည်!" data-en="Welcome!">ကြိုဆိုပါသည်!</h1>
        <p class="i18n" data-my="ကြက်မျိုးရင်း၊ ရောဂါ၊ မေးလေ့ရှိသောမေးခွန်းများနှင့် ဆောင်းပါးများကို လေ့လာပြီး စျေးကွက်ကို ကြည့်ရှုပါ" data-en="Browse breeds, diseases, FAQs and articles, and check the marketplace">Browse breeds, diseases, FAQs and articles, and check the marketplace</p>
      </div>

      <div class="row g-3">
        <div class="col-6 col-lg-4">
          <a class="card quick-link-card" href="<c:url value='/farmer/my-batches'/>">
            <div class="quick-link-icon"><i class="bi bi-clipboard2-pulse"></i></div>
            <div>
              <div class="quick-link-title i18n" data-my="ကျွန်ုပ်၏ Batch များ" data-en="My Batches">ကျွန်ုပ်၏ Batch များ</div>
              <div class="quick-link-sub i18n" data-my="Batch များကို စီမံခန့်ခွဲရန်" data-en="Manage your active batches">Manage your active batches</div>
            </div>
          </a>
        </div>
        <div class="col-6 col-lg-4">
          <a class="card quick-link-card" href="<c:url value='/farmer/daily-log'/>">
            <div class="quick-link-icon"><i class="bi bi-journal-plus"></i></div>
            <div>
              <div class="quick-link-title i18n" data-my="နေ့စဉ်မှတ်တမ်း" data-en="Daily Log">နေ့စဉ်မှတ်တမ်း</div>
              <div class="quick-link-sub i18n" data-my="ယနေ့ မှတ်တမ်းတင်ရန်" data-en="Log today's mortality &amp; feed">Log today's mortality &amp; feed</div>
            </div>
          </a>
        </div>
        <div class="col-6 col-lg-4">
          <a class="card quick-link-card" href="<c:url value='/farmer/premium'/>">
            <div class="quick-link-icon"><i class="bi bi-gem"></i></div>
            <div>
              <div class="quick-link-title i18n" data-my="Premium နှင့် ငွေပေးချေမှု" data-en="Premium &amp; Payments">Premium &amp; Payments</div>
              <div class="quick-link-sub i18n" data-my="Batch/ပို့စ်တင်ခွင့် ငွေပေးချေရန်" data-en="Pay for batch/posting privileges">Pay for batch/posting privileges</div>
            </div>
          </a>
        </div>
        <div class="col-6 col-lg-4">
          <a class="card quick-link-card" href="<c:url value='/farmer/breeds'/>">
            <div class="quick-link-icon"><i class="bi bi-egg-fried"></i></div>
            <div>
              <div class="quick-link-title i18n" data-my="ကြက်မျိုးရင်းများ" data-en="Breeds">ကြက်မျိုးရင်းများ</div>
              <div class="quick-link-sub i18n" data-my="မျိုးရင်း အချက်အလက်များ ကြည့်ရန်" data-en="View breed reference data">View breed reference data</div>
            </div>
          </a>
        </div>
        <div class="col-6 col-lg-4">
          <a class="card quick-link-card" href="<c:url value='/farmer/diseases'/>">
            <div class="quick-link-icon"><i class="bi bi-virus"></i></div>
            <div>
              <div class="quick-link-title i18n" data-my="ရောဂါများ" data-en="Diseases">ရောဂါများ</div>
              <div class="quick-link-sub i18n" data-my="ရောဂါလက္ခဏာများ ကြည့်ရန်" data-en="View disease symptoms &amp; guidance">View disease symptoms &amp; guidance</div>
            </div>
          </a>
        </div>
        <div class="col-6 col-lg-4">
          <a class="card quick-link-card" href="<c:url value='/farmer/faqs'/>">
            <div class="quick-link-icon"><i class="bi bi-question-circle-fill"></i></div>
            <div>
              <div class="quick-link-title i18n" data-my="မေးလေ့ရှိသောမေးခွန်းများ" data-en="FAQs">FAQs</div>
              <div class="quick-link-sub i18n" data-my="အဖြေများ ရှာဖွေရန်" data-en="Find answers to common questions">Find answers to common questions</div>
            </div>
          </a>
        </div>
        <div class="col-6 col-lg-4">
          <a class="card quick-link-card" href="<c:url value='/farmer/articles'/>">
            <div class="quick-link-icon"><i class="bi bi-journal-richtext"></i></div>
            <div>
              <div class="quick-link-title i18n" data-my="ဆောင်းပါးများ" data-en="Articles">Articles</div>
              <div class="quick-link-sub i18n" data-my="ဗဟုသုတဆောင်းပါးများ ဖတ်ရန်" data-en="Read knowledge-base articles">Read knowledge-base articles</div>
            </div>
          </a>
        </div>
        <div class="col-6 col-lg-4">
          <a class="card quick-link-card" href="<c:url value='/farmer/marketplace'/>">
            <div class="quick-link-icon"><i class="bi bi-shop"></i></div>
            <div>
              <div class="quick-link-title i18n" data-my="စျေးကွက်" data-en="Marketplace">Marketplace</div>
              <div class="quick-link-sub i18n" data-my="ရောင်း/ဝယ် ပို့စ်များ ကြည့်ရန်" data-en="Browse sell &amp; buy posts">Browse sell &amp; buy posts</div>
            </div>
          </a>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/dashboard-home-page.js'/>"></script>
</body>
</html>
