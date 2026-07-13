<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="ဗဟုသုတဆောင်းပါးများ | Broiler Admin" />
<c:set var="currentPage" value="articles" />
<c:set var="topbarTitleMy" value="ဗဟုသုတဆောင်းပါးများ" />
<c:set var="topbarTitleEn" value="Knowledge Articles" />
<c:set var="topbarSubMy" value="ဆောင်းပါး၊ အကြံပြုချက်နှင့် ကြေညာချက် ပို့စ်များကို စီမံခန့်ခွဲရန်" />
<c:set var="topbarSubEn" value="Manage articles, tips, disease guides and announcements" />
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
          <h1 class="i18n" data-my="ဆောင်းပါး စာရင်း" data-en="All Articles">ဆောင်းပါး စာရင်း</h1>
          <p class="i18n" data-my="ARTICLE, DISEASE_GUIDE, TIP နှင့် ANNOUNCEMENT အမျိုးအစားများ" data-en="Covers ARTICLE, DISEASE_GUIDE, TIP and ANNOUNCEMENT post types">ARTICLE, DISEASE_GUIDE, TIP နှင့် ANNOUNCEMENT အမျိုးအစားများ</p>
        </div>
        <button class="btn btn-gold" id="addArticleBtn">
          <i class="bi bi-plus-lg"></i>
          <span class="i18n" data-my="ဆောင်းပါးအသစ်ရေးရန်" data-en="Write New Article">ဆောင်းပါးအသစ်ရေးရန်</span>
        </button>
      </div>

      <div class="card" style="padding:6px 4px;overflow-x:auto;">
        <table class="table table-hover align-middle mb-0">
          <thead>
            <tr>
              <th class="i18n" data-my="ပုံ" data-en="Photo">ပုံ</th>
              <th class="i18n" data-my="ခေါင်းစဉ်" data-en="Title">ခေါင်းစဉ်</th>
              <th class="i18n" data-my="အမျိုးအစား" data-en="Type">အမျိုးအစား</th>
              <th class="i18n" data-my="ရေးသားသူ" data-en="Author">ရေးသားသူ</th>
              <th class="i18n" data-my="ရက်စွဲ" data-en="Date">ရက်စွဲ</th>
              <th class="i18n" data-my="အခြေအနေ" data-en="Status">အခြေအနေ</th>
              <th class="text-end i18n" data-my="လုပ်ဆောင်ချက်" data-en="Actions">လုပ်ဆောင်ချက်</th>
            </tr>
          </thead>
          <tbody id="articlesTableBody">
            <tr>
              <td colspan="7" class="text-center" style="padding:30px;color:var(--muted);">
                <span class="i18n" data-my="တင်နေသည်..." data-en="Loading...">တင်နေသည်...</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</div>

<!-- Add/Edit article modal -->
<div class="modal fade" id="articleModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content modal-accent-gold">
      <div class="modal-body p-4">
        <h3 class="h5 mb-3" id="articleModalTitle">Write New Article</h3>
        <form id="articleForm">
          <input type="hidden" id="articleIdInput">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label i18n" data-my="ခေါင်းစဉ် (မြန်မာ)" data-en="Title (Myanmar)">ခေါင်းစဉ် (မြန်မာ)</label>
              <input type="text" class="form-control" id="articleTitleMyInput" required>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="ခေါင်းစဉ် (English)" data-en="Title (English)">ခေါင်းစဉ် (English)</label>
              <input type="text" class="form-control" id="articleTitleEnInput" required>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="အမျိုးအစား" data-en="Type">အမျိုးအစား</label>
              <select class="form-control" id="articleTypeInput">
                <option value="ARTICLE">ARTICLE</option>
                <option value="DISEASE_GUIDE">DISEASE_GUIDE</option>
                <option value="TIP">TIP</option>
                <option value="ANNOUNCEMENT">ANNOUNCEMENT</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="အခြေအနေ" data-en="Status">အခြေအနေ</label>
              <select class="form-control" id="articleStatusInput">
                <option value="PUBLISHED" class="i18n" data-my="ထုတ်ဝေပြီး" data-en="Published">Published</option>
                <option value="DRAFT" class="i18n" data-my="မူကြမ်း" data-en="Draft">Draft</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="အကြောင်းအရာ (မြန်မာ)" data-en="Content (Myanmar)">အကြောင်းအရာ (မြန်မာ)</label>
              <textarea class="form-control" id="articleContentMyInput" rows="4" required></textarea>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="အကြောင်းအရာ (English)" data-en="Content (English)">အကြောင်းအရာ (English)</label>
              <textarea class="form-control" id="articleContentEnInput" rows="4" required></textarea>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="ဓာတ်ပုံ" data-en="Photo">ဓာတ်ပုံ</label>
              <input type="file" accept="image/*" class="form-control" id="articleImageInput">
              <div id="articleImagePreviewWrap" class="mt-2" style="display:none;">
                <img id="articleImagePreview" src="" alt="" style="max-width:120px;max-height:90px;object-fit:cover;border-radius:8px;border:1px solid var(--line);">
              </div>
            </div>
            <div class="col-md-6">
              <label class="form-label i18n" data-my="PDF ဖိုင်" data-en="PDF File">PDF File</label>
              <input type="file" accept="application/pdf" class="form-control" id="articleDocumentInput">
              <div id="articleDocumentCurrentWrap" class="mt-2" style="display:none;">
                <a id="articleDocumentCurrentLink" href="#" target="_blank" rel="noopener">
                  <i class="bi bi-file-earmark-pdf-fill"></i>
                  <span class="i18n" data-my="လက်ရှိ ဖိုင်ကို ကြည့်ရန်" data-en="View current file">View current file</span>
                </a>
              </div>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မလုပ်တော့ပါ" data-en="Cancel">မလုပ်တော့ပါ</span>
        </button>
        <button type="button" class="btn btn-gold" id="saveArticleBtn">
          <i class="bi bi-check-lg"></i>
          <span class="i18n" data-my="သိမ်းမည်" data-en="Save">သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Delete confirmation modal -->
<div class="modal fade" id="deleteArticleModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-body p-4">
        <div class="modal-icon"><i class="bi bi-exclamation-triangle-fill"></i></div>
        <h3 class="h5 mb-2 modal-title i18n" data-my="ဤဆောင်းပါးကို ဖျက်သိမ်းမှာ သေချာပါသလား?" data-en="Delete this article?">ဤဆောင်းပါးကို ဖျက်သိမ်းမှာ သေချာပါသလား?</h3>
        <p class="mb-0 i18n" style="font-size:13.5px;color:var(--muted);" data-my="ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။" data-en="This action cannot be undone.">ဤလုပ်ဆောင်ချက်ကို ပြန်လည်ပြင်ဆင်၍ မရပါ။</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" data-bs-dismiss="modal">
          <span class="i18n" data-my="မဖျက်ပါ" data-en="Cancel">မဖျက်ပါ</span>
        </button>
        <button type="button" class="btn btn-danger" id="confirmDeleteArticle">
          <i class="bi bi-trash-fill"></i>
          <span class="i18n" data-my="ဖျက်သိမ်းမည်" data-en="Yes, Delete">ဖျက်သိမ်းမည်</span>
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="fragments/scripts.jspf" %>
<script src="<c:url value='/admin/assets/js/articles-page.js'/>"></script>
</body>
</html>
