<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="အသုံးပြုသူများ၏ ပို့စ်များ | Broiler Admin" />
<c:set var="currentPage" value="posts" />
<c:set var="topbarTitleMy" value="အသုံးပြုသူများ၏ ပို့စ်များ" />
<c:set var="topbarTitleEn" value="User Posts Management" />
<c:set var="topbarSubMy" value="အများမြင် ရောင်းချမည့်ပို့စ်များနှင့် စကားပြောတောင်းဆိုချက်များကို စီမံခန့်ခွဲရန်" />
<c:set var="topbarSubEn" value="Moderate public sales posts and chat requests submitted by users" />
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
      <div class="page-header">
        <div>
          <h1 class="i18n" data-my="ပို့စ်များ စာရင်း" data-en="All Posts">ပို့စ်များ စာရင်း</h1>
          <p class="i18n" data-my="ရောင်းရန်ပို့စ်များနှင့် ဝယ်လို/စကားပြောရန် တောင်းဆိုချက်များ အားလုံး" data-en="Every sales post and buy/chat request submitted across the platform">ရောင်းရန်ပို့စ်များနှင့် ဝယ်လို/စကားပြောရန် တောင်းဆိုချက်များ အားလုံး</p>
        </div>
      </div>

      <div class="toolbar">
        <div class="filter-tabs" id="filterTabs">
          <button class="active" data-filter="all">
            <span class="i18n" data-my="အားလုံး" data-en="All">အားလုံး</span>
          </button>
          <button data-filter="sale">
            <span class="i18n" data-my="ရောင်းရန်ပို့စ်များ" data-en="Sales Posts">ရောင်းရန်ပို့စ်များ</span>
          </button>
          <button data-filter="request">
            <span class="i18n" data-my="စကားပြော/ဝယ်လို တောင်းဆိုချက်များ" data-en="Chat / Buy Requests">စကားပြော/ဝယ်လို တောင်းဆိုချက်များ</span>
          </button>
        </div>
        <div class="search-box">
          <i class="bi bi-search"></i>
          <input type="text" id="searchInput" placeholder="အမည် သို့မဟုတ် အကြောင်းအရာဖြင့် ရှာဖွေရန်..."
                 data-my-ph="အမည် သို့မဟုတ် အကြောင်းအရာဖြင့် ရှာဖွေရန်..." data-en-ph="Search by name or content...">
        </div>
        <span class="results-count" id="resultsCount"></span>
      </div>

      <section class="posts-feed" id="postsFeed"></section>
    </main>
  </div>
</div>

<!-- Delete confirmation modal (Bootstrap 5) -->
<div class="modal fade" id="deleteModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body p-4">
        <div class="modal-icon"><i class="bi bi-exclamation-triangle-fill"></i></div>
        <h3 class="h5 mb-2 modal-title i18n" data-my="ပို့စ်ကို ဖျက်သိမ်းမှာ သေချာပါသလား?" data-en="Delete this post?">ပို့စ်ကို ဖျက်သိမ်းမှာ သေချာပါသလား?</h3>
        <p class="mb-0 i18n" style="font-size:13.5px;color:var(--muted);" data-my="ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။ ပို့စ်သည် အသုံးပြုသူများထံမှ အပြီးတိုင် ဖယ်ရှားသွားမည်ဖြစ်သည်။" data-en="This action cannot be undone. The post will be permanently removed from public view.">ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။ ပို့စ်သည် အသုံးပြုသူများထံမှ အပြီးတိုင် ဖယ်ရှားသွားမည်ဖြစ်သည်။</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မဖျက်ပါ" data-en="Cancel">မဖျက်ပါ</span>
        </button>
        <button type="button" class="btn btn-danger" id="confirmDelete">
          <i class="bi bi-trash-fill"></i>
          <span class="i18n" data-my="ဖျက်သိမ်းမည်" data-en="Yes, Delete">ဖျက်သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/posts-page.js'/>"></script>
</body>
</html>
