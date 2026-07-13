<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="မေးလေ့ရှိသောမေးခွန်းများ | Broiler Admin" />
<c:set var="currentPage" value="faqs" />
<c:set var="topbarTitleMy" value="မေးလေ့ရှိသောမေးခွန်းများ" />
<c:set var="topbarTitleEn" value="FAQ Management" />
<c:set var="topbarSubMy" value="အသုံးပြုသူများအတွက် မေးလေ့ရှိသောမေးခွန်းနှင့် အဖြေများကို စီမံခန့်ခွဲရန်" />
<c:set var="topbarSubEn" value="Manage the frequently asked questions shown to users" />
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
          <h1 class="i18n" data-my="မေးခွန်း စာရင်း" data-en="Question List">မေးခွန်း စာရင်း</h1>
          <p class="i18n" data-my="ဤနေရာမှ တည်းဖြတ်ထားသည့် အကြောင်းအရာများသည် အသုံးပြုသူများ၏ အကူအညီစာမျက်နှာတွင် ပေါ်လာမည်" data-en="Edits here appear directly on the user-facing help page">ဤနေရာမှ တည်းဖြတ်ထားသည့် အကြောင်းအရာများသည် အသုံးပြုသူများ၏ အကူအညီစာမျက်နှာတွင် ပေါ်လာမည်</p>
        </div>
        <button class="btn btn-gold" id="addFaqBtn">
          <i class="bi bi-plus-lg"></i>
          <span class="i18n" data-my="မေးခွန်းအသစ်ထည့်ရန်" data-en="Add New FAQ">မေးခွန်းအသစ်ထည့်ရန်</span>
        </button>
      </div>

      <div class="accordion" id="faqAccordion">
        <div class="card text-center" style="padding:30px;color:var(--muted);">
          <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
        </div>
      </div>
    </main>
  </div>
</div>

<!-- Add/Edit FAQ modal -->
<div class="modal fade" id="faqModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3" id="faqModalTitle">Add New FAQ</h3>
        <form id="faqForm">
          <input type="hidden" id="faqIdInput">
          <div class="row g-3">
            <div class="col-md-12">
              <label class="form-label i18n" data-my="မေးခွန်း" data-en="Question">မေးခွန်း</label>
              <input type="text" class="form-control" id="faqQuestionInput" required>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="အဖြေ" data-en="Answer">အဖြေ</label>
              <textarea class="form-control" id="faqAnswerInput" rows="4"></textarea>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">မလုပ်တော့ပါ</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveFaqBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="သိမ်းမည်" data-en="Save">သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Delete confirmation modal -->
<div class="modal fade" id="deleteFaqModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body p-4">
        <div class="modal-icon"><i class="bi bi-exclamation-triangle-fill"></i></div>
        <h3 class="h5 mb-2 modal-title i18n" data-my="ဤမေးခွန်းကို ဖျက်သိမ်းမှာ သေချာပါသလား?" data-en="Delete this FAQ?">ဤမေးခွန်းကို ဖျက်သိမ်းမှာ သေချာပါသလား?</h3>
        <p class="mb-0 i18n" style="font-size:13.5px;color:var(--muted);" data-my="ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။" data-en="This action cannot be undone.">ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မဖျက်ပါ" data-en="Cancel">မဖျက်ပါ</span>
        </button>
        <button type="button" class="btn btn-danger" id="confirmDeleteFaq">
          <i class="bi bi-trash-fill"></i>
          <span class="i18n" data-my="ဖျက်သိမ်းမည်" data-en="Yes, Delete">ဖျက်သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/faqs-page.js'/>"></script>
</body>
</html>
