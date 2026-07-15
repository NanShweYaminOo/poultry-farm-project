<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="ကျွန်ုပ်၏ပရိုဖိုင် | Broiler Farming System" />
<c:set var="currentPage" value="profile" />
<c:set var="topbarTitleMy" value="ကျွန်ုပ်၏ပရိုဖိုင်" />
<c:set var="topbarTitleEn" value="My Profile" />
<c:set var="topbarSubMy" value="သင့်အကောင့် အချက်အလက်များနှင့် စကားဝှက်ကို စီမံခန့်ခွဲရန်" />
<c:set var="topbarSubEn" value="Manage your account details and password" />
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
      <div class="row g-3">
        <div class="col-lg-4">
          <div class="card text-center" style="padding:30px 22px;">
            <div id="profileAvatar" class="mx-auto mb-3" style="width:96px;height:96px;border-radius:50%;background:linear-gradient(135deg,var(--emerald-600),var(--emerald-900));color:var(--gold-300,var(--gold-400));display:flex;align-items:center;justify-content:center;font-size:34px;font-weight:700;border:3px solid var(--gold-500);overflow:hidden;">?</div>
            <h3 id="profileFullName" class="h5 mb-0">-</h3>
            <p id="profileEmail" style="font-size:12.5px;color:var(--muted);" class="mb-3">-</p>
            <span id="profileRole" class="badge badge-emerald mx-auto" style="width:fit-content;">-</span>
            <p id="profileUserId" style="font-size:11px;color:var(--muted);margin:8px 0 0;">
              <span class="i18n" data-my="User ID" data-en="User ID">User ID</span>: <span id="profileUserIdValue">-</span>
            </p>
            <input type="file" id="photoInput" accept="image/*" style="display:none">
            <button type="button" id="changePhotoBtn" class="btn btn-outline mt-4">
              <i class="bi bi-image"></i>
              <span class="i18n" data-my="ပုံပြောင်းရန်" data-en="Change Photo">ပုံပြောင်းရန်</span>
            </button>
          </div>
        </div>

        <div class="col-lg-8">
          <div class="card" style="padding:26px;">
            <div class="section-heading">
              <h2 class="i18n" data-my="အကောင့်အချက်အလက်များ" data-en="Account Details">အကောင့်အချက်အလက်များ</h2>
            </div>
            <form id="profileForm">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label i18n" data-my="အမည်ပြည့်" data-en="Full Name">အမည်ပြည့်</label>
                  <input type="text" class="form-control" id="fullNameInput">
                </div>
                <div class="col-md-6">
                  <label class="form-label i18n" data-my="အသုံးပြုသူအမည်" data-en="Username">အသုံးပြုသူအမည်</label>
                  <input type="text" class="form-control" id="usernameInput" readonly>
                </div>
                <div class="col-md-6">
                  <label class="form-label i18n" data-my="အီးမေးလ်" data-en="Email">အီးမေးလ်</label>
                  <input type="email" class="form-control" id="emailInput" readonly>
                </div>
                <div class="col-md-6">
                  <label class="form-label i18n" data-my="ဖုန်းနံပါတ်" data-en="Phone Number">ဖုန်းနံပါတ်</label>
                  <input type="text" class="form-control" id="phoneInput">
                </div>
              </div>

              <hr style="border-color:var(--line);margin:26px 0;">

              <div class="section-heading">
                <h2 class="i18n" data-my="စကားဝှက် ပြောင်းလဲရန်" data-en="Change Password">စကားဝှက် ပြောင်းလဲရန်</h2>
              </div>
              <div class="row g-3">
                <div class="col-md-4">
                  <label class="form-label i18n" data-my="လက်ရှိစကားဝှက်" data-en="Current Password">လက်ရှိစကားဝှက်</label>
                  <input type="password" class="form-control" id="currentPasswordInput" placeholder="••••••••">
                </div>
                <div class="col-md-4">
                  <label class="form-label i18n" data-my="စကားဝှက်အသစ်" data-en="New Password">စကားဝှက်အသစ်</label>
                  <input type="password" class="form-control" id="newPasswordInput" placeholder="••••••••">
                </div>
                <div class="col-md-4">
                  <label class="form-label i18n" data-my="အတည်ပြုပါ" data-en="Confirm Password">အတည်ပြုပါ</label>
                  <input type="password" class="form-control" id="confirmPasswordInput" placeholder="••••••••">
                </div>
              </div>

              <div class="mt-4 d-flex justify-content-end gap-2">
                <button type="button" id="cancelBtn" class="btn btn-ghost i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">မလုပ်တော့ပါ</button>
                <button type="submit" id="saveChangesBtn" class="btn btn-gold">
                  <i class="bi bi-check-lg"></i>
                  <span class="i18n" data-my="အပြောင်းအလဲများ သိမ်းမည်" data-en="Save Changes">အပြောင်းအလဲများ သိမ်းမည်</span>
                </button>
              </div>
            </form>
          </div>

          <div class="card mt-3" style="padding:26px;">
            <div class="section-heading">
              <h2 class="i18n" data-my="တောင်သူအဖြစ် အဆင့်တင်ရန်" data-en="Upgrade to Farmer">တောင်သူအဖြစ် အဆင့်တင်ရန်</h2>
            </div>
            <p class="i18n" style="font-size:13.5px;color:var(--muted);"
               data-my="တောင်သူအကောင့်များသည် Batch စီမံခန့်ခွဲမှု၊ နေ့စဉ်မှတ်တမ်း၊ ဆေးဝါးသတိပေးချက်များနှင့် စျေးကွက်တွင် ပို့စ်တင်ခြင်းကို အသုံးပြုနိုင်ပါသည်။ Admin မှ အတည်ပြုပြီးမှသာ ပြောင်းလဲပါမည်။"
               data-en="Farmer accounts unlock batch management, daily logs, medicine alarms and posting in the marketplace. An admin must approve your request before it takes effect.">
              Farmer accounts unlock batch management, daily logs, medicine alarms and posting in the marketplace. An admin must approve your request before it takes effect.
            </p>

            <div id="upgradeRequestStatus" class="mb-3" style="display:none;"></div>

            <form id="upgradeRequestForm">
              <label class="form-label i18n" data-my="အကြောင်းရင်း (ရွေးချယ်ခွင့်)" data-en="Reason (optional)">Reason (optional)</label>
              <textarea class="form-control" id="upgradeReasonInput" rows="3"
                        placeholder="e.g. I run a 500-bird broiler farm in Mandalay"></textarea>
              <div class="mt-3 d-flex justify-content-end">
                <button type="submit" id="requestUpgradeBtn" class="btn btn-gold">
                  <i class="bi bi-arrow-up-circle-fill"></i>
                  <span class="i18n" data-my="တောင်သူအဖြစ် အဆင့်တင်ရန် တောင်းဆိုမည်" data-en="Request Farmer Upgrade">Request Farmer Upgrade</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/guest/assets/js/profile-page.js'/>"></script>
</body>
</html>
