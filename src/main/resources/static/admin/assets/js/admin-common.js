/* ==========================================================================
   Broiler Farming System — Admin Frontend
   Shared language switcher, mobile sidebar toggle and toast helper.
   The sidebar/topbar markup itself lives in the JSP fragments
   (WEB-INF/jsp/admin/fragments/*.jsp) and is rendered server-side.
   ========================================================================== */

(function () {
  var TOKEN_KEY = 'admin_token';
  var TOKEN_TYPE_KEY = 'admin_token_type';
  var TOKEN_EXPIRES_KEY = 'admin_token_expires_at';
  var USER_KEY = 'admin_user';

  function getSession() {
    var token = localStorage.getItem(TOKEN_KEY);
    var expiresAt = Number(localStorage.getItem(TOKEN_EXPIRES_KEY) || 0);
    if (!token || !expiresAt || Date.now() >= expiresAt) {
      return null;
    }
    var user = null;
    try {
      user = JSON.parse(localStorage.getItem(USER_KEY) || 'null');
    } catch (e) {
      user = null;
    }
    return { token: token, tokenType: localStorage.getItem(TOKEN_TYPE_KEY) || 'Bearer', user: user };
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(TOKEN_TYPE_KEY);
    localStorage.removeItem(TOKEN_EXPIRES_KEY);
    localStorage.removeItem(USER_KEY);
  }

  function requireSession() {
    var session = getSession();
    if (!session) {
      clearSession();
      window.location.href = '/admin/login';
      return null;
    }
    return session;
  }

  // Attaches the stored JWT as an Authorization header; clears the
  // session and bounces to login if the API rejects it as expired/invalid.
  function authFetch(url, options) {
    var session = getSession();
    options = options || {};
    options.headers = Object.assign({}, options.headers, session
      ? { Authorization: session.tokenType + ' ' + session.token }
      : {});
    return fetch(url, options).then(function (response) {
      if (response.status === 401) {
        clearSession();
        window.location.href = '/admin/login';
      }
      return response;
    });
  }

  function renderProfileFallback(session) {
    if (!session || !session.user) return;
    var nameEl = document.querySelector('.admin-profile .name');
    var avatarEl = document.querySelector('.admin-profile .avatar');
    if (nameEl) nameEl.textContent = session.user.username;
    if (avatarEl) {
      var initials = session.user.username ? session.user.username.slice(0, 2).toUpperCase() : 'AD';
      avatarEl.textContent = initials;
    }
  }

  function initProfileDisplay(session) {
    if (!session || !session.user) return;
    authFetch('/api/v1/users/me')
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
            var initials = profile.fullName ? profile.fullName.trim().slice(0, 2).toUpperCase() : 'AD';
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
        clearSession();
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
    localStorage.setItem('admin_lang', lang);
    document.dispatchEvent(new CustomEvent('adminlangchange', { detail: { lang: lang } }));
  }

  function initLangSwitch() {
    var lang = localStorage.getItem('admin_lang') || 'my';
    document.querySelectorAll('.lang-switch button').forEach(function (btn) {
      btn.addEventListener('click', function () {
        applyLang(btn.getAttribute('data-lang'));
      });
    });
    applyLang(lang);
  }

  function initMobileToggle() {
    var toggle = document.querySelector('.menu-toggle');
    var sidebar = document.getElementById('adminSidebar');
    if (!toggle || !sidebar) return;
    toggle.addEventListener('click', function () {
      sidebar.classList.toggle('open');
    });
  }

  window.AdminUI = {
    getSession: getSession,
    clearSession: clearSession,
    authFetch: authFetch,
    toast: function (myText, enText, icon) {
      var el = document.getElementById('adminToast');
      if (!el) {
        el = document.createElement('div');
        el.id = 'adminToast';
        el.className = 'toast';
        document.body.appendChild(el);
      }
      var lang = document.documentElement.getAttribute('data-lang') || 'my';
      el.innerHTML = '<i class="bi ' + (icon || 'bi-check-circle-fill') + '"></i><span>' + (lang === 'en' ? enText : myText) + '</span>';
      el.classList.add('show');
      clearTimeout(el._timer);
      el._timer = setTimeout(function () {
        el.classList.remove('show');
      }, 2600);
    }
  };

  document.addEventListener('DOMContentLoaded', function () {
    var session = requireSession();
    if (!session) return;
    initProfileDisplay(session);
    initLogout();
    initLangSwitch();
    initMobileToggle();
  });
})();
