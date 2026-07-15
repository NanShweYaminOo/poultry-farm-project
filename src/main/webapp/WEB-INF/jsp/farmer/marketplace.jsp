<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Marketplace | Broiler Farming System" />
<c:set var="currentPage" value="marketplace" />
<c:set var="topbarTitleMy" value="စျေးကွက်" />
<c:set var="topbarTitleEn" value="Marketplace" />
<c:set var="topbarSubMy" value="ရောင်း/ဝယ် ပို့စ်များ" />
<c:set var="topbarSubEn" value="Sell and buy posts" />
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
      <ul class="nav nav-tabs" id="marketplaceTabs">
        <li class="nav-item">
          <button class="nav-link active" data-tab="sell" type="button">
            <i class="bi bi-tag-fill"></i>
            <span class="i18n" data-my="ရောင်းရန်ပို့စ်များ" data-en="Sales Posts">ရောင်းရန်ပို့စ်များ</span>
          </button>
        </li>
        <li class="nav-item" id="buyTabItem">
          <button class="nav-link" data-tab="buy" type="button">
            <i class="bi bi-boxes"></i>
            <span class="i18n" data-my="ဝယ်လိုမှုများ" data-en="Buy Requests">ဝယ်လိုမှုများ</span>
          </button>
        </li>
      </ul>

      <div class="marketplace-pane" data-pane="sell">
        <div class="d-flex justify-content-end mb-3">
          <button class="btn btn-gold" id="addSalesPostBtn">
            <i class="bi bi-plus-lg"></i>
            <span class="i18n" data-my="ရောင်းရန်ပို့စ်အသစ်" data-en="New Sales Post">New Sales Post</span>
          </button>
        </div>
        <div class="row g-3" id="salesPostsGrid">
          <div class="col-12 card text-center" style="padding:30px;color:var(--muted);">
            <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
          </div>
        </div>
      </div>

      <div class="marketplace-pane" data-pane="buy" style="display:none;">
        <div class="d-flex justify-content-end mb-3">
          <button class="btn btn-gold" id="addBuyRequestBtn">
            <i class="bi bi-plus-lg"></i>
            <span class="i18n" data-my="ဝယ်လိုမှုအသစ်" data-en="New Buy Request">New Buy Request</span>
          </button>
        </div>
        <div class="row g-3" id="buyRequestsGrid">
          <div class="col-12 card text-center" style="padding:30px;color:var(--muted);">
            <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<!-- New sales post modal -->
<div class="modal fade" id="salesPostModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3 i18n" data-my="ရောင်းရန်ပို့စ်အသစ်" data-en="New Sales Post">New Sales Post</h3>
        <form id="salesPostForm">
          <div class="row g-3">
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ခေါင်းစဉ်" data-en="Title">Title</label>
              <input type="text" class="form-control" id="salesPostTitleInput" required>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="အသေးစိတ်" data-en="Description">Description</label>
              <textarea class="form-control" id="salesPostDescriptionInput" rows="3"></textarea>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="စျေးနှုန်း" data-en="Price">Price</label>
              <input type="number" min="0" step="0.01" class="form-control" id="salesPostPriceInput">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">Cancel</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveSalesPostBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="တင်မည်" data-en="Post">Post</span>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- New buy request modal -->
<div class="modal fade" id="buyRequestModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3 i18n" data-my="ဝယ်လိုမှုအသစ်" data-en="New Buy Request">New Buy Request</h3>
        <form id="buyRequestForm">
          <div class="row g-3">
            <div class="col-md-12">
              <label class="form-label i18n" data-my="ခေါင်းစဉ်" data-en="Title">Title</label>
              <input type="text" class="form-control" id="buyRequestTitleInput" required>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="အသေးစိတ်" data-en="Description">Description</label>
              <textarea class="form-control" id="buyRequestDescriptionInput" rows="3"></textarea>
            </div>
            <div class="col-md-12">
              <label class="form-label i18n" data-my="အရေအတွက်" data-en="Quantity">Quantity</label>
              <input type="number" min="1" step="1" class="form-control" id="buyRequestQuantityInput">
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">Cancel</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveBuyRequestBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="တင်မည်" data-en="Post">Post</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/farmer/assets/js/marketplace-page.js'/>"></script>
</body>
</html>
