<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Breeds | Broiler Farming System" />
<c:set var="currentPage" value="breeds" />
<c:set var="topbarTitleMy" value="ကြက်မျိုးရင်းများ" />
<c:set var="topbarTitleEn" value="Breeds" />
<c:set var="topbarSubMy" value="ဘရွိုင်လာ ကြက်မျိုးရင်း ရည်ညွှန်းချက် အချက်အလက်များ" />
<c:set var="topbarSubEn" value="Broiler breed reference data" />
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
              <th class="i18n" data-my="မျိုးရင်းအမည်" data-en="Breed Name">မျိုးရင်းအမည်</th>
              <th class="i18n" data-my="မူလဇစ်မြစ်" data-en="Origin">မူလဇစ်မြစ်</th>
              <th class="i18n" data-my="ပျမ်းမျှစျေးကွက်အလေးချိန်" data-en="Avg Market Weight">ပျမ်းမျှစျေးကွက်အလေးချိန်</th>
              <th class="i18n" data-my="ကြီးထွားကာလ (ရက်)" data-en="Growth Period (days)">ကြီးထွားကာလ (ရက်)</th>
              <th class="i18n" data-my="FCR" data-en="FCR">FCR</th>
              <th class="i18n" data-my="ဖော်ပြချက်" data-en="Description">ဖော်ပြချက်</th>
            </tr>
          </thead>
          <tbody id="breedsTableBody">
            <tr>
              <td colspan="7" class="text-center" style="padding:30px;color:var(--muted);">
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
<script src="<c:url value='/farmer/assets/js/breeds-page.js'/>"></script>
</body>
</html>
