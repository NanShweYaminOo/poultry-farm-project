/* ==========================================================================
   Broiler Farming System — Public Register (Guest / Farmer)
   Posts to POST /api/v1/auth/register (which auto-logs-in on success) and
   stores the returned JWT the same way login-page.js does.
   ========================================================================== */

(function () {
  var MIN_PASSWORD_LENGTH = 8;
  var selectedAccountType = 'FARMER';

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function storeSession(auth) {
    localStorage.setItem('user_token', auth.accessToken);
    localStorage.setItem('user_token_type', auth.tokenType || 'Bearer');
    localStorage.setItem('user_token_expires_at', String(Date.now() + auth.expiresInSeconds * 1000));
    localStorage.setItem('user_user', JSON.stringify({
      userId: auth.userId,
      username: auth.username,
      role: auth.role,
      accountType: auth.accountType
    }));
  }

  function errorText(status, serverMessage) {
    if (status === 409) {
      return serverMessage || (lang() === 'en'
        ? 'That username or email is already taken.'
        : 'ဤအသုံးပြုသူအမည် (သို့) အီးမေးလ်ကို အသုံးပြုပြီးသားဖြစ်သည်။');
    }
    if (status === 400) {
      return serverMessage || (lang() === 'en'
        ? 'Please check the details you entered.'
        : 'ဖြည့်စွက်ထားသော အချက်အလက်များကို စစ်ဆေးပါ။');
    }
    if (status === 0) {
      return lang() === 'en'
        ? 'Could not reach the server. Please try again.'
        : 'ဆာဗာနှင့် ဆက်သွယ်၍မရပါ။ ထပ်မံကြိုးစားပါ။';
    }
    return serverMessage || (lang() === 'en' ? 'Registration failed.' : 'အကောင့်ဖွင့်ခြင်း မအောင်မြင်ပါ။');
  }

  document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('registerForm');
    var btn = document.getElementById('registerBtn');
    var btnLabel = document.getElementById('registerBtnLabel');
    var alertBox = document.getElementById('registerAlert');
    var accountTypeGroup = document.getElementById('accountTypeGroup');

    function showAlert(text, isSuccess) {
      alertBox.classList.toggle('success', !!isSuccess);
      alertBox.querySelector('i').className = isSuccess ? 'bi bi-check-circle-fill' : 'bi bi-exclamation-triangle-fill';
      alertBox.querySelector('span').textContent = text;
      alertBox.classList.add('show');
    }

    function resetButton() {
      btn.disabled = false;
      btnLabel.innerHTML = '<i class="bi bi-person-plus-fill"></i> ' +
        '<span class="i18n" data-my="အကောင့်ဖွင့်မည်" data-en="Create Account">' +
        (lang() === 'en' ? 'Create Account' : 'အကောင့်ဖွင့်မည်') + '</span>';
    }

    if (accountTypeGroup) {
      accountTypeGroup.querySelectorAll('.account-type-option').forEach(function (option) {
        option.addEventListener('click', function () {
          accountTypeGroup.querySelectorAll('.account-type-option').forEach(function (o) {
            o.classList.toggle('active', o === option);
          });
          selectedAccountType = option.getAttribute('data-account-type');
        });
      });
    }

    form.addEventListener('submit', function (event) {
      event.preventDefault();

      var fullName = document.getElementById('fullName').value.trim();
      var username = document.getElementById('username').value.trim();
      var phoneNumber = document.getElementById('phoneNumber').value.trim();
      var email = document.getElementById('email').value.trim();
      var location = document.getElementById('location').value.trim();
      var password = document.getElementById('password').value;
      var confirmPassword = document.getElementById('confirmPassword').value;

      if (!fullName || !username || !phoneNumber || !email || !password) {
        showAlert(lang() === 'en'
          ? 'Please fill in all required fields.'
          : 'လိုအပ်သော အချက်အလက်များအားလုံးကို ဖြည့်စွက်ပါ။', false);
        return;
      }
      if (password.length < MIN_PASSWORD_LENGTH) {
        showAlert(lang() === 'en'
          ? 'Password must be at least 8 characters.'
          : 'စကားဝှက်သည် အနည်းဆုံး စာလုံး ၈ လုံး ရှိရမည်။', false);
        return;
      }
      if (password !== confirmPassword) {
        showAlert(lang() === 'en'
          ? 'Passwords do not match.'
          : 'စကားဝှက်များ မတူညီပါ။', false);
        return;
      }

      alertBox.classList.remove('show');
      btn.disabled = true;
      btnLabel.innerHTML = '<span class="spinner"></span> ' +
        (lang() === 'en' ? 'Creating account...' : 'အကောင့်ဖွင့်နေသည်...');

      fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fullName: fullName,
          username: username,
          phoneNumber: phoneNumber,
          email: email,
          password: password,
          location: location,
          preferredLanguage: lang(),
          accountType: selectedAccountType
        })
      })
        .then(function (response) {
          return response.json().catch(function () { return {}; }).then(function (body) {
            return { ok: response.ok, status: response.status, body: body };
          });
        })
        .then(function (result) {
          if (!result.ok) {
            showAlert(errorText(result.status, result.body && result.body.message), false);
            resetButton();
            return;
          }

          var auth = result.body;
          storeSession(auth);

          form.reset();
          btnLabel.innerHTML = '<i class="bi bi-check-circle-fill"></i> ' +
            (lang() === 'en' ? 'Success!' : 'အောင်မြင်ပါသည်!');
          showAlert(
            (lang() === 'en' ? 'Account created — welcome, ' : 'အကောင့်ဖွင့်ပြီးပါပြီ — ကြိုဆိုပါသည်, ') + auth.username + '!',
            true
          );
        })
        .catch(function () {
          showAlert(errorText(0), false);
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
        document.querySelectorAll('[data-my-ph]').forEach(function (input) {
          input.placeholder = newLang === 'en'
            ? input.getAttribute('data-en-ph')
            : input.getAttribute('data-my-ph');
        });
      });
    });
  });
})();
