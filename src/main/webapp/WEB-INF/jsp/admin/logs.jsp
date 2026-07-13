<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="စနစ်မှတ်တမ်းများ | Broiler Admin" />
<c:set var="currentPage" value="logs" />
<c:set var="topbarTitleMy" value="စနစ်မှတ်တမ်းများ" />
<c:set var="topbarTitleEn" value="System Logs" />
<c:set var="topbarSubMy" value="အက်ဒမင် နှင့် စနစ် လုပ်ဆောင်ချက် မှတ်တမ်းများ" />
<c:set var="topbarSubEn" value="Audit trail of admin actions and system events" />
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
          <h1 class="i18n" data-my="မှတ်တမ်း စာရင်း" data-en="Activity Log">မှတ်တမ်း စာရင်း</h1>
          <p class="i18n" data-my="အကြောင်းအရာဖျက်သိမ်းခြင်း၊ အကောင့်ပိတ်ခြင်းစသည့် အက်ဒမင်လုပ်ဆောင်ချက်များ အားလုံးကို ခြေရာခံနိုင်သည်" data-en="Traces every moderation action -- post deletions, user bans, role changes and more">အကြောင်းအရာဖျက်သိမ်းခြင်း၊ အကောင့်ပိတ်ခြင်းစသည့် အက်ဒမင်လုပ်ဆောင်ချက်များ အားလုံးကို ခြေရာခံနိုင်သည်</p>
        </div>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="အချိန်" data-en="Timestamp">အချိန်</th>
              <th class="i18n" data-my="လုပ်ဆောင်သူ" data-en="Actor">လုပ်ဆောင်သူ</th>
              <th class="i18n" data-my="လုပ်ဆောင်ချက်" data-en="Action">လုပ်ဆောင်ချက်</th>
              <th class="i18n" data-my="ပစ်မှတ်" data-en="Target">ပစ်မှတ်</th>
              <th class="i18n" data-my="အဆင့်" data-en="Level">အဆင့်</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td style="color:var(--muted);white-space:nowrap;">၁၃-၀၇-၂၀၂၆ ၀၉:၂၄</td>
              <td style="font-weight:600;">Super Admin</td>
              <td>Deleted a sales post</td>
              <td style="color:var(--muted);">Post #482</td>
              <td><span class="badge badge-danger-soft">MODERATION</span></td>
            </tr>
            <tr>
              <td style="color:var(--muted);white-space:nowrap;">၁၃-၀၇-၂၀၂၆ ၀၈:၅၁</td>
              <td style="font-weight:600;">Super Admin</td>
              <td>Banned user account</td>
              <td style="color:var(--muted);">ကိုစိုးမင်းထွန်း</td>
              <td><span class="badge badge-danger-soft">MODERATION</span></td>
            </tr>
            <tr>
              <td style="color:var(--muted);white-space:nowrap;">၁၂-၀၇-၂၀၂၆ ၂၂:၀၃</td>
              <td style="font-weight:600;">System</td>
              <td>Payment transaction auto-expired</td>
              <td style="color:var(--muted);">Txn #1029</td>
              <td><span class="badge badge-muted">INFO</span></td>
            </tr>
            <tr>
              <td style="color:var(--muted);white-space:nowrap;">၁၂-၀၇-၂၀၂၆ ၁၈:၄၅</td>
              <td style="font-weight:600;">System</td>
              <td>Scheduled medicine alarm job failed to send notification</td>
              <td style="color:var(--muted);">Batch #77</td>
              <td><span class="badge" style="background:#fdecc8;color:#8a5a00;">WARNING</span></td>
            </tr>
            <tr>
              <td style="color:var(--muted);white-space:nowrap;">၁၂-၀၇-၂၀၂၆ ၁၄:၁၂</td>
              <td style="font-weight:600;">Super Admin</td>
              <td>Reviewed and cleared a content flag</td>
              <td style="color:var(--muted);">မခင်ဇာဇာ</td>
              <td><span class="badge badge-emerald">MODERATION</span></td>
            </tr>
            <tr>
              <td style="color:var(--muted);white-space:nowrap;">၁၁-၀၇-၂၀၂၆ ၁၀:၀၀</td>
              <td style="font-weight:600;">System</td>
              <td>Nightly database backup completed</td>
              <td style="color:var(--muted);">—</td>
              <td><span class="badge badge-muted">INFO</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
</body>
</html>
