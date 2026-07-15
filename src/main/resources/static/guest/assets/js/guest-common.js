/* ==========================================================================
   Broiler Farming System — Guest Dashboard
   Session/auth/toast delegate to the shared ApiClient (see
   shared/assets/js/api.js, loaded just before this file in
   fragments/scripts.jspf); this file adds the guest-specific extras:
   profile display, language switch, mobile sidebar toggle. Mirrors
   farmer/assets/js/farmer-common.js but exposes window.GuestUI instead of
   window.DashboardUI (Guest and Farmer are separate areas now, not one
   shared "dashboard") and reads/writes the guest_* session keys written by
   public/assets/js/login-page.js and register-page.js.
   ========================================================================== */

(function () {
  var client = window.ApiClient.create({ prefix: 'guest', loginPath: '/login', toastId: 'guestToast' });

  function renderProfileFallback(session) {
    if (!session || !session.user) return;
    var nameEl = document.querySelector('.admin-profile .name');
    var avatarEl = document.querySelector('.admin-profile .avatar');
    if (nameEl) nameEl.textContent = session.user.username;
    if (avatarEl) {
      var initials = session.user.username ? session.user.username.slice(0, 2).toUpperCase() : '?';
      avatarEl.textContent = initials;
    }
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
        if (nameEl) nameEl.textContent = profile.fullName || profile.username;
        if (avatarEl) {
          if (profile.profileImageUrl) {
            avatarEl.innerHTML = '<img src="' + profile.profileImageUrl + '" style="width:100%;height:100%;object-fit:cover;border-radius:inherit;">';
          } else {
            var initials = profile.fullName ? profile.fullName.trim().slice(0, 2).toUpperCase() : '?';
            avatarEl.textContent = initials;
          }
        }
      })
      .catch(function () {
        renderProfileFallback(session);
      });
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

  window.GuestUI = {
    getSession: client.getSession,
    clearSession: client.clearSession,
    authFetch: client.apiFetch,
    toast: client.toast
  };

  document.addEventListener('DOMContentLoaded', function () {
    var session = client.requireSession();
    if (!session) return;
    initProfileDisplay(session);
    initLogout();
    initLangSwitch();
    initMobileToggle();
  });
})();
