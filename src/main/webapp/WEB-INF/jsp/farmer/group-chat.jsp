<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Group Chat | Broiler Farming System" />
<c:set var="currentPage" value="group-chat" />
<c:set var="topbarTitleMy" value="အဖွဲ့စကားပြော" />
<c:set var="topbarTitleEn" value="Group Chat" />
<c:set var="topbarSubMy" value="တောင်သူများနှင့် အက်ဒမင်များ အတူတကွ ဆွေးနွေးရန်" />
<c:set var="topbarSubEn" value="Chat together with fellow farmers and admins" />
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

    <!-- One single, system-wide chat room shared by every Premium Farmer
         and Admin -- no room list/search/create-group UI, it just loads
         directly. See GroupChatService's Javadoc on the backend. -->
    <main class="content" id="groupChatContent">
      <div class="chat-shell chat-shell-single" id="chatShell">
        <section class="chat-thread-pane">
          <div class="chat-thread-active" id="chatThreadActive">
            <div class="chat-thread-header">
              <div class="avatar" id="chatThreadAvatar"></div>
              <div class="info">
                <div class="name" id="chatThreadName"></div>
                <div class="sub" id="chatThreadSub"></div>
              </div>
            </div>
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
<script src="<c:url value='/farmer/assets/js/group-chat-page.js'/>"></script>
</body>
</html>
