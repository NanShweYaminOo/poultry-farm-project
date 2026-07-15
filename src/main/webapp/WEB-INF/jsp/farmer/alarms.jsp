<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Medicine &amp; Vaccination Alarms | Broiler Farming System" />
<c:set var="currentPage" value="alarms" />
<c:set var="topbarTitleMy" value="ဆေးဝါးနှင့် ကာကွယ်ဆေး သတိပေးချက်များ" />
<c:set var="topbarTitleEn" value="Medicine &amp; Vaccination Alarms" />
<c:set var="topbarSubMy" value="Batch အလိုက် ဆေးဝါးအချိန်ဇယား သတ်မှတ်ပါ" />
<c:set var="topbarSubEn" value="Schedule medicine and vaccination reminders per batch" />
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

    <main class="content" id="alarmsContent">
      <div class="card mb-4" style="padding:20px;">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div style="min-width:260px;">
            <label class="form-label i18n" data-my="Batch ရွေးပါ" data-en="Select Batch">Select Batch</label>
            <select class="form-select" id="alarmBatchSelect" style="max-width:360px;"></select>
          </div>
          <button class="btn btn-gold" id="addAlarmBtn">
            <i class="bi bi-plus-lg"></i>
            <span class="i18n" data-my="သတိပေးချက်အသစ်" data-en="New Alarm">New Alarm</span>
          </button>
        </div>
        <div id="alarmsNoBatchNotice" class="empty-state" style="display:none;">
          <i class="bi bi-clipboard2-pulse"></i>
          <p class="i18n" data-my="လက်ရှိလည်ပတ်နေသော Batch မရှိသေးပါ။" data-en="You have no active batch yet.">You have no active batch yet.</p>
        </div>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;" id="alarmsTableWrap">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="ဆေးဝါးအမည်" data-en="Medicine">Medicine</th>
              <th class="i18n" data-my="အချိန်ဇယား" data-en="Schedule">Schedule</th>
              <th class="i18n" data-my="နောက်တစ်ကြိမ်" data-en="Next Fire">Next Fire</th>
              <th class="i18n" data-my="အခြေအနေ" data-en="Status">Status</th>
              <th class="i18n" data-my="လုပ်ဆောင်ချက်" data-en="Actions">Actions</th>
            </tr>
          </thead>
          <tbody id="alarmsTableBody">
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

<!-- New alarm modal: friendly schedule picker instead of raw cron -->
<div class="modal fade" id="alarmModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3 i18n" data-my="သတိပေးချက်အသစ်" data-en="New Alarm">New Alarm</h3>
        <div id="alarmFormAlert" class="login-alert" style="display:none;margin-bottom:14px;">
          <i class="bi bi-exclamation-triangle-fill"></i><span></span>
        </div>
        <form id="alarmForm">
          <div class="row g-3">
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ဆေးဝါး / ကာကွယ်ဆေးအမည်" data-en="Medicine / Vaccine Name">Medicine / Vaccine Name</label>
              <input type="text" class="form-control" id="alarmMedicineNameInput"
                     placeholder="e.g. Amoxicillin" data-my-ph="ဥပမာ - Amoxicillin" data-en-ph="e.g. Amoxicillin">
              <div style="font-size:11.5px;color:var(--muted);margin-top:4px;" class="i18n"
                   data-my="အက်ဒမင်က ဤအမည်အတွက် စျေးနှုန်းသတ်မှတ်ထားရပါမည်။" data-en="The admin must have this exact medicine configured with a dosage/price.">
                The admin must have this exact medicine configured with a dosage/price.
              </div>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="အချိန်ဇယားအမျိုးအစား" data-en="Schedule Type">Schedule Type</label>
              <select class="form-select" id="alarmScheduleType">
                <option value="DAILY" class="i18n" data-my="နေ့စဉ်" data-en="Daily">Daily</option>
                <option value="WEEKLY" class="i18n" data-my="အပတ်စဉ်" data-en="Weekly">Weekly</option>
                <option value="INTERVAL" class="i18n" data-my="N ရက်တစ်ကြိမ်" data-en="Every N Days">Every N Days</option>
                <option value="ONCE" class="i18n" data-my="တစ်ကြိမ်တည်း (ရက်စွဲသတ်မှတ်)" data-en="One-Time (specific date)">One-Time (specific date)</option>
              </select>
            </div>
            <div class="col-md-6" id="alarmWeekdayField" style="display:none;">
              <label class="form-label i18n" data-my="နေ့ရက်" data-en="Day of Week">Day of Week</label>
              <select class="form-select" id="alarmWeekdaySelect">
                <option value="MON" class="i18n" data-my="တနင်္လာ" data-en="Monday">Monday</option>
                <option value="TUE" class="i18n" data-my="အင်္ဂါ" data-en="Tuesday">Tuesday</option>
                <option value="WED" class="i18n" data-my="ဗုဒ္ဓဟူး" data-en="Wednesday">Wednesday</option>
                <option value="THU" class="i18n" data-my="ကြာသပတေး" data-en="Thursday">Thursday</option>
                <option value="FRI" class="i18n" data-my="သောကြာ" data-en="Friday">Friday</option>
                <option value="SAT" class="i18n" data-my="စနေ" data-en="Saturday">Saturday</option>
                <option value="SUN" class="i18n" data-my="တနင်္ဂနွေ" data-en="Sunday">Sunday</option>
              </select>
            </div>
            <div class="col-md-6" id="alarmIntervalField" style="display:none;">
              <label class="form-label i18n" data-my="ရက်အရေအတွက်" data-en="Every how many days">Every how many days</label>
              <input type="number" min="2" max="28" step="1" class="form-control" id="alarmIntervalInput" value="3">
              <div style="font-size:11px;color:var(--muted);margin-top:4px;" class="i18n"
                   data-my="ခန့်မှန်းချက်ဖြစ်သည် - လကုန်တွင် ပြန်စတင်နိုင်သည်။" data-en="Approximate -- resets at each month boundary.">
                Approximate -- resets at each month boundary.
              </div>
            </div>
            <div class="col-md-6" id="alarmDateField" style="display:none;">
              <label class="form-label i18n" data-my="ရက်စွဲ" data-en="Date">Date</label>
              <input type="date" class="form-control" id="alarmDateInput">
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="အချိန်" data-en="Time">Time</label>
              <input type="time" class="form-control" id="alarmTimeInput" value="08:00">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">Cancel</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveAlarmBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="ဖန်တီးမည်" data-en="Create">Create</span>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Cancel alarm confirm modal -->
