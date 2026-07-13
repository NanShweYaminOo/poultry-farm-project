<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="အကူအညီ | Broiler Admin" />
<c:set var="currentPage" value="help" />
<c:set var="topbarTitleMy" value="အကူအညီ" />
<c:set var="topbarTitleEn" value="Help & Documentation" />
<c:set var="topbarSubMy" value="အက်ဒမင်ဘုတ်ကို အသုံးပြုနည်း လမ်းညွှန်ချက်များ" />
<c:set var="topbarSubEn" value="Guidance for using this admin panel" />
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
        <div class="col-md-6 col-lg-4">
          <div class="card h-100" style="padding:22px;">
            <div class="stat-icon mb-3"><i class="bi bi-rocket-takeoff-fill"></i></div>
            <h3 class="h6 i18n" data-my="အစပြုနည်း" data-en="Getting Started">အစပြုနည်း</h3>
            <p style="font-size:13px;color:var(--muted);" class="i18n"
               data-my="ဆက်တင်များတွင် သင့်ပရိုဖိုင်ကို ပြီးမြောက်အောင်ပြင်ဆင်ပြီးနောက် ဒက်ရှ်ဘုတ်မှတဆင့် နေ့စဉ်ဆောင်ရွက်ရမည့်အလုပ်များကို စတင်နိုင်ပါသည်။"
               data-en="Finish setting up your profile, then use the Dashboard to see what needs attention each day.">
              ဆက်တင်များတွင် သင့်ပရိုဖိုင်ကို ပြီးမြောက်အောင်ပြင်ဆင်ပြီးနောက် ဒက်ရှ်ဘုတ်မှတဆင့် နေ့စဉ်ဆောင်ရွက်ရမည့်အလုပ်များကို စတင်နိုင်ပါသည်။
            </p>
          </div>
        </div>
        <div class="col-md-6 col-lg-4">
          <div class="card h-100" style="padding:22px;">
            <div class="stat-icon mb-3"><i class="bi bi-megaphone-fill"></i></div>
            <h3 class="h6 i18n" data-my="ပို့စ်များ စီမံခန့်ခွဲခြင်း" data-en="Managing Posts">ပို့စ်များ စီမံခန့်ခွဲခြင်း</h3>
            <p style="font-size:13px;color:var(--muted);" class="i18n"
               data-my="&quot;အသုံးပြုသူများ၏ ပို့စ်များ&quot; စာမျက်နှာတွင် စည်းမျဉ်းချိုးဖောက်သော ရောင်းရန်ပို့စ်များနှင့် စကားပြောတောင်းဆိုချက်များကို ရှာဖွေ၍ ဖျက်သိမ်းနိုင်ပါသည်။"
               data-en="Use the User Posts page to search, filter and remove sales posts or chat requests that break the rules.">
              "အသုံးပြုသူများ၏ ပို့စ်များ" စာမျက်နှာတွင် စည်းမျဉ်းချိုးဖောက်သော ရောင်းရန်ပို့စ်များနှင့် စကားပြောတောင်းဆိုချက်များကို ရှာဖွေ၍ ဖျက်သိမ်းနိုင်ပါသည်။
            </p>
          </div>
        </div>
        <div class="col-md-6 col-lg-4">
          <div class="card h-100" style="padding:22px;">
            <div class="stat-icon mb-3" style="background:var(--danger-100);color:var(--danger);"><i class="bi bi-flag-fill"></i></div>
            <h3 class="h6 i18n" data-my="အကြောင်းအရာ ပြန်လည်စစ်ဆေးခြင်း" data-en="Toxicity Review">အကြောင်းအရာ ပြန်လည်စစ်ဆေးခြင်း</h3>
            <p style="font-size:13px;color:var(--muted);" class="i18n"
               data-my="&quot;အသုံးပြုသူများ&quot; စာမျက်နှာတွင် အမှတ်အသားပြုခံရသော အကောင့်များကို &quot;Review&quot; ခလုတ်ဖြင့် ကြည့်ရှု၍ အမှတ်အသားဖယ်ရှားရန် သို့မဟုတ် အကောင့်ပိတ်ရန် ဆုံးဖြတ်နိုင်ပါသည်။"
               data-en="On the Users page, click Review on a flagged account to read the reported content, then clear the flag or ban the account.">
              "အသုံးပြုသူများ" စာမျက်နှာတွင် အမှတ်အသားပြုခံရသော အကောင့်များကို "Review" ခလုတ်ဖြင့် ကြည့်ရှု၍ အမှတ်အသားဖယ်ရှားရန် သို့မဟုတ် အကောင့်ပိတ်ရန် ဆုံးဖြတ်နိုင်ပါသည်။
            </p>
          </div>
        </div>
        <div class="col-md-6 col-lg-4">
          <div class="card h-100" style="padding:22px;">
            <div class="stat-icon mb-3"><i class="bi bi-egg-fried"></i></div>
            <h3 class="h6 i18n" data-my="ရည်ညွှန်းအချက်အလက်များ" data-en="Reference Data">ရည်ညွှန်းအချက်အလက်များ</h3>
            <p style="font-size:13px;color:var(--muted);" class="i18n"
               data-my="&quot;ကြက်မျိုးရင်းများ&quot; နှင့် &quot;ရောဂါများ&quot; စာမျက်နှာများသည် ကုန်ကျစရိတ်တွက်ချက်မှုနှင့် AI စကားပြောဘော့ အတွက် အခြေခံဒေတာများ ဖြစ်သည်။"
               data-en="Breeds and Diseases feed the cost-estimation tool and the AI chatbot with their reference data.">
              "ကြက်မျိုးရင်းများ" နှင့် "ရောဂါများ" စာမျက်နှာများသည် ကုန်ကျစရိတ်တွက်ချက်မှုနှင့် AI စကားပြောဘော့ အတွက် အခြေခံဒေတာများ ဖြစ်သည်။
            </p>
          </div>
        </div>
        <div class="col-md-6 col-lg-4">
          <div class="card h-100" style="padding:22px;">
            <div class="stat-icon mb-3"><i class="bi bi-clock-history"></i></div>
            <h3 class="h6 i18n" data-my="စနစ်မှတ်တမ်း ကြည့်ရှုနည်း" data-en="Reading the Logs">စနစ်မှတ်တမ်း ကြည့်ရှုနည်း</h3>
            <p style="font-size:13px;color:var(--muted);" class="i18n"
               data-my="&quot;စနစ်မှတ်တမ်းများ&quot; စာမျက်နှာတွင် အက်ဒမင်များ၏ လုပ်ဆောင်ချက်အားလုံးကို အချိန်အလိုက် ခြေရာခံနိုင်သည်။"
               data-en="The System Logs page gives a chronological audit trail of every admin action for accountability.">
              "စနစ်မှတ်တမ်းများ" စာမျက်နှာတွင် အက်ဒမင်များ၏ လုပ်ဆောင်ချက်အားလုံးကို အချိန်အလိုက် ခြေရာခံနိုင်သည်။
            </p>
          </div>
        </div>
        <div class="col-md-6 col-lg-4">
          <div class="card h-100" style="padding:22px;background:linear-gradient(135deg,var(--emerald-900),var(--emerald-950));color:var(--gold-100);">
            <div class="stat-icon mb-3" style="background:rgba(212,175,55,.18);color:var(--gold-400);"><i class="bi bi-headset"></i></div>
            <h3 class="h6" style="color:var(--gold-200);"><span class="i18n" data-my="နောက်ထပ် အကူအညီလိုပါသလား?" data-en="Need more help?">နောက်ထပ် အကူအညီလိုပါသလား?</span></h3>
            <p style="font-size:13px;color:rgba(250,243,224,.75);">
              <i class="bi bi-envelope-fill"></i> support@broilerfarm.com<br>
              <i class="bi bi-telephone-fill"></i> +95 9 123 456 789
            </p>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
</body>
</html>
