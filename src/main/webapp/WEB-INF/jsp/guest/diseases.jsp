<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Diseases | Broiler Farming System" />
<c:set var="currentPage" value="diseases" />
<c:set var="topbarTitleMy" value="ရောဂါများ" />
<c:set var="topbarTitleEn" value="Diseases" />
<c:set var="topbarSubMy" value="ကြက်ရောဂါ လက္ခဏာများနှင့် လမ်းညွှန်ချက်များ" />
<c:set var="topbarSubEn" value="Disease symptoms and guidance" />
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
      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="ပုံ" data-en="Photo">ပုံ</th>
              <th class="i18n" data-my="ရောဂါအမည်" data-en="Disease">ရောဂါအမည်</th>
              <th class="i18n" data-my="အဓိက လက္ခဏာများ" data-en="Key Symptoms">အဓိက လက္ခဏာများ</th>
              <th class="i18n" data-my="ပြင်းထန်မှု" data-en="Severity">ပြင်းထန်မှု</th>
              <th class="i18n" data-my="မှတ်ချက်" data-en="Notes">မှတ်ချက်</th>
            </tr>
          </thead>
          <tbody id="diseasesTableBody">
            <tr>
              <td colspan="5" class="text-center" style="padding:30px;color:var(--muted);">
                <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/guest/assets/js/diseases-page.js'/>"></script>
</body>
</html>
