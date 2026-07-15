<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Feedback | Broiler Farming System" />
<c:set var="currentPage" value="feedback" />
<c:set var="topbarTitleMy" value="အကြံပြုချက်" />
<c:set var="topbarTitleEn" value="Feedback" />
<c:set var="topbarSubMy" value="သင်၏ အကြံပြုချက် (သို့) ပြဿနာကို တင်ပြပါ" />
<c:set var="topbarSubEn" value="Submit your feedback or issue to the admin team" />
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
      <div class="card" style="padding:20px;margin-bottom:18px;">
        <h3 class="h5 mb-3 i18n" data-my="အကြံပြုချက် အသစ်တင်ရန်" data-en="Submit New Feedback">Submit New Feedback</h3>
        <form id="feedbackForm">
          <div class="mb-3">
            <label class="form-label i18n" data-my="အကြောင်းအရာ" data-en="Content">Content</label>
            <textarea class="form-control" id="feedbackContentInput" rows="4" required
                      placeholder="ဤနေရာတွင် သင်၏ အကြံပြုချက် (သို့) ပြဿနာကို ရေးပါ..."
                      data-my-ph="ဤနေရာတွင် သင်၏ အကြံပြုချက် (သို့) ပြဿနာကို ရေးပါ..."
                      data-en-ph="Write your feedback or issue here..."></textarea>
          </div>
          <button type="button" class="btn btn-gold" id="submitFeedbackBtn">
            <i class="bi bi-send-fill"></i>
            <span class="i18n" data-my="တင်ပြမည်" data-en="Submit">Submit</span>
          </button>
        </form>
      </div>

      <h3 class="h6 mb-3 i18n" data-my="ကျွန်ုပ်၏ တင်ပြချက်များ" data-en="My Tickets">My Tickets</h3>
      <div id="myFeedbackTickets">
        <div class="card text-center" style="padding:30px;color:var(--muted);">
          <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
        </div>
      </div>
    </main>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/feedback-page.js'/>"></script>
</body>
</html>
