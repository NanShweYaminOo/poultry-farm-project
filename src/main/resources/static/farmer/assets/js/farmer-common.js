/* ==========================================================================
   Broiler Farming System — Farmer Dashboard
   Session/auth/toast now delegate to the shared ApiClient (see
   shared/assets/js/api.js, loaded just before this file in
   fragments/scripts.jspf); this file adds the dashboard-specific extras:
   profile display, language switch, mobile sidebar toggle. window.DashboardUI
   keeps its exact previous shape so every other farmer page.js file is
   unaffected. Mirrors admin-common.js but reads/writes the farmer_* session
   keys written by public/assets/js/login-page.js and register-page.js (see
   its areaPrefixFor comment for why each area gets its own prefix), and
   bounces unauthenticated visitors to /login instead of /admin/login.
   ========================================================================== */

(function () {
  var client = window.ApiClient.create({ prefix: 'farmer', loginPath: '/login', toastId: 'dashboardToast' });

  function accountTypeLabel(accountType) {
    var lang = document.documentElement.getAttribute('data-lang') || 'my';
    if (accountType === 'FARMER') {
      return lang === 'en' ? 'Farmer' : 'တောင်သူ';
    }
    if (accountType === 'GUEST') {
      return lang === 'en' ? 'Guest' : 'ဧည့်သည်';
    }
    return accountType || '-';
  }

  function renderProfileFallback(session) {
    if (!session || !session.user) return;
    var nameEl = document.querySelector('.admin-profile .name');
    var avatarEl = document.querySelector('.admin-profile .avatar');
    var roleEl = document.getElementById('dashboardAccountTypeLabel');
    if (nameEl) nameEl.textContent = session.user.username;
    if (avatarEl) {
      var initials = session.user.username ? session.user.username.slice(0, 2).toUpperCase() : '?';
      avatarEl.textContent = initials;
    }
    if (roleEl) roleEl.textContent = accountTypeLabel(session.user.accountType);
  }

  function initProfileDisplay(session) {
    if (!session || !session.user) return;
    client.apiFetch('/api/v1/users/me')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load profile');
        return response.json();
      })
      .then(function (profile) {
        var nameEl = document.querySelector('.admin-profile .name');
        var avatarEl = document.querySelector('.admin-profile .avatar');
        var roleEl = document.getElementById('dashboardAccountTypeLabel');
        if (nameEl) nameEl.textContent = profile.fullName || profile.username;
        if (avatarEl) {
          if (profile.profileImageUrl) {
            avatarEl.innerHTML = '<img src="' + profile.profileImageUrl + '" style="width:100%;height:100%;object-fit:cover;border-radius:inherit;">';
          } else {
            var initials = profile.fullName ? profile.fullName.trim().slice(0, 2).toUpperCase() : '?';
            avatarEl.textContent = initials;
          }
        }
        if (roleEl) roleEl.textContent = accountTypeLabel(session.user.accountType);
      })
      .catch(function () {
        renderProfileFallback(session);
      });
  }

  function applyAccountTypeVisibility(session) {
    var premiumLink = document.getElementById('dashboardPremiumNavLink');
    if (!premiumLink || !session || !session.user) return;
    premiumLink.style.display = session.user.accountType === 'FARMER' ? '' : 'none';
  }

  function initLogout() {
    document.querySelectorAll('.nav-link-logout').forEach(function (link) {
      link.addEventListener('click', function () {
        client.clearSession();
      });
    });
  }

  function applyLang(lang) {
    document.documentElement.setAttribute('data-lang', lang);
    document.querySelectorAll('.i18n').forEach(function (el) {
      var text = lang === 'en' ? el.getAttribute('data-en') : el.getAttribute('data-my');
      if (text !== null) el.textContent = text;
    });
    document.querySelectorAll('.lang-switch button').forEach(function (btn) {
      btn.classList.toggle('active', btn.getAttribute('data-lang') === lang);
    });
    localStorage.setItem('dashboard_lang', lang);
    var session = client.getSession();
    var roleEl = document.getElementById('dashboardAccountTypeLabel');
    if (roleEl && session && session.user) roleEl.textContent = accountTypeLabel(session.user.accountType);
    document.dispatchEvent(new CustomEvent('dashboardlangchange', { detail: { lang: lang } }));
  }

  function initLangSwitch() {
    var lang = localStorage.getItem('dashboard_lang') || 'my';
    document.querySelectorAll('.lang-switch button').forEach(function (btn) {
      btn.addEventListener('click', function () {
        applyLang(btn.getAttribute('data-lang'));
      });
    });
    applyLang(lang);
  }

  function initMobileToggle() {
    var toggle = document.querySelector('.menu-toggle');
    var sidebar = document.getElementById('dashboardSidebar');
    if (!toggle || !sidebar) return;
    toggle.addEventListener('click', function () {
      sidebar.classList.toggle('open');
    });
  }

  window.DashboardUI = {
    getSession: client.getSession,
    clearSession: client.clearSession,
    authFetch: client.apiFetch,
    accountTypeLabel: accountTypeLabel,
    toast: client.toast
  };

  document.addEventListener('DOMContentLoaded', function () {
    var session = client.requireSession();
    if (!session) return;
    initProfileDisplay(session);
    applyAccountTypeVisibility(session);
    initLogout();
    initLangSwitch();
    initMobileToggle();
  });
})();
