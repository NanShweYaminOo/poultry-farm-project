/* ==========================================================================
   Broiler Farming System — Admin Login
   Authenticates against POST /api/v1/auth/login, stores the returned JWT
   in localStorage (read by admin-common.js on every other admin page) and
   redirects to the dashboard. Non-ADMIN accounts are rejected client-side
   even on a successful login, since this form is admin-only.
   ========================================================================== */

(function () {
  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function storeSession(auth) {
    localStorage.setItem('admin_token', auth.accessToken);
    localStorage.setItem('admin_token_type', auth.tokenType || 'Bearer');
    localStorage.setItem('admin_token_expires_at', String(Date.now() + auth.expiresInSeconds * 1000));
    localStorage.setItem('admin_user', JSON.stringify({
      userId: auth.userId,
      username: auth.username,
      role: auth.role
    }));
  }

  function errorText(status, serverMessage) {
    if (status === 401) {
      return lang() === 'en'
        ? 'Invalid username/email or password.'
        : 'အသုံးပြုသူအမည်/အီးမေးလ် (သို့) စကားဝှက် မှားနေပါသည်။';
    }
    if (status === 403) {
      return lang() === 'en'
        ? 'This account has been banned.'
        : 'ဤအကောင့်ကို ပိတ်ထားပါသည်။';
    }
    if (status === 0) {
      return lang() === 'en'
        ? 'Could not reach the server. Please try again.'
        : 'ဆာဗာနှင့် ဆက်သွယ်၍မရပါ။ ထပ်မံကြိုးစားပါ။';
    }
    return serverMessage || (lang() === 'en' ? 'Login failed.' : 'ဝင်ရောက်ခြင်း မအောင်မြင်ပါ။');
  }

  document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('loginForm');
    var btn = document.getElementById('loginBtn');
    var btnLabel = document.getElementById('loginBtnLabel');
    var alertBox = document.getElementById('loginAlert');

    function showAlert(text) {
      alertBox.querySelector('span').textContent = text;
      alertBox.classList.add('show');
    }

    function resetButton() {
      btn.disabled = false;
      btnLabel.innerHTML = '<i class="bi bi-box-arrow-in-right"></i> ' +
        '<span class="i18n" data-my="ဝင်ရောက်မည်" data-en="Login">' +
        (lang() === 'en' ? 'Login' : 'ဝင်ရောက်မည်') + '</span>';
    }

    form.addEventListener('submit', function (event) {
      event.preventDefault();

      var username = document.getElementById('username').value.trim();
      var password = document.getElementById('password').value.trim();

      if (!username || !password) {
        showAlert(lang() === 'en'
          ? 'Please enter both username/email and password.'
          : 'အသုံးပြုသူအမည်/အီးမေးလ်နှင့် စကားဝှက်ကို ဖြည့်စွက်ပါ။');
        return;
      }

      alertBox.classList.remove('show');
      btn.disabled = true;
      btnLabel.innerHTML = '<span class="spinner"></span> ' +
        (lang() === 'en' ? 'Signing in...' : 'ဝင်ရောက်နေသည်...');

      fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usernameOrEmail: username, password: password })
      })
        .then(function (response) {
          return response.json().catch(function () { return {}; }).then(function (body) {
            return { ok: response.ok, status: response.status, body: body };
          });
        })
        .then(function (result) {
          if (!result.ok) {
            showAlert(errorText(result.status, result.body && result.body.message));
            resetButton();
            return;
          }

          var auth = result.body;
          if (auth.role !== 'ADMIN') {
            showAlert(lang() === 'en'
              ? 'This account does not have admin access.'
              : 'ဤအကောင့်တွင် အက်ဒမင်ဝင်ရောက်ခွင့် မရှိပါ။');
            resetButton();
            return;
          }

          storeSession(auth);

          btnLabel.innerHTML = '<i class="bi bi-check-circle-fill"></i> ' +
            (lang() === 'en' ? 'Success!' : 'အောင်မြင်ပါသည်!');
          setTimeout(function () {
            window.location.href = '/admin/dashboard';
          }, 500);
        })
        .catch(function () {
          showAlert(errorText(0));
          resetButton();
        });
    });

    document.querySelectorAll('.login-lang button').forEach(function (b) {
      b.addEventListener('click', function () {
        document.querySelectorAll('.login-lang button').forEach(function (x) {
          x.classList.toggle('active', x === b);
        });
        var newLang = b.getAttribute('data-lang');
        document.documentElement.setAttribute('data-lang', newLang);
        document.querySelectorAll('.i18n').forEach(function (el) {
          var text = newLang === 'en' ? el.getAttribute('data-en') : el.getAttribute('data-my');
          if (text !== null) el.textContent = text;
        });
        var userInput = document.getElementById('username');
        userInput.placeholder = newLang === 'en'
          ? userInput.getAttribute('data-en-ph')
          : userInput.getAttribute('data-my-ph');
        var passInput = document.getElementById('password');
        passInput.placeholder = newLang === 'en'
          ? passInput.getAttribute('data-en-ph')
          : passInput.getAttribute('data-my-ph');
      });
    });
  });
})();
