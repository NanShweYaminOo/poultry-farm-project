<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="FAQs | Broiler Farming System" />
<c:set var="currentPage" value="faqs" />
<c:set var="topbarTitleMy" value="မေးလေ့ရှိသောမေးခွန်းများ" />
<c:set var="topbarTitleEn" value="FAQs" />
<c:set var="topbarSubMy" value="အသုံးများသော မေးခွန်းများနှင့် အဖြေများ" />
<c:set var="topbarSubEn" value="Common questions and answers" />
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
      <div class="accordion" id="faqAccordion">
        <div class="card text-center" style="padding:30px;color:var(--muted);">
          <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/faqs-page.js'/>"></script>
</body>
</html>
