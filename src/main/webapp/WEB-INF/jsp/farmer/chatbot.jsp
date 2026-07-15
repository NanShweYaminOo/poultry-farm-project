<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="AI Chatbot | Broiler Farming System" />
<c:set var="currentPage" value="chatbot" />
<c:set var="topbarTitleMy" value="AI ဆေးကုသမှုအကူအညီ" />
<c:set var="topbarTitleEn" value="AI Chatbot" />
<c:set var="topbarSubMy" value="ရောဂါလက္ခဏာများ မေးမြန်းပါ၊ ဓာတ်ပုံဖြင့်လည်း တင်နိုင်သည်" />
<c:set var="topbarSubEn" value="Ask about symptoms, or upload a photo for diagnosis" />
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

    <main class="content" id="chatbotContent">
      <div class="card mb-3" style="padding:14px 18px;font-size:12.5px;color:var(--muted);">
        <i class="bi bi-info-circle-fill" style="color:var(--gold-600);"></i>
        <span class="i18n" data-my="ဤသည်မှာ ကနဦး AI အကဲဖြတ်ချက်သာဖြစ်ပြီး ကုသရေးဆရာဝန်၏အစားထိုးမှု မဟုတ်ပါ။" data-en="This is a preliminary AI assessment, not a substitute for a veterinarian.">
          This is a preliminary AI assessment, not a substitute for a veterinarian.
        </span>
      </div>

      <div class="chat-shell" style="height:calc(100vh - 260px);">
        <section class="chat-thread-pane">
          <div class="chat-thread-messages" id="chatbotMessages">
            <div class="chat-list-empty i18n" data-my="ရောဂါလက္ခဏာများ (သို့) ဓာတ်ပုံဖြင့် မေးမြန်းခြင်းဖြင့် စတင်ပါ" data-en="Start by describing symptoms, or attach a photo">
              Start by describing symptoms, or attach a photo
            </div>
          </div>

          <form id="chatbotComposerForm" style="display:flex;flex-direction:column;gap:8px;padding:14px 18px;border-top:1px solid var(--line);background:var(--white);">
            <div id="chatbotImagePreviewRow" style="display:none;align-items:center;gap:10px;">
              <img id="chatbotImagePreviewThumb" style="width:52px;height:52px;object-fit:cover;border-radius:8px;">
              <span style="font-size:12px;color:var(--muted);" id="chatbotImagePreviewName"></span>
              <button type="button" class="btn btn-ghost btn-sm" id="chatbotRemoveImageBtn"><i class="bi bi-x-lg"></i></button>
            </div>
            <div style="display:flex;gap:8px;align-items:center;">
              <input type="file" id="chatbotImageInput" accept="image/*" style="display:none;">
              <button type="button" class="chat-back-btn" id="chatbotAttachBtn" title="Attach image" style="display:inline-flex;">
                <i class="bi bi-paperclip"></i>
              </button>
              <input type="text" id="chatbotMessageInput" autocomplete="off" style="flex:1;border:1px solid var(--line);border-radius:20px;padding:10px 16px;font-size:13.5px;"
                     placeholder="ရောဂါလက္ခဏာများ ရေးပါ..." data-my-ph="ရောဂါလက္ခဏာများ ရေးပါ..." data-en-ph="Describe the symptoms...">
              <button type="submit" class="chat-composer-send" id="chatbotSendBtn" aria-label="Send">
                <i class="bi bi-send-fill"></i>
              </button>
            </div>
          </form>
        </section>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/chatbot-page.js'/>"></script>
</body>
</html>
