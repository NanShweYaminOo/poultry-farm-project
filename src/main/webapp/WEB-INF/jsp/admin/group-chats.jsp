<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="အဖွဲ့စကားပြောများ | Broiler Admin" />
<c:set var="currentPage" value="group-chats" />
<c:set var="topbarTitleMy" value="အဖွဲ့စကားပြောများ" />
<c:set var="topbarTitleEn" value="Group Chats" />
<c:set var="topbarSubMy" value="အဖွဲ့စကားပြောများနှင့် မက်ဆေ့ချ်များ ကြီးကြပ်ခြင်း" />
<c:set var="topbarSubEn" value="View and moderate all group chats and their messages" />
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
      <div class="chat-shell" id="chatShell">
        <aside class="chat-list-pane">
          <div class="chat-list-search">
            <div class="search-box" style="max-width:none;">
              <i class="bi bi-search"></i>
              <input type="text" id="groupChatSearchInput" placeholder="အဖွဲ့အမည်ဖြင့် ရှာဖွေရန်..."
                     data-my-ph="အဖွဲ့အမည်ဖြင့် ရှာဖွေရန်..." data-en-ph="Search groups...">
            </div>
          </div>
          <div class="chat-list" id="groupChatList"></div>
        </aside>

        <section class="chat-thread-pane">
          <div class="chat-thread-empty" id="chatThreadEmpty">
            <i class="bi bi-chat-square-dots"></i>
            <p class="i18n" data-my="ကြည့်ရှုရန် အဖွဲ့တစ်ခုကို ရွေးချယ်ပါ" data-en="Select a group to view its messages">
              ကြည့်ရှုရန် အဖွဲ့တစ်ခုကို ရွေးချယ်ပါ
            </p>
          </div>

          <div class="chat-thread-active d-none" id="chatThreadActive">
            <div class="chat-thread-header">
              <button class="chat-back-btn" id="chatBackBtn" aria-label="Back"><i class="bi bi-arrow-left"></i></button>
              <div class="avatar" id="chatThreadAvatar"></div>
              <div class="info">
                <div class="name" id="chatThreadName"></div>
                <div class="sub" id="chatThreadSub"></div>
              </div>
            </div>
            <div class="chat-members-strip" id="chatMembersStrip"></div>
            <div class="chat-thread-messages" id="chatThreadMessages"></div>

            <form class="chat-composer" id="chatComposerForm">
              <input type="text" id="chatComposerInput" class="i18n-ph" autocomplete="off"
                     placeholder="မက်ဆေ့ချ် ရေးပါ..." data-my-ph="မက်ဆေ့ချ် ရေးပါ..." data-en-ph="Write a message...">
              <button type="submit" class="chat-composer-send" aria-label="Send">
                <i class="bi bi-send-fill"></i>
              </button>
            </form>
          </div>
        </section>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/group-chats-page.js'/>"></script>
</body>
</html>
