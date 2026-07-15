/* ==========================================================================
   Broiler Farming System — User Dashboard
   Feedback (feedback.jsp) — submit a ticket and list your own past tickets
   against /api/v1/feedback-tickets.
   ========================================================================== */

(function () {
  var tickets = [];

  var STATUS_LABELS = {
    PENDING: { my: 'ဆိုင်းငံ့ထား', en: 'Pending' },
    IN_PROGRESS: { my: 'လုပ်ဆောင်နေဆဲ', en: 'In Progress' },
    RESOLVED: { my: 'ဖြေရှင်းပြီး', en: 'Resolved' },
    CLOSED: { my: 'ပိတ်ပြီး', en: 'Closed' }
  };

  var STATUS_BADGE_CLASS = {
    PENDING: 'badge-gold',
    IN_PROGRESS: 'badge-gold',
    RESOLVED: 'badge-emerald',
    CLOSED: 'badge-muted'
  };

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function formatDate(iso) {
    if (!iso) return '';
    var d = new Date(iso);
    return isNaN(d.getTime()) ? '' : d.toLocaleDateString();
  }

  function statusBadge(status) {
    var label = STATUS_LABELS[status] || { my: status, en: status };
    var cls = STATUS_BADGE_CLASS[status] || 'badge-muted';
    return '<span class="badge ' + cls + '">' + escapeHtml(lang() === 'en' ? label.en : label.my) + '</span>';
  }

  function render() {
    var container = document.getElementById('myFeedbackTickets');

    if (tickets.length === 0) {
      container.innerHTML = '<div class="card empty-state">' +
        '<i class="bi bi-chat-square-text"></i>' +
        '<p>' + (lang() === 'en' ? 'You have not submitted any feedback yet.' : 'သင် မည်သည့်အကြံပြုချက်မျှ မတင်ပြရသေးပါ။') + '</p>' +
        '</div>';
      return;
    }

    container.innerHTML = tickets.map(function (t) {
      return '<div class="card" style="padding:16px;margin-bottom:10px;">' +
        '<div class="d-flex justify-content-between align-items-start gap-2">' +
        '<p style="margin:0;color:#3d473f;font-size:13.5px;flex:1;">' + escapeHtml(t.content) + '</p>' +
        statusBadge(t.status) +
        '</div>' +
        '<div style="font-size:12px;color:var(--muted);margin-top:8px;">' +
        (lang() === 'en' ? 'Submitted: ' : 'တင်ပြသည့်ရက်: ') + formatDate(t.createdDate) +
        (t.resolvedDate ? (' &middot; ' + (lang() === 'en' ? 'Resolved: ' : 'ဖြေရှင်းသည့်ရက်: ') + formatDate(t.resolvedDate)) : '') +
        '</div>' +
        '</div>';
    }).join('');
  }

  function loadTickets() {
    window.DashboardUI.authFetch('/api/v1/feedback-tickets')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        tickets = data;
        render();
      })
      .catch(function () {
        window.DashboardUI.toast('တင်ပြချက်များ တင်ရန် မအောင်မြင်ပါ။', 'Failed to load your tickets.', 'bi-exclamation-triangle-fill');
      });
  }

  function submitFeedback() {
    var contentInput = document.getElementById('feedbackContentInput');
    var content = contentInput.value.trim();
    if (!content) {
      window.DashboardUI.toast('အကြောင်းအရာ ဖြည့်စွက်ပါ။', 'Content is required.', 'bi-exclamation-triangle-fill');
      return;
    }

    var submitBtn = document.getElementById('submitFeedbackBtn');
    submitBtn.disabled = true;

    window.DashboardUI.authFetch('/api/v1/feedback-tickets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content: content })
    })
      .then(function (response) {
        if (!response.ok) throw new Error('submit failed');
        contentInput.value = '';
        loadTickets();
        window.DashboardUI.toast('အကြံပြုချက် တင်ပြပြီးပါပြီ။', 'Feedback submitted.');
      })
      .catch(function () {
        window.DashboardUI.toast('အကြံပြုချက် တင်ပြ၍ မရပါ။', 'Could not submit feedback.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        submitBtn.disabled = false;
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadTickets();
    document.getElementById('submitFeedbackBtn').addEventListener('click', submitFeedback);
    document.addEventListener('dashboardlangchange', render);
  });
})();
