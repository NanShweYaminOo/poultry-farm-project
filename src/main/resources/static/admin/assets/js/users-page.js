/* ==========================================================================
   Broiler Farming System — Admin Frontend
   User Management & Toxicity Review (users.jsp)
   ========================================================================== */

(function () {
  var users = [
    { id: 1, name: 'ဦးအောင်ကျော်', email: 'aungkyaw@broilerfarm.com', role: 'PAID', banned: false, flagged: false, joined: '၁၂-၀၁-၂၀၂၅', flaggedContent: null },
    { id: 2, name: 'မသီတာဝင်း', email: 'thidarwin@broilerfarm.com', role: 'FREE', banned: false, flagged: true, joined: '၀၃-၀၃-၂၀၂၅', flaggedContent: 'ကိုယ်ရေးကိုယ်တာ ကျိန်ဆဲသည့် စကားလုံးများပါဝင်သော မှတ်ချက်တစ်ခုကို ပို့စ်တွင် ချန်ခဲ့သည်။' },
    { id: 3, name: 'ကိုမင်းသူ', email: 'minthu@broilerfarm.com', role: 'PAID', banned: false, flagged: false, joined: '၂၂-၀၅-၂၀၂၄', flaggedContent: null },
    { id: 4, name: 'ဒေါ်နှင်းနှင်းအေး', email: 'nweaye@broilerfarm.com', role: 'FREE', banned: true, flagged: false, joined: '၀၉-၀၉-၂၀၂၄', flaggedContent: null },
    { id: 5, name: 'ကိုဇော်လင်း', email: 'zawlin@broilerfarm.com', role: 'ADMIN', banned: false, flagged: false, joined: '၀၁-၀၁-၂၀၂၃', flaggedContent: null },
    { id: 6, name: 'မခင်ဇာဇာ', email: 'khinzarzar@broilerfarm.com', role: 'FREE', banned: false, flagged: true, joined: '၁၅-၁၁-၂၀၂၅', flaggedContent: 'အခြားအသုံးပြုသူတစ်ဦးအား ခြိမ်းခြောက်သည့်သဘောသက်ရောက်သော စကားပြောဆိုမှု တစ်ခု တွေ့ရှိရသည်။' },
    { id: 7, name: 'ကိုစိုးမင်းထွန်း', email: 'soemint@broilerfarm.com', role: 'PAID', banned: true, flagged: true, joined: '၀၅-၀၆-၂၀၂၄', flaggedContent: 'ရောင်းချမည့်ပို့စ်တွင် လိမ်လည်လှည့်ဖြားမှုရှိသည်ဟု အသုံးပြုသူများစွာမှ တိုင်ကြားခဲ့သည်။' },
    { id: 8, name: 'ဒေါ်ခင်မာလေး', email: 'khinmalay@broilerfarm.com', role: 'FREE', banned: false, flagged: false, joined: '၂၈-၀၂-၂၀၂၅', flaggedContent: null }
  ];

  var currentFilter = 'all';
  var currentSearch = '';
  var reviewUserId = null;
  var reviewModalEl = document.getElementById('reviewModal');
  var reviewModal = reviewModalEl ? new bootstrap.Modal(reviewModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function roleBadge(role) {
    if (role === 'ADMIN') return '<span class="badge badge-gold">ADMIN</span>';
    if (role === 'PAID') return '<span class="badge badge-emerald">PAID</span>';
    return '<span class="badge badge-muted">FREE</span>';
  }

  function statusBadges(u) {
    var html = u.banned
      ? '<span class="badge badge-danger-soft"><i class="bi bi-slash-circle-fill"></i> ' + (lang() === 'en' ? 'Banned' : 'ပိတ်ပင်ထားသည်') + '</span>'
      : '<span class="badge badge-emerald"><i class="bi bi-check-circle-fill"></i> ' + (lang() === 'en' ? 'Active' : 'အသက်ဝင်နေသည်') + '</span>';
    if (u.flagged) {
      html += ' <span class="badge badge-gold"><i class="bi bi-flag-fill"></i> ' + (lang() === 'en' ? 'Flagged' : 'အမှတ်အသား') + '</span>';
    }
    return html;
  }

  function actionButtons(u) {
    var btns = '';
    if (u.flagged) {
      btns += '<button class="btn btn-outline btn-sm btn-review" data-id="' + u.id + '"><i class="bi bi-search"></i> ' +
        (lang() === 'en' ? 'Review' : 'စစ်ဆေးရန်') + '</button> ';
    }
    btns += u.banned
      ? '<button class="btn btn-gold btn-sm btn-toggle-ban" data-id="' + u.id + '"><i class="bi bi-unlock-fill"></i> ' + (lang() === 'en' ? 'Unban' : 'ပြန်ဖွင့်မည်') + '</button>'
      : '<button class="btn btn-danger btn-sm btn-toggle-ban" data-id="' + u.id + '"><i class="bi bi-slash-circle"></i> ' + (lang() === 'en' ? 'Ban' : 'ပိတ်ပင်မည်') + '</button>';
    return btns;
  }

  function render() {
    var body = document.getElementById('usersTableBody');
    var filtered = users.filter(function (u) {
      var matchesFilter = currentFilter === 'all' ||
        (currentFilter === 'flagged' && u.flagged) ||
        (currentFilter === 'banned' && u.banned);
      var haystack = (u.name + ' ' + u.email).toLowerCase();
      var matchesSearch = haystack.indexOf(currentSearch.toLowerCase()) !== -1;
      return matchesFilter && matchesSearch;
    });

    document.getElementById('userResultsCount').textContent =
      filtered.length + (lang() === 'en' ? ' result(s)' : ' ဦး တွေ့ရှိသည်');

    if (filtered.length === 0) {
      body.innerHTML = '<tr><td colspan="6"><div class="empty-state"><i class="bi bi-people"></i><h3>' +
        (lang() === 'en' ? 'No users found' : 'အသုံးပြုသူများ မတွေ့ရှိပါ') + '</h3></div></td></tr>';
      return;
    }

    body.innerHTML = filtered.map(function (u) {
      var initials = u.name.charAt(0);
      return (
        '<tr data-id="' + u.id + '">' +
        '<td><div class="d-flex align-items-center gap-2">' +
        '<div class="post-avatar" style="width:34px;height:34px;font-size:12px;">' + initials + '</div>' +
        '<span style="font-weight:600;">' + u.name + '</span></div></td>' +
        '<td style="color:var(--muted);">' + u.email + '</td>' +
        '<td>' + roleBadge(u.role) + '</td>' +
        '<td>' + statusBadges(u) + '</td>' +
        '<td style="color:var(--muted);">' + u.joined + '</td>' +
        '<td class="text-end">' + actionButtons(u) + '</td>' +
        '</tr>'
      );
    }).join('');
  }

  document.addEventListener('DOMContentLoaded', function () {
    render();

    document.getElementById('userFilterTabs').addEventListener('click', function (e) {
      var btn = e.target.closest('button[data-filter]');
      if (!btn) return;
      currentFilter = btn.getAttribute('data-filter');
      document.querySelectorAll('#userFilterTabs button').forEach(function (b) {
        b.classList.toggle('active', b === btn);
      });
      render();
    });

    document.getElementById('userSearchInput').addEventListener('input', function (e) {
      currentSearch = e.target.value;
      render();
    });

    document.getElementById('usersTableBody').addEventListener('click', function (e) {
      var reviewBtn = e.target.closest('.btn-review');
      var banBtn = e.target.closest('.btn-toggle-ban');

      if (reviewBtn) {
        reviewUserId = Number(reviewBtn.getAttribute('data-id'));
        var u = users.find(function (x) { return x.id === reviewUserId; });
        document.getElementById('reviewUserName').textContent = u.name;
        document.getElementById('reviewContent').textContent = u.flaggedContent || '';
        reviewModal.show();
      } else if (banBtn) {
        var id = Number(banBtn.getAttribute('data-id'));
        var user = users.find(function (x) { return x.id === id; });
        user.banned = !user.banned;
        render();
        window.AdminUI.toast(
          user.banned ? 'အသုံးပြုသူကို ပိတ်ပင်လိုက်ပါပြီ' : 'အသုံးပြုသူကို ပြန်ဖွင့်ပေးလိုက်ပါပြီ',
          user.banned ? 'User has been banned' : 'User has been unbanned',
          user.banned ? 'bi-slash-circle-fill' : 'bi-unlock-fill'
        );
      }
    });

    document.getElementById('clearFlagBtn').addEventListener('click', function () {
      var u = users.find(function (x) { return x.id === reviewUserId; });
      if (u) { u.flagged = false; }
      reviewModal.hide();
      render();
      window.AdminUI.toast('အမှတ်အသားကို ဖယ်ရှားပြီးပါပြီ', 'Flag cleared', 'bi-check-circle-fill');
    });

    document.getElementById('banFromReviewBtn').addEventListener('click', function () {
      var u = users.find(function (x) { return x.id === reviewUserId; });
      if (u) { u.banned = true; }
      reviewModal.hide();
      render();
      window.AdminUI.toast('အသုံးပြုသူကို ပိတ်ပင်လိုက်ပါပြီ', 'User has been banned', 'bi-slash-circle-fill');
    });

    document.addEventListener('adminlangchange', function (e) {
      var input = document.getElementById('userSearchInput');
      input.placeholder = e.detail.lang === 'en' ? input.getAttribute('data-en-ph') : input.getAttribute('data-my-ph');
      render();
    });
  });
})();
