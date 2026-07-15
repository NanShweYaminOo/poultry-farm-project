<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Medicine Calculator | Broiler Farming System" />
<c:set var="currentPage" value="medicine-calculator" />
<c:set var="topbarTitleMy" value="ဆေးဝါးတွက်ချက်စက်" />
<c:set var="topbarTitleEn" value="Medicine Calculator" />
<c:set var="topbarSubMy" value="ကျန်ရှိကြက်အရေအတွက်အပေါ်မူတည်၍ လိုအပ်သောဆေးပမာဏ တွက်ချက်ပါ" />
<c:set var="topbarSubEn" value="Estimate required medicine quantity from your latest remaining bird count" />
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

    <main class="content" id="medCalcContent">
      <div class="card mb-4" style="padding:20px;">
        <div id="medCalcFormAlert" class="login-alert" style="display:none;margin-bottom:14px;">
          <i class="bi bi-exclamation-triangle-fill"></i><span></span>
        </div>
        <div class="row g-3">
          <div class="col-md-5">
            <label class="form-label i18n" data-my="Batch ရွေးပါ" data-en="Select Batch">Select Batch</label>
            <select class="form-select" id="medCalcBatchSelect"></select>
            <div id="medCalcNoBatchNotice" class="empty-state" style="display:none;">
              <i class="bi bi-clipboard2-pulse"></i>
              <p class="i18n" data-my="လက်ရှိလည်ပတ်နေသော Batch မရှိသေးပါ။" data-en="You have no active batch yet.">You have no active batch yet.</p>
            </div>
          </div>
          <div class="col-md-5">
            <label class="form-label i18n" data-my="ဆေးဝါးအမည်" data-en="Medicine Name">Medicine Name</label>
            <input type="text" class="form-control" id="medCalcMedicineNameInput"
                   placeholder="e.g. Amoxicillin" data-my-ph="ဥပမာ - Amoxicillin" data-en-ph="e.g. Amoxicillin">
          </div>
          <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-gold w-100" id="medCalcCalculateBtn">
              <i class="bi bi-calculator"></i>
              <span class="i18n" data-my="တွက်ချက်မည်" data-en="Calculate">Calculate</span>
            </button>
          </div>
        </div>
      </div>

      <div class="card" id="medCalcResultCard" style="padding:24px;display:none;">
        <div class="row g-4">
          <div class="col-md-6">
            <div style="font-size:12.5px;color:var(--muted);" class="i18n" data-my="ကျန်ရှိကြက်အရေအတွက် (နောက်ဆုံးမှတ်တမ်း)" data-en="Remaining Chickens (latest log)">Remaining Chickens (latest log)</div>
            <div style="font-size:20px;font-weight:800;color:var(--emerald-900);" id="medCalcRemainingCount">-</div>

            <div style="font-size:12.5px;color:var(--muted);margin-top:14px;" class="i18n" data-my="ကြက်တစ်ကောင်ချင်းအတွက် ဆေးပမာဏ" data-en="Dosage Per Bird">Dosage Per Bird</div>
            <div style="font-size:16px;font-weight:700;color:var(--ink);" id="medCalcDosagePerBird">-</div>

            <div style="font-size:12.5px;color:var(--muted);margin-top:14px;" class="i18n" data-my="စုစုပေါင်းလိုအပ်ချက်" data-en="Total Calculated Quantity">Total Calculated Quantity</div>
            <div style="font-size:24px;font-weight:800;color:var(--emerald-900);" id="medCalcCalculatedQuantity">-</div>
          </div>
          <div class="col-md-6">
            <label class="form-label i18n" data-my="စျေးနှုန်း တစ်ယူနစ်လျှင် (ချိန်ညှိနိုင်သည်)" data-en="Price Per Unit (adjustable)">Price Per Unit (adjustable)</label>
            <input type="number" min="0.01" step="0.01" class="form-control" id="medCalcPriceInput" style="font-size:18px;font-weight:700;">
            <div style="font-size:11.5px;color:var(--muted);margin-top:4px;" id="medCalcDefaultPriceNote"></div>

            <div style="margin-top:20px;padding:16px;background:var(--cream);border-radius:10px;">
              <div style="font-size:12.5px;color:var(--muted);" class="i18n" data-my="ခန့်မှန်း စုစုပေါင်းကုန်ကျငွေ" data-en="Estimated Total Cost">Estimated Total Cost</div>
              <div style="font-size:28px;font-weight:800;color:var(--gold-700);" id="medCalcTotalCost">-</div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/medicine-calculator-page.js'/>"></script>
</body>
</html>
