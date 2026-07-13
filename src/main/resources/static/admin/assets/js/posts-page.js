/* ==========================================================================
   Broiler Farming System — Admin Frontend
   User Posts Management (posts.jsp)
   Feed of public sales posts + chat/buy requests with moderation delete.
   ========================================================================== */

(function () {
  var posts = [
    {
      id: 1, type: 'sale', name: 'ဦးအောင်ကျော်', initials: 'အက',
      time: '၂ နာရီအရင်', title: 'ဘရွိုင်လာကြက် ၅၀၀ ကောင် ရောင်းရန်ရှိသည်',
      content: 'အသက် ၄၀ ရက်သား ကျန်းမာသန်စွမ်းသော ဘရွိုင်လာကြက်များ အထုပ်ကြီးဖြင့် ရောင်းချမည်။ တစ်ပြိုင်နက် ဝယ်ယူပါက စျေးနှုန်း လျှော့ပေးပါမည်။',
      price: '၈,၅၀၀ ကျပ်/ကီလို'
    },
    {
      id: 2, type: 'request', name: 'မသီတာဝင်း', initials: 'သဝ',
      time: '၅ နာရီအရင်', title: 'ကြက်ကုန်ကျစရိတ်နှင့် ကျွေးမွေးနည်း ဆွေးနွေးလိုပါသည်',
      content: 'ကြက်ဆေးထိုးရန် အချိန်ဇယားနှင့် အစားအစာ ရွေးချယ်မှုနှင့် ပတ်သက်၍ အတွေ့အကြုံရှိသူများနှင့် တိုက်ရိုက် စကားပြောလိုပါသည်။',
      quantity: '၁၀၀'
    },
    {
      id: 3, type: 'sale', name: 'ကိုမင်းသူ', initials: 'ကမ',
      time: '၁ ရက်အရင်', title: 'အသက် ၄၅ ရက်သား ကြက်များ ရောင်းမည်',
      content: 'ပျမ်းမျှ အလေးချိန် ၂.၂ ကီလိုဂရမ်ရှိသော ကြက်များ ရနိုင်ပါသည်။ နေရပ်တွင် ကိုယ်တိုင်လာ၍ ကြည့်ရှုနိုင်ပါသည်။',
      price: '၉,၀၀၀ ကျပ်/ကီလို'
    },
    {
      id: 4, type: 'request', name: 'ဒေါ်နှင်းနှင်းအေး', initials: 'နအ',
      time: '၂ ရက်အရင်', title: 'ကြက်ဆေးဝါးများ အစုလိုက် ဝယ်လိုပါသည်',
      content: 'ကြက် ၃၀၀ ကောင်အတွက် လိုအပ်သည့် ဗီတာမင်နှင့် ရောဂါကာကွယ်ဆေးများ အကြံပြုပေးနိုင်သူနှင့် စကားပြောလိုပါသည်။',
      quantity: '၃၀၀'
    },
    {
      id: 5, type: 'sale', name: 'ကိုဇော်လင်း', initials: 'ကဇ',
      time: '၃ ရက်အရင်', title: 'ဥထုတ်ကြက်များ လက်ကားရောင်းချမည်',
      content: 'နေ့စဉ် ဥအထွက်နှုန်း ကောင်းမွန်သော ကြက်များအား လက်ကားစျေးနှုန်းဖြင့် ရောင်းချပေးပါသည်။',
      price: '၇,၂၀၀ ကျပ်/ကောင်'
    },
    {
      id: 6, type: 'request', name: 'မခင်ဇာဇာ', initials: 'ခဇ',
      time: '၄ ရက်အရင်', title: 'တိုက်ကြက်မွေးမြူရေး နည်းစနစ် မေးမြန်းလိုသည်',
      content: 'အိမ်တွင်း တိုက်ကန်စနစ်ဖြင့် ကြက်ကလေး ၅၀ စတင်မွေးမြူလိုပါသဖြင့် အကြံဉာဏ်ရယူလိုပါသည်။',
      quantity: '၅၀'
    }
  ];

  var deletedId = null;
  var currentFilter = 'all';
  var currentSearch = '';
  var deleteModalEl = document.getElementById('deleteModal');
  var deleteModal = deleteModalEl ? new bootstrap.Modal(deleteModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function typeBadge(type) {
    if (type === 'sale') {
      return '<span class="post-type-badge sale">' + (lang() === 'en' ? 'Sales Post' : 'ရောင်းရန်ပို့စ်') + '</span>';
    }
    return '<span class="post-type-badge request">' + (lang() === 'en' ? 'Chat / Buy Request' : 'ဝယ်လို/စကားပြောရန်') + '</span>';
  }

  function metaChip(post) {
    if (post.type === 'sale') {
      return '<span class="post-chip"><i class="bi bi-tag-fill"></i> ' + post.price + '</span>';
    }
    return '<span class="post-chip"><i class="bi bi-boxes"></i> ' + (lang() === 'en' ? 'Qty: ' : 'အရေအတွက်: ') + post.quantity + '</span>';
  }

  function render() {
    var feed = document.getElementById('postsFeed');
    var filtered = posts.filter(function (p) {
      var matchesFilter = currentFilter === 'all' || p.type === currentFilter;
      var haystack = (p.name + ' ' + p.title + ' ' + p.content).toLowerCase();
      var matchesSearch = haystack.indexOf(currentSearch.toLowerCase()) !== -1;
      return matchesFilter && matchesSearch;
    });

    document.getElementById('resultsCount').textContent =
      filtered.length + (lang() === 'en' ? ' result(s)' : ' ခု တွေ့ရှိသည်');

    if (filtered.length === 0) {
      feed.innerHTML =
        '<div class="empty-state">' +
        '<i class="bi bi-inbox"></i>' +
        '<h3>' + (lang() === 'en' ? 'No posts found' : 'ပို့စ်များ မတွေ့ရှိပါ') + '</h3>' +
        '<p>' + (lang() === 'en' ? 'Try a different filter or search term.' : 'အခြား ဇစ်ခွဲ သို့မဟုတ် ရှာဖွေမှုစကားလုံးဖြင့် ထပ်မံစမ်းကြည့်ပါ။') + '</p>' +
        '</div>';
      return;
    }

    feed.innerHTML = filtered.map(function (p) {
      return (
        '<article class="post-card" data-id="' + p.id + '">' +
        '<div class="post-card-head">' +
        '<div class="post-avatar">' + p.initials + '</div>' +
        '<div class="post-user"><div class="name">' + p.name + '</div><div class="time">' + p.time + '</div></div>' +
        typeBadge(p.type) +
        '</div>' +
        '<p class="post-title">' + p.title + '</p>' +
        '<p class="post-content">' + p.content + '</p>' +
        '<div class="post-meta">' + metaChip(p) + '</div>' +
        '<div class="post-card-footer">' +
        '<button class="btn btn-outline btn-view"><i class="bi bi-eye-fill"></i> ' + (lang() === 'en' ? 'View' : 'ကြည့်ရန်') + '</button>' +
        '<button class="btn btn-danger btn-delete" data-id="' + p.id + '"><i class="bi bi-trash-fill"></i> ' + (lang() === 'en' ? 'Delete Post' : 'အရေးယူ/ဖျက်သိမ်းရန်') + '</button>' +
        '</div>' +
        '</article>'
      );
    }).join('');
  }

  document.addEventListener('DOMContentLoaded', function () {
    render();

    document.getElementById('filterTabs').addEventListener('click', function (e) {
      var btn = e.target.closest('button[data-filter]');
      if (!btn) return;
      currentFilter = btn.getAttribute('data-filter');
      document.querySelectorAll('#filterTabs button').forEach(function (b) {
        b.classList.toggle('active', b === btn);
      });
      render();
    });

    document.getElementById('searchInput').addEventListener('input', function (e) {
      currentSearch = e.target.value;
      render();
    });

    document.getElementById('postsFeed').addEventListener('click', function (e) {
      var delBtn = e.target.closest('.btn-delete');
      if (delBtn && deleteModal) {
        deletedId = Number(delBtn.getAttribute('data-id'));
        deleteModal.show();
      }
    });

    document.getElementById('confirmDelete').addEventListener('click', function () {
      posts = posts.filter(function (p) { return p.id !== deletedId; });
      deleteModal.hide();
      render();
      window.AdminUI.toast('ပို့စ်ကို ဖျက်သိမ်းပြီးပါပြီ', 'Post deleted successfully', 'bi-trash-fill');
    });

    document.addEventListener('adminlangchange', function (e) {
      var searchInput = document.getElementById('searchInput');
      searchInput.placeholder = e.detail.lang === 'en'
        ? searchInput.getAttribute('data-en-ph')
        : searchInput.getAttribute('data-my-ph');
      render();
    });
  });
})();
