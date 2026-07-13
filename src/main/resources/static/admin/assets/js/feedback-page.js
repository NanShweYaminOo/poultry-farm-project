(function () {
  var tickets = [];
  var currentFilter = 'all';
  var currentSearch = '';
  var editingTicketId = null;
  var updateStatusModalEl = document.getElementById('updateStatusModal');
  var updateStatusModal = updateStatusModalEl ? new bootstrap.Modal(updateStatusModalEl) : null;

  var STATUS_LABELS = {
    PENDING: { my: 'ဆိုင်းငံ့ထား', en: 'Pending' },
    IN_PROGRESS: { my: 'လုပ်ဆောင်နေဆဲ', en: 'In Progress' },
    RESOLVED: { my: 'ဖြေရှင်းပြီး', en: 'Resolved' },
    CLOSED: { my: 'ပိတ်ပြီး', en: 'Closed' }
  };

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text == null ? '' : String(text);
    return div.innerHTML;
  }

  function formatDate(iso) {
    if (!iso) return '';
    var d = new Date(iso);
    return isNaN(d.getTime()) ? '' : d.toLocaleDateString();
  }

  function statusBadge(status) {
    var label = STATUS_LABELS[status] || { my: status, en: status };
    return '<span class="badge">' + escapeHtml(lang() === 'en' ? label.en : label.my) + '</span>';
  }

  function matches(ticket) {
    if (currentFilter !== 'all' && ticket.status !== currentFilter) return false;
    if (currentSearch && ticket.content.toLowerCase().indexOf(currentSearch) === -1) return false;
    return true;
  }

  function submitterCell(ticket) {
    var name = ticket.submittedByFullName || '';
    var initials = name ? name.trim().slice(0, 2).toUpperCase() : '?';
    var avatar = ticket.submittedByProfileImageUrl ?
      '<img src="' + escapeHtml(ticket.submittedByProfileImageUrl) + '" style="width:100%;height:100%;object-fit:cover;">' :
      escapeHtml(initials);
    return '<div class="d-flex align-items-center gap-2">' +
      '<div class="post-avatar" style="width:34px;height:34px;font-size:12px;">' + avatar + '</div>' +
      '<span style="font-weight:600;">' + escapeHtml(name) + '</span></div>';
  }

  function render() {
    var tbody = document.getElementById('feedbackTableBody');
    var visible = tickets.filter(matches);

    tbody.innerHTML = visible.map(function (ticket) {
      var content = ticket.content.length > 120 ? ticket.content.slice(0, 120) + '...' : ticket.content;
      return '<tr>' +
        '<td>' + submitterCell(ticket) + '</td>' +
        '<td>' + escapeHtml(content) + '</td>' +
        '<td>' + statusBadge(ticket.status) + '</td>' +
        '<td>' + formatDate(ticket.createdDate) + '</td>' +
        '<td>' + (formatDate(ticket.resolvedDate) || '-') + '</td>' +
        '<td class="text-end">' +
        '<button class="btn btn-outline btn-sm btn-update-status" data-id="' + ticket.id + '">' +
        '<i class="bi bi-arrow-repeat"></i> ' + (lang() === 'en' ? 'Update Status' : 'အခြေအနေ ပြောင်းရန်') +
        '</button></td>' +
      '</tr>';
    }).join('');

    var countEl = document.getElementById('feedbackResultsCount');
    if (countEl) countEl.textContent = visible.length + ' / ' + tickets.length;
  }

  function loadTickets() {
    window.AdminUI.authFetch('/api/v1/admin/feedback-tickets')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load feedback tickets');
        return response.json();
      })
      .then(function (data) {
        tickets = data;
        render();
      })
      .catch(function () {
        window.AdminUI.toast('အကြံပြုချက်များ ရယူ၍မရပါ။', 'Failed to load feedback tickets.', 'bi-exclamation-triangle-fill');
      });
  }

  function openUpdateStatusModal(ticketId) {
    var ticket = tickets.find(function (t) { return t.id === ticketId; });
    if (!ticket || !updateStatusModal) return;
    editingTicketId = ticketId;
    document.getElementById('updateStatusSelect').value = ticket.status;
    updateStatusModal.show();
  }

  function saveStatus() {
    if (editingTicketId == null) return;
    var newStatus = document.getElementById('updateStatusSelect').value;
    var saveBtn = document.getElementById('saveStatusBtn');
    saveBtn.disabled = true;

    window.AdminUI.authFetch('/api/v1/admin/feedback-tickets/' + editingTicketId + '/status', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (body) {
          return { ok: response.ok, body: body };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not update status.' : 'အခြေအနေ ပြောင်းလဲ၍ မရပါ။');
          window.AdminUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        updateStatusModal.hide();
        loadTickets();
        window.AdminUI.toast('အခြေအနေ ပြောင်းလဲပြီးပါပြီ။', 'Ticket status updated.', 'bi-check-circle-fill');
      })
      .catch(function () {
        window.AdminUI.toast('အခြေအနေ ပြောင်းလဲ၍ မရပါ။', 'Could not update status.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var tabs = document.getElementById('feedbackFilterTabs');
    if (tabs) {
      tabs.querySelectorAll('button').forEach(function (btn) {
        btn.addEventListener('click', function () {
          tabs.querySelectorAll('button').forEach(function (b) { b.classList.remove('active'); });
          btn.classList.add('active');
          currentFilter = btn.getAttribute('data-filter');
          render();
        });
      });
    }

    var searchInput = document.getElementById('feedbackSearchInput');
    if (searchInput) {
      searchInput.addEventListener('input', function () {
        currentSearch = searchInput.value.trim().toLowerCase();
        render();
      });
    }

    document.addEventListener('adminlangchange', render);

    var tableBody = document.getElementById('feedbackTableBody');
    if (tableBody) {
      tableBody.addEventListener('click', function (e) {
        var btn = e.target.closest('.btn-update-status');
        if (btn) openUpdateStatusModal(Number(btn.getAttribute('data-id')));
      });
    }

    var saveStatusBtn = document.getElementById('saveStatusBtn');
    if (saveStatusBtn) saveStatusBtn.addEventListener('click', saveStatus);

    loadTickets();
  });
})();
