/* ==========================================================================
   Broiler Farming System — Farmer/Guest Profile
   Loads the current user's real profile from /api/v1/users/me and wires
   Change Photo / Cancel / Save Changes to the real backend (profile info,
   password change, photo upload) via DashboardUI.authFetch. Mirrors
   admin/assets/js/profile-page.js exactly, just against window.GuestUI.
   ========================================================================== */

(function () {
  var currentProfile = null;

  var ROLE_BADGE_CLASS = {
    FREE: 'badge-muted',
    PAID: 'badge-emerald',
    ADMIN: 'badge-gold'
  };

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function renderAvatar(profileImageUrl, fullName) {
    var avatar = document.getElementById('profileAvatar');
    if (profileImageUrl) {
      avatar.innerHTML = '<img src="' + profileImageUrl + '" style="width:100%;height:100%;object-fit:cover;">';
    } else {
      avatar.textContent = fullName ? fullName.trim().slice(0, 2).toUpperCase() : '?';
    }
  }

  function renderProfile(profile) {
    currentProfile = profile;
    document.getElementById('profileFullName').textContent = profile.fullName;
    document.getElementById('profileEmail').textContent = profile.email;
    var roleEl = document.getElementById('profileRole');
    roleEl.textContent = profile.role;
    roleEl.className = 'badge mx-auto ' + (ROLE_BADGE_CLASS[profile.role] || 'badge-muted');
    roleEl.style.width = 'fit-content';
    document.getElementById('profileUserIdValue').textContent = profile.id;
    document.getElementById('fullNameInput').value = profile.fullName;
    document.getElementById('usernameInput').value = profile.username;
    document.getElementById('emailInput').value = profile.email;
    document.getElementById('phoneInput').value = profile.phoneNumber || '';
    renderAvatar(profile.profileImageUrl, profile.fullName);
  }

  function clearPasswordFields() {
    document.getElementById('currentPasswordInput').value = '';
    document.getElementById('newPasswordInput').value = '';
    document.getElementById('confirmPasswordInput').value = '';
  }

  function errorMessage(status, serverMessage, fallbackEn, fallbackMy) {
    if (serverMessage) return serverMessage;
    return lang() === 'en' ? fallbackEn : fallbackMy;
  }

  function loadProfile() {
    window.GuestUI.authFetch('/api/v1/users/me')
      .then(function (response) { return response.json(); })
      .then(renderProfile)
      .catch(function () {
        window.GuestUI.toast('ပရိုဖိုင်တင်ရန် မအောင်မြင်ပါ။', 'Failed to load profile.', 'bi-exclamation-triangle-fill');
      });
  }

  function initChangePhoto() {
    var btn = document.getElementById('changePhotoBtn');
    var input = document.getElementById('photoInput');

    btn.addEventListener('click', function () {
      input.click();
    });

    input.addEventListener('change', function () {
      var file = input.files[0];
      if (!file) return;

      var formData = new FormData();
      formData.append('photo', file);

      window.GuestUI.authFetch('/api/v1/users/me/photo', { method: 'POST', body: formData })
        .then(function (response) {
          return response.json().catch(function () { return {}; }).then(function (body) {
            return { ok: response.ok, status: response.status, body: body };
          });
        })
        .then(function (result) {
          if (!result.ok) {
            window.GuestUI.toast(
              errorMessage(result.status, result.body && result.body.message, 'Could not upload photo.', 'ပုံတင်၍ မရပါ။'),
              errorMessage(result.status, result.body && result.body.message, 'Could not upload photo.', 'ပုံတင်၍ မရပါ။'),
              'bi-exclamation-triangle-fill'
            );
            return;
          }
          renderProfile(result.body);
          window.GuestUI.toast('ပရိုဖိုင်ပုံ ပြောင်းလဲပြီးပါပြီ။', 'Profile photo updated.');
        })
        .catch(function () {
          window.GuestUI.toast('ပုံတင်၍ မရပါ။', 'Could not upload photo.', 'bi-exclamation-triangle-fill');
        })
        .finally(function () {
          input.value = '';
        });
    });
  }

  function initCancel() {
    document.getElementById('cancelBtn').addEventListener('click', function () {
      if (currentProfile) renderProfile(currentProfile);
      clearPasswordFields();
    });
  }

  function initSave() {
    var form = document.getElementById('profileForm');
    var saveBtn = document.getElementById('saveChangesBtn');

    form.addEventListener('submit', function (event) {
      event.preventDefault();

      var fullName = document.getElementById('fullNameInput').value.trim();
      var phoneNumber = document.getElementById('phoneInput').value.trim();
      var currentPassword = document.getElementById('currentPasswordInput').value;
      var newPassword = document.getElementById('newPasswordInput').value;
      var confirmPassword = document.getElementById('confirmPasswordInput').value;
      var wantsPasswordChange = currentPassword || newPassword || confirmPassword;

      if (!fullName || !phoneNumber) {
        window.GuestUI.toast('အမည်နှင့် ဖုန်းနံပါတ် ဖြည့်စွက်ပါ။', 'Full name and phone number are required.', 'bi-exclamation-triangle-fill');
        return;
      }
      if (wantsPasswordChange) {
        if (!currentPassword || !newPassword || !confirmPassword) {
          window.GuestUI.toast('စကားဝှက်ပြောင်းရန် ကွက်လပ်များအားလုံး ဖြည့်စွက်ပါ။', 'Fill in all password fields to change your password.', 'bi-exclamation-triangle-fill');
          return;
        }
        if (newPassword !== confirmPassword) {
          window.GuestUI.toast('စကားဝှက်အသစ် နှစ်ခု မတူညီပါ။', 'New password and confirmation do not match.', 'bi-exclamation-triangle-fill');
          return;
        }
        if (newPassword.length < 8) {
          window.GuestUI.toast('စကားဝှက်အသစ်သည် အနည်းဆုံး စာလုံး ၈ လုံး ရှိရမည်။', 'New password must be at least 8 characters.', 'bi-exclamation-triangle-fill');
          return;
        }
      }

      saveBtn.disabled = true;

      window.GuestUI.authFetch('/api/v1/users/me', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullName: fullName, phoneNumber: phoneNumber })
      })
        .then(function (response) {
          return response.json().catch(function () { return {}; }).then(function (body) {
            return { ok: response.ok, status: response.status, body: body };
          });
        })
        .then(function (result) {
          if (!result.ok) {
            throw new Error(errorMessage(result.status, result.body && result.body.message, 'Could not save profile.', 'ပရိုဖိုင် သိမ်းဆည်း၍ မရပါ။'));
          }
          renderProfile(result.body);

          if (!wantsPasswordChange) return null;

          return window.GuestUI.authFetch('/api/v1/users/me/password', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ currentPassword: currentPassword, newPassword: newPassword })
          }).then(function (response) {
            if (response.status === 204) return null;
            return response.json().catch(function () { return {}; }).then(function (body) {
              throw new Error(errorMessage(response.status, body && body.message, 'Could not change password.', 'စကားဝှက် ပြောင်းလဲ၍ မရပါ။'));
            });
          });
        })
        .then(function () {
          clearPasswordFields();
          window.GuestUI.toast('အပြောင်းအလဲများ သိမ်းဆည်းပြီးပါပြီ။', 'Changes saved.');
        })
        .catch(function (err) {
          window.GuestUI.toast(err.message, err.message, 'bi-exclamation-triangle-fill');
        })
        .finally(function () {
          saveBtn.disabled = false;
        });
    });
  }

  // -- Farmer upgrade request --------------------------------------------
  // Guest-only addition (Farmer profile has no equivalent -- Farmers are
  // already Farmers). Shows the caller's most recent request (if any) and
  // lets them submit a new one when they don't already have one PENDING.
  // Approval itself happens on the admin side (AdminAccountUpgradeRequestController);
  // this just files the request and reflects its status back.

  var UPGRADE_STATUS_LABEL = {
    PENDING: { en: 'Pending review', my: 'စိစစ်နေဆဲ' },
    APPROVED: { en: 'Approved', my: 'အတည်ပြုပြီး' },
    REJECTED: { en: 'Rejected', my: 'ပယ်ချခံရသည်' }
  };
  var UPGRADE_STATUS_BADGE = {
    PENDING: 'badge-muted',
    APPROVED: 'badge-emerald',
    REJECTED: 'badge-gold'
  };

  function renderUpgradeStatus(latest) {
    var statusEl = document.getElementById('upgradeRequestStatus');
    var form = document.getElementById('upgradeRequestForm');
    var submitBtn = document.getElementById('requestUpgradeBtn');
    if (!statusEl || !form) return;

    if (!latest) {
      statusEl.style.display = 'none';
      submitBtn.disabled = false;
      return;
    }

    var label = UPGRADE_STATUS_LABEL[latest.status] || { en: latest.status, my: latest.status };
    var badgeClass = UPGRADE_STATUS_BADGE[latest.status] || 'badge-muted';
    var noteHtml = latest.adminNote
      ? '<div style="font-size:12px;color:var(--muted);margin-top:6px;">' + (lang() === 'en' ? 'Admin note: ' : 'Admin မှတ်ချက်: ') + escapeHtmlLocal(latest.adminNote) + '</div>'
      : '';

    statusEl.style.display = '';
    statusEl.innerHTML = '<span class="badge ' + badgeClass + '">' + (lang() === 'en' ? label.en : label.my) + '</span>' + noteHtml;

    // Only block resubmission while a request is still PENDING -- a
    // REJECTED request should be resubmittable.
    submitBtn.disabled = latest.status === 'PENDING';
  }

  function escapeHtmlLocal(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function loadUpgradeRequestStatus() {
    window.GuestUI.authFetch('/api/v1/account-upgrade-requests/me')
      .then(function (response) { return response.ok ? response.json() : []; })
      .then(function (requests) {
        renderUpgradeStatus(requests && requests.length ? requests[0] : null);
      })
      .catch(function () {
        // Non-fatal -- the form still works even if the status fetch fails.
      });
  }

  function initUpgradeRequest() {
    var form = document.getElementById('upgradeRequestForm');
    if (!form) return;

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      var submitBtn = document.getElementById('requestUpgradeBtn');
      var reason = document.getElementById('upgradeReasonInput').value.trim();
      submitBtn.disabled = true;

      window.GuestUI.authFetch('/api/v1/account-upgrade-requests', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reason: reason })
      })
        .then(function (response) {
          return response.json().catch(function () { return {}; }).then(function (body) {
            return { ok: response.ok, status: response.status, body: body };
          });
        })
        .then(function (result) {
          if (!result.ok) {
            var message = (result.body && result.body.message) ||
              (lang() === 'en' ? 'Could not submit request.' : 'တောင်းဆိုချက် တင်၍ မရပါ။');
            window.GuestUI.toast(message, message, 'bi-exclamation-triangle-fill');
            submitBtn.disabled = false;
            return;
          }
          document.getElementById('upgradeReasonInput').value = '';
          renderUpgradeStatus(result.body);
          window.GuestUI.toast('တောင်းဆိုချက် တင်ပြီးပါပြီ။', 'Upgrade request submitted.');
        })
        .catch(function () {
          window.GuestUI.toast('တောင်းဆိုချက် တင်၍ မရပါ။', 'Could not submit request.', 'bi-exclamation-triangle-fill');
          submitBtn.disabled = false;
        });
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadProfile();
    initChangePhoto();
    initCancel();
    initSave();
    initUpgradeRequest();
    loadUpgradeRequestStatus();
  });
})();
