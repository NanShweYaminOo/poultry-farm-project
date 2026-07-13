<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Register | Broiler Farming System" />
<!DOCTYPE html>
<html lang="my" data-lang="my">
<head>
<%@ include file="fragments/head.jspf" %>
</head>
<body>
<div class="login-shell">
  <div class="login-lang lang-switch">
    <button class="active" data-lang="my">MY</button>
    <button data-lang="en">EN</button>
  </div>

  <div class="login-card register-card">
    <div class="login-card-body">
      <div class="login-brand">
        <div class="brand-icon"><i class="bi bi-egg-fried"></i></div>
        <h1 class="i18n" data-my="အကောင့်ဖွင့်မည်" data-en="Create Account">အကောင့်ဖွင့်မည်</h1>
        <p class="i18n" data-my="ဘရွိုင်လာ ကြက်မွေးမြူရေးစနစ်တွင် ပါဝင်ပါ" data-en="Join the Broiler Farming System">ဘရွိုင်လာ ကြက်မွေးမြူရေးစနစ်တွင် ပါဝင်ပါ</p>
      </div>

      <div class="login-alert" id="registerAlert">
        <i class="bi bi-exclamation-triangle-fill"></i>
        <span></span>
      </div>

      <form id="registerForm" novalidate>
        <div class="form-group">
          <label class="i18n" data-my="အကောင့်အမျိုးအစား" data-en="Account Type">အကောင့်အမျိုးအစား</label>
          <div class="account-type-group" id="accountTypeGroup">
            <button type="button" class="account-type-option active" data-account-type="FARMER">
              <i class="bi bi-person-workspace"></i>
              <span class="i18n" data-my="တောင်သူ" data-en="Farmer">တောင်သူ</span>
            </button>
            <button type="button" class="account-type-option" data-account-type="GUEST">
              <i class="bi bi-person"></i>
              <span class="i18n" data-my="ဧည့်သည်" data-en="Guest">ဧည့်သည်</span>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label class="i18n" data-my="အမည်အပြည့်အစုံ" data-en="Full Name">အမည်အပြည့်အစုံ</label>
          <div class="input-wrap">
            <i class="bi bi-person-fill"></i>
            <input type="text" id="fullName" autocomplete="name"
                   placeholder="Aung Aung" data-my-ph="အောင်အောင်" data-en-ph="Aung Aung">
          </div>
        </div>

        <div class="form-group">
          <label class="i18n" data-my="အသုံးပြုသူအမည်" data-en="Username">အသုံးပြုသူအမည်</label>
          <div class="input-wrap">
            <i class="bi bi-at"></i>
            <input type="text" id="username" autocomplete="username" placeholder="aungaung01">
          </div>
        </div>

        <div class="form-group">
          <label class="i18n" data-my="ဖုန်းနံပါတ်" data-en="Phone Number">ဖုန်းနံပါတ်</label>
          <div class="input-wrap">
            <i class="bi bi-telephone-fill"></i>
            <input type="text" id="phoneNumber" autocomplete="tel" placeholder="09xxxxxxxxx">
          </div>
        </div>

        <div class="form-group">
          <label class="i18n" data-my="အီးမေးလ်" data-en="Email">အီးမေးလ်</label>
          <div class="input-wrap">
            <i class="bi bi-envelope-fill"></i>
            <input type="email" id="email" autocomplete="email" placeholder="you@example.com">
          </div>
        </div>

        <div class="form-group">
          <label class="i18n" data-my="တည်နေရာ (ရွေးချယ်ရန်)" data-en="Location (optional)">တည်နေရာ (ရွေးချယ်ရန်)</label>
          <div class="input-wrap">
            <i class="bi bi-geo-alt-fill"></i>
            <input type="text" id="location" autocomplete="address-level2" placeholder="Yangon">
          </div>
        </div>

        <div class="form-group">
          <label class="i18n" data-my="စကားဝှက်" data-en="Password">စကားဝှက်</label>
          <div class="input-wrap">
            <i class="bi bi-lock-fill"></i>
            <input type="password" id="password" autocomplete="new-password"
                   placeholder="••••••••" data-my-ph="••••••••" data-en-ph="••••••••">
          </div>
        </div>

        <div class="form-group">
          <label class="i18n" data-my="စကားဝှက် အတည်ပြုရန်" data-en="Confirm Password">စကားဝှက် အတည်ပြုရန်</label>
          <div class="input-wrap">
            <i class="bi bi-lock-fill"></i>
            <input type="password" id="confirmPassword" autocomplete="new-password"
                   placeholder="••••••••" data-my-ph="••••••••" data-en-ph="••••••••">
          </div>
        </div>

        <button type="submit" class="btn btn-gold login-btn" id="registerBtn">
          <span id="registerBtnLabel">
            <i class="bi bi-person-plus-fill"></i>
            <span class="i18n" data-my="အကောင့်ဖွင့်မည်" data-en="Create Account">အကောင့်ဖွင့်မည်</span>
          </span>
        </button>
      </form>

      <p class="login-footnote">
        <span class="i18n" data-my="အကောင့်ရှိပြီးသားလား?" data-en="Already have an account?">အကောင့်ရှိပြီးသားလား?</span>
        <a href="<c:url value='/login'/>" class="i18n" data-my="ဝင်ရောက်ရန်" data-en="Login">ဝင်ရောက်ရန်</a>
      </p>
    </div>
  </div>
</div>

<script src="<c:url value='/public/assets/js/register-page.js'/>"></script>
</body>
</html>
