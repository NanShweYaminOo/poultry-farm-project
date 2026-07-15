<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Articles | Broiler Farming System" />
<c:set var="currentPage" value="articles" />
<c:set var="topbarTitleMy" value="ဗဟုသုတဆောင်းပါးများ" />
<c:set var="topbarTitleEn" value="Articles" />
<c:set var="topbarSubMy" value="ဆောင်းပါး၊ အကြံပြုချက်နှင့် ကြေညာချက်များ" />
<c:set var="topbarSubEn" value="Articles, tips and announcements" />
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
      <div class="row g-3" id="articlesGrid">
        <div class="col-12 card text-center" style="padding:30px;color:var(--muted);">
          <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
        </div>
      </div>
    </main>
  </div>
</div>

<!-- Article read modal -->
<div class="modal fade" id="articleViewModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <img id="articleViewImage" src="" alt="" style="display:none;width:100%;max-height:220px;object-fit:cover;border-radius:10px;margin-bottom:14px;">
        <h3 class="h5 mb-2" id="articleViewTitle"></h3>
        <p style="white-space:pre-line;color:#3d473f;" id="articleViewContent"></p>
      </div>
      <div class="modal-footer">
        <a id="articleViewDocumentLink" href="#" target="_blank" rel="noopener" class="btn btn-outline me-auto" style="display:none;">
          <i class="bi bi-file-earmark-pdf-fill"></i>
          <span class="i18n" data-my="PDF ဒေါင်းလုဒ်" data-en="Download PDF">Download PDF</span>
        </a>
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="ပိတ်မည်" data-en="Close">ပိတ်မည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/articles-page.js'/>"></script>
</body>
</html>