<div class="modal fade" id="cancelAlarmModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body p-4 text-center">
        <div class="modal-icon"><i class="bi bi-x-circle"></i></div>
        <h3 class="h5 mb-2 i18n" data-my="သတိပေးချက် ပယ်ဖျက်မည်" data-en="Cancel Alarm">Cancel Alarm</h3>
        <p class="i18n" data-my="ဤသတိပေးချက်ကို ပယ်ဖျက်လိုသည်မှာ သေချာပါသလား။" data-en="Are you sure you want to cancel this alarm?">
          Are you sure you want to cancel this alarm?
        </p>
      </div>
      <div class="modal-footer justify-content-center">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Never mind">Never mind</span>
        </button>
        <button type="button" class="btn btn-danger" id="confirmCancelAlarmBtn">
          <i class="bi bi-x-lg"></i>
          <span class="i18n" data-my="ပယ်ဖျက်မည်" data-en="Cancel Alarm">Cancel Alarm</span>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Mark as Done modal -->
<div class="modal fade" id="completeAlarmModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3 i18n" data-my="ပြီးစီးကြောင်း သတ်မှတ်မည်" data-en="Mark as Done">Mark as Done</h3>
        <div id="completeAlarmEstimateBox" style="background:var(--cream);border-radius:10px;padding:14px;margin-bottom:14px;font-size:13px;">
          <span class="i18n" data-my="တွက်ချက်နေသည်..." data-en="Calculating...">Calculating...</span>
        </div>
        <div id="completeAlarmFormAlert" class="login-alert" style="display:none;margin-bottom:14px;">
          <i class="bi bi-exclamation-triangle-fill"></i><span></span>
        </div>
        <form id="completeAlarmForm">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label i18n" data-my="အမှန်တကယ်သုံးစွဲသည့်ပမာဏ" data-en="Actual Quantity Used">Actual Quantity Used</label>
              <input type="number" min="0.01" step="0.01" class="form-control" id="completeOverriddenQuantityInput">
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="အမှန်တကယ်ကုန်ကျငွေ" data-en="Actual Cost Incurred">Actual Cost Incurred</label>
              <input type="number" min="0.01" step="0.01" class="form-control" id="completeFinalCostInput">
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="မှတ်ချက် (ချန်လှပ်ထားနိုင်သည်)" data-en="Notes (optional)">Notes (optional)</label>
              <textarea class="form-control" id="completeNotesInput" rows="2"></textarea>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">Cancel</span>
        </button>
        <button type="button" class="btn btn-gold" id="confirmCompleteAlarmBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="ပြီးစီးကြောင်းသတ်မှတ်မည်" data-en="Mark as Done">Mark as Done</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/alarms-page.js'/>"></script>
</body>
</html>
