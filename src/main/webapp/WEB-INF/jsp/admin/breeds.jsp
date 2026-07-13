<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="ကြက်မျိုးရင်းများ | Broiler Admin" />
<c:set var="currentPage" value="breeds" />
<c:set var="topbarTitleMy" value="ကြက်မျိုးရင်းများ" />
<c:set var="topbarTitleEn" value="Breed Reference" />
<c:set var="topbarSubMy" value="ဘရွိုင်လာ ကြက်မျိုးရင်း ရည်ညွှန်းချက် အချက်အလက်များ စီမံခန့်ခွဲရန်" />
<c:set var="topbarSubEn" value="Manage the broiler breed reference data used across the app" />
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
          <h1 class="i18n" data-my="ကြက်မျိုးရင်း စာရင်း" data-en="Breed Catalogue">ကြက်မျိုးရင်း စာရင်း</h1>
          <p class="i18n" data-my="ကုန်ကျစရိတ်တွက်ချက်မှုနှင့် လမ်းညွှန်ချက်များအတွက် အသုံးပြုသော မျိုးရင်းအချက်အလက်များ" data-en="Reference weights and growth data used by the cost-estimation and guidance features">ကုန်ကျစရိတ်တွက်ချက်မှုနှင့် လမ်းညွှန်ချက်များအတွက် အသုံးပြုသော မျိုးရင်းအချက်အလက်များ</p>
        </div>
        <button class="btn btn-gold" id="addBreedBtn">
          <i class="bi bi-plus-lg"></i>
          <span class="i18n" data-my="မျိုးရင်းအသစ်ထည့်ရန်" data-en="Add New Breed">မျိုးရင်းအသစ်ထည့်ရန်</span>
        </button>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="ပုံ" data-en="Photo">ပုံ</th>
              <th class="i18n" data-my="မျိုးရင်းအမည်" data-en="Breed Name">မျိုးရင်းအမည်</th>
              <th class="i18n" data-my="မူလဇစ်မြစ်" data-en="Origin">မူလဇစ်မြစ်</th>
              <th class="i18n" data-my="ပျမ်းမျှစျေးကွက်အလေးချိန်" data-en="Avg Market Weight">ပျမ်းမျှစျေးကွက်အလေးချိန်</th>
              <th class="i18n" data-my="ကြီးထွားကာလ (ရက်)" data-en="Growth Period (days)">ကြီးထွားကာလ (ရက်)</th>
              <th class="text-end i18n" data-my="လုပ်ဆောင်ချက်" data-en="Actions">လုပ်ဆောင်ချက်</th>
            </tr>
          </thead>
          <tbody id="breedsTableBody">
            <tr>
              <td colspan="6" class="text-center" style="padding:30px;color:var(--muted);">
                <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<!-- Add/Edit breed modal -->
<div class="modal fade" id="breedModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3" id="breedModalTitle">Add New Breed</h3>
        <form id="breedForm">
          <input type="hidden" id="breedIdInput">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label i18n" data-my="မျိုးရင်းအမည်" data-en="Breed Name">မျိုးရင်းအမည်</label>
              <input type="text" class="form-control" id="breedNameInput" required>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="မူလဇစ်မြစ်" data-en="Origin">မူလဇစ်မြစ်</label>
              <input type="text" class="form-control" id="breedOriginInput">
            </div>
            <div class="col-md-4">
              <label class="form-label i18n" data-my="ပျမ်းမျှစျေးကွက်အလေးချိန် (ကီလို)" data-en="Avg Market Weight (kg)">ပျမ်းမျှစျေးကွက်အလေးချိန် (ကီလို)</label>
              <input type="number" step="0.01" min="0" class="form-control" id="breedWeightInput">
            </div>
            <div class="col-md-4">
              <label class="form-label i18n" data-my="ကြီးထွားကာလ (ရက်)" data-en="Growth Period (days)">ကြီးထွားကာလ (ရက်)</label>
              <input type="number" step="1" min="0" class="form-control" id="breedGrowthInput">
            </div>
            <div class="col-md-8">
              <label class="form-label i18n" data-my="အသေးစိတ်ဖော်ပြချက်" data-en="Description">အသေးစိတ်ဖော်ပြချက်</label>
              <textarea class="form-control" id="breedDescriptionInput" rows="2"></textarea>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ဓာတ်ပုံ" data-en="Photo">ဓာတ်ပုံ</label>
              <input type="file" accept="image/*" class="form-control" id="breedImageInput">
              <div id="breedImagePreviewWrap" class="mt-2" style="display:none;">
                <img id="breedImagePreview" src="" alt="" style="max-width:120px;max-height:90px;object-fit:cover;border-radius:8px;border:1px solid var(--line);">
              </div>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">မလုပ်တော့ပါ</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveBreedBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="သိမ်းမည်" data-en="Save">သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Delete confirmation modal -->
<div class="modal fade" id="deleteBreedModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body p-4">
        <div class="modal-icon"><i class="bi bi-exclamation-triangle-fill"></i></div>
        <h3 class="h5 mb-2 modal-title i18n" data-my="ဤမျိုးရင်းကို ဖျက်သိမ်းမှာ သေချာပါသလား?" data-en="Delete this breed?">ဤမျိုးရင်းကို ဖျက်သိမ်းမှာ သေချာပါသလား?</h3>
        <p class="mb-0 i18n" style="font-size:13.5px;color:var(--muted);" data-my="ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။" data-en="This action cannot be undone.">ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မဖျက်ပါ" data-en="Cancel">မဖျက်ပါ</span>
        </button>
        <button type="button" class="btn btn-danger" id="confirmDeleteBreed">
          <i class="bi bi-trash-fill"></i>
          <span class="i18n" data-my="ဖျက်သိမ်းမည်" data-en="Yes, Delete">ဖျက်သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/breeds-page.js'/>"></script>
</body>
</html>
