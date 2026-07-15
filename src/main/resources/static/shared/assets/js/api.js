/* ==========================================================================
   Broiler Farming System — Shared API Client
   One apiFetch implementation, parameterized per app area (admin vs
   dashboard) by token-storage prefix, login redirect path, and toast
   element id. admin-common.js and dashboard-common.js each create one
   instance of this via ApiClient.create(...) and re-export it as
   window.AdminUI / window.DashboardUI, so every existing page.js file's
   calling convention (window.AdminUI.authFetch, window.AdminUI.toast, etc.)
   is completely unchanged.
   ========================================================================== */
(function () {
  function create(config) {
    var TOKEN_KEY = config.prefix + '_token';
    var TOKEN_TYPE_KEY = config.prefix + '_token_type';
    var TOKEN_EXPIRES_KEY = config.prefix + '_token_expires_at';
    var USER_KEY = config.prefix + '_user';
    var loginPath = config.loginPath;
    var toastId = config.toastId;

    function lang() {
      return document.documentElement.getAttribute('data-lang') || 'my';
    }

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

    function storeSession(auth) {
      localStorage.setItem(TOKEN_KEY, auth.accessToken);
      localStorage.setItem(TOKEN_TYPE_KEY, auth.tokenType || 'Bearer');
      localStorage.setItem(TOKEN_EXPIRES_KEY, String(Date.now() + auth.expiresInSeconds * 1000));
      localStorage.setItem(USER_KEY, JSON.stringify({
        userId: auth.userId,
        username: auth.username,
        role: auth.role,
        accountType: auth.accountType
      }));
    }

    function clearSession() {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(TOKEN_TYPE_KEY);
      localStorage.removeItem(TOKEN_EXPIRES_KEY);
      localStorage.removeItem(USER_KEY);
      // Also clears this area's own auth cookie (admin_auth_token /
      // farmer_auth_token / guest_auth_token) that gates the server-rendered
      // page shells -- localStorage removal alone doesn't touch it. ?area=
      // tells the server which one, since a browser can legitimately hold
      // all three at once (independent Admin/Farmer/Guest sessions in
      // different tabs) and logging out of one must not touch the others.
      fetch('/api/v1/auth/logout?area=' + encodeURIComponent(config.prefix), { method: 'POST', keepalive: true }).catch(function () {});
    }

    function requireSession() {
      var session = getSession();
      if (!session) {
        clearSession();
        window.location.href = loginPath;
        return null;
      }
      return session;
    }

    function toast(myText, enText, icon) {
      var el = document.getElementById(toastId);
      if (!el) {
        el = document.createElement('div');
        el.id = toastId;
        el.className = 'toast';
        document.body.appendChild(el);
      }
      el.innerHTML = '<i class="bi ' + (icon || 'bi-check-circle-fill') + '"></i><span>' +
        (lang() === 'en' ? enText : myText) + '</span>';
      el.classList.add('show');
      clearTimeout(el._timer);
      el._timer = setTimeout(function () { el.classList.remove('show'); }, 2600);
    }

    // Attaches the stored JWT as an Authorization header on every call.
    // - 401 (expired/invalid token, or banned mid-session -- JwtAuthenticationFilter
    //   re-resolves the user from the DB on every request): clears the
    //   session and redirects to login.
    // - 403 (wrong role, or a business-rule UnauthorizedActionException):
    //   shows the server's own message as a toast. Peeks at a *clone* of the
    //   body so the caller's own .json()/.then() handling of the same
    //   response is completely unaffected -- this never replaces the
    //   caller's own status handling, it just adds a consistent baseline.
    function apiFetch(url, options) {
      var session = getSession();
      options = options || {};
      options.headers = Object.assign({}, options.headers, session
        ? { Authorization: session.tokenType + ' ' + session.token }
        : {});
      return fetch(url, options).then(function (response) {
        if (response.status === 401) {
          clearSession();
          window.location.href = loginPath;
          return response;
        }
        if (response.status === 403) {
          response.clone().json().catch(function () { return {}; }).then(function (body) {
            var message = body && body.message;
            toast(
              message || 'သင့်တွင် ဤလုပ်ဆောင်ချက်အတွက် ခွင့်ပြုချက်မရှိပါ။',
              message || 'You do not have permission to do this.',
              'bi-shield-lock-fill'
            );
          });
        }
        return response;
      });
    }

    // Centralizes the '.json().catch(() => ({}))' boilerplate duplicated
    // across every page.js file -- returns { ok, status, body }, where body
    // is always the parsed ApiError/DTO JSON (or {} if unparsable). The
    // ApiError shape from GlobalExceptionHandler is always
    // { timestamp, status, error, message }.
    function readJson(response) {
      return response.json().catch(function () { return {}; }).then(function (body) {
        return { ok: response.ok, status: response.status, body: body };
      });
    }

    return {
      getSession: getSession,
      storeSession: storeSession,
      clearSession: clearSession,
      requireSession: requireSession,
      apiFetch: apiFetch,
      readJson: readJson,
      toast: toast,
      lang: lang
    };
  }

  window.ApiClient = { create: create };
})();
