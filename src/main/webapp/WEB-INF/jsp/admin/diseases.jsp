<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="ရောဂါများ | Broiler Admin" />
<c:set var="currentPage" value="diseases" />
<c:set var="topbarTitleMy" value="ရောဂါများ" />
<c:set var="topbarTitleEn" value="Disease Reference" />
<c:set var="topbarSubMy" value="ကြက်ရောဂါ ဗဟုသုတလမ်းညွှန်များနှင့် ချိတ်ဆက်ထားသော အချက်အလက်များ" />
<c:set var="topbarSubEn" value="Disease guides linked to the chatbot's knowledge base" />
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
          <h1 class="i18n" data-my="ရောဂါစာရင်း" data-en="Disease Catalogue">ရောဂါစာရင်း</h1>
          <p class="i18n" data-my="AI စကားပြောဘော့နှင့် ဗဟုသုတဆောင်းပါးများတွင် အသုံးပြုသည့် ရောဂါအချက်အလက်များ" data-en="Reference data used by the AI chatbot diagnosis feature and knowledge posts">AI စကားပြောဘော့နှင့် ဗဟုသုတဆောင်းပါးများတွင် အသုံးပြုသည့် ရောဂါအချက်အလက်များ</p>
        </div>
        <button class="btn btn-gold" id="addDiseaseBtn">
          <i class="bi bi-plus-lg"></i>
          <span class="i18n" data-my="ရောဂါအသစ်ထည့်ရန်" data-en="Add New Disease">ရောဂါအသစ်ထည့်ရန်</span>
        </button>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="ပုံ" data-en="Photo">ပုံ</th>
              <th class="i18n" data-my="ရောဂါအမည်" data-en="Disease">ရောဂါအမည်</th>
              <th class="i18n" data-my="ရောဂါပိုး" data-en="virus">ရောဂါပိုး</th>
              <th class="i18n" data-my="အကြောင်းအရာ" data-en="Details">အကြောင်းအရာ</th>
              <th class="text-end i18n" data-my="လုပ်ဆောင်ချက်" data-en="Actions">လုပ်ဆောင်ချက်</th>
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

<!-- Add/Edit disease modal -->
<div class="modal fade" id="diseaseModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3" id="diseaseModalTitle">Add New Disease</h3>
        <form id="diseaseForm">
          <input type="hidden" id="diseaseIdInput">
          <div class="row g-3">
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ရောဂါအမည်" data-en="Disease Name">ရောဂါအမည်</label>
              <input type="text" class="form-control" id="diseaseNameInput" required>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ရောဂါပိုး" data-en="Virus">ရောဂါပိုး</label>
              <textarea class="form-control" id="diseaseSymptomsInput" rows="2"></textarea>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="အကြောင်းအရာ" data-en="Details">အကြောင်းအရာ</label>
              <textarea class="form-control" id="diseaseNotesInput" rows="2"></textarea>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ဓာတ်ပုံ" data-en="Photo">ဓာတ်ပုံ</label>
              <input type="file" accept="image/*" class="form-control" id="diseaseImageInput">
              <div id="diseaseImagePreviewWrap" class="mt-2" style="display:none;">
                <img id="diseaseImagePreview" src="" alt="" style="max-width:120px;max-height:90px;object-fit:cover;border-radius:8px;border:1px solid var(--line);">
              </div>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">မလုပ်တော့ပါ</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveDiseaseBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="သိမ်းမည်" data-en="Save">သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Delete confirmation modal -->
<div class="modal fade" id="deleteDiseaseModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body p-4">
        <div class="modal-icon"><i class="bi bi-exclamation-triangle-fill"></i></div>
        <h3 class="h5 mb-2 modal-title i18n" data-my="ဤရောဂါကို ဖျက်သိမ်းမှာ သေချာပါသလား?" data-en="Delete this disease?">ဤရောဂါကို ဖျက်သိမ်းမှာ သေချာပါသလား?</h3>
        <p class="mb-0 i18n" style="font-size:13.5px;color:var(--muted);" data-my="ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။" data-en="This action cannot be undone.">ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မဖျက်ပါ" data-en="Cancel">မဖျက်ပါ</span>
        </button>
        <button type="button" class="btn btn-danger" id="confirmDeleteDisease">
          <i class="bi bi-trash-fill"></i>
          <span class="i18n" data-my="ဖျက်သိမ်းမည်" data-en="Yes, Delete">ဖျက်သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/diseases-page.js'/>"></script>
</body>
</html>
