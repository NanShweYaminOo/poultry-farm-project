<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Marketplace | Broiler Farming System" />
<c:set var="currentPage" value="marketplace" />
<c:set var="topbarTitleMy" value="စျေးကွက်" />
<c:set var="topbarTitleEn" value="Marketplace" />
<c:set var="topbarSubMy" value="ရောင်းရန်ပို့စ်များ (ကြည့်ရှုရုံသာ)" />
<c:set var="topbarSubEn" value="Sales posts (view only)" />
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
      <div class="card mb-3" style="padding:16px 20px;display:flex;align-items:center;gap:12px;">
        <i class="bi bi-info-circle-fill" style="font-size:20px;color:var(--gold-500);"></i>
        <p class="mb-0 i18n" style="font-size:13.5px;color:var(--muted);"
           data-my="ဧည့်သည်အကောင့်များသည် ရောင်းရန်ပို့စ်များကို ကြည့်ရှုနိုင်သော်လည်း ပို့စ်တင်၍မရပါ။ ပို့စ်တင်ရန် တောင်သူအဖြစ် အဆင့်တင်ရန် လိုအပ်ပါသည်။"
           data-en="Guest accounts can browse sales posts but cannot post. Upgrade to a Farmer account (request it from your profile page) to post in the marketplace.">
          Guest accounts can browse sales posts but cannot post. Upgrade to a Farmer account (request it from your profile page) to post in the marketplace.
        </p>
      </div>

      <div class="row g-3" id="salesPostsGrid">
        <div class="col-12 card text-center" style="padding:30px;color:var(--muted);">
          <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/guest/assets/js/marketplace-page.js'/>"></script>
</body>
</html>
