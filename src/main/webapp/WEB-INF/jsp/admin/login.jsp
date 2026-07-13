<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Admin Login | Broiler Admin" />
<!DOCTYPE html>
<html lang="my" data-lang="my">
<head>
<%@ include file="fragments/head.jspf" %>
<link rel="stylesheet" href="<c:url value='/admin/assets/css/login.css'/>">
</head>
<body>
<div class="login-shell">
  <div class="login-lang lang-switch">
    <button class="active" data-lang="my">MY</button>
    <button data-lang="en">EN</button>
  </div>

  <div class="login-card">
    <div class="login-card-body">
      <div class="login-brand">
        <div class="brand-icon"><i class="bi bi-shield-lock-fill"></i></div>
        <h1 class="i18n" data-my="အက်ဒမင် လော့ဂ်အင်" data-en="Admin Login">အက်ဒမင် လော့ဂ်အင်</h1>
        <p class="i18n" data-my="ဆက်လက်ရှေ့ဆက်ရန် သင့်အကောင့်ဖြင့် ဝင်ရောက်ပါ" data-en="Sign in to continue to the admin panel">ဆက်လက်ရှေ့ဆက်ရန် သင့်အကောင့်ဖြင့် ဝင်ရောက်ပါ</p>
      </div>

      <div class="login-alert" id="loginAlert">
        <i class="bi bi-exclamation-triangle-fill"></i>
        <span></span>
      </div>

      <form id="loginForm" novalidate>
        <div class="form-group">
          <label class="i18n" data-my="အသုံးပြုသူအမည် / အီးမေးလ်" data-en="Username or Email">အသုံးပြုသူအမည် / အီးမေးလ်</label>
          <div class="input-wrap">
            <i class="bi bi-person-fill"></i>
            <input type="text" id="username" autocomplete="username"
                   placeholder="admin@broilerfarm.com"
                   data-my-ph="admin@broilerfarm.com" data-en-ph="admin@broilerfarm.com">
          </div>
        </div>

        <div class="form-group">
          <label class="i18n" data-my="စကားဝှက်" data-en="Password">စကားဝှက်</label>
          <div class="input-wrap">
            <i class="bi bi-lock-fill"></i>
            <input type="password" id="password" autocomplete="current-password"
                   placeholder="••••••••"
                   data-my-ph="••••••••" data-en-ph="••••••••">
          </div>
        </div>

        <button type="submit" class="btn btn-gold login-btn" id="loginBtn">
          <span id="loginBtnLabel">
            <i class="bi bi-box-arrow-in-right"></i>
            <span class="i18n" data-my="ဝင်ရောက်မည်" data-en="Login">ဝင်ရောက်မည်</span>
          </span>
        </button>
      </form>

      <p class="login-footnote i18n" data-my="© 2026 ဘရွိုင်လာ ကြက်မွေးမြူရေး စီမံခန့်ခွဲမှုစနစ် — Classic Emerald &amp; Gold Admin" data-en="© 2026 Broiler Farming System — Classic Emerald &amp; Gold Admin">© 2026 ဘရွိုင်လာ ကြက်မွေးမြူရေး စီမံခန့်ခွဲမှုစနစ် — Classic Emerald &amp; Gold Admin</p>
    </div>
  </div>
</div>

<script src="<c:url value='/admin/assets/js/login-page.js'/>"></script>
</body>
</html>
