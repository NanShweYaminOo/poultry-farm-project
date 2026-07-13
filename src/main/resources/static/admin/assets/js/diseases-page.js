/* ==========================================================================
   Broiler Farming System — Admin Frontend
   Disease Reference (diseases.jsp) — full CRUD against /api/v1/diseases
   via AdminUI.authFetch.
   ========================================================================== */

(function () {
  var diseases = [];
  var editingId = null;
  var deletingId = null;

  var diseaseModalEl = document.getElementById('diseaseModal');
  var diseaseModal = diseaseModalEl ? new bootstrap.Modal(diseaseModalEl) : null;
  var deleteModalEl = document.getElementById('deleteDiseaseModal');
  var deleteModal = deleteModalEl ? new bootstrap.Modal(deleteModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function thumb(disease) {
    if (disease.imageUrl) {
      return '<img src="' + disease.imageUrl + '" alt="" style="width:44px;height:44px;object-fit:cover;border-radius:8px;">';
    }
    return '<div style="width:44px;height:44px;border-radius:8px;background:var(--line);"></div>';
  }

  function render() {
    var tbody = document.getElementById('diseasesTableBody');

    if (diseases.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No diseases yet.' : 'ရောဂါ မရှိသေးပါ။') + '</td></tr>';
      return;
    }

    tbody.innerHTML = diseases.map(function (d) {
      return '<tr data-id="' + d.id + '">' +
        '<td>' + thumb(d) + '</td>' +
        '<td style="font-weight:700;color:var(--emerald-900);">' + escapeHtml(d.name) + '</td>' +
        '<td style="color:var(--muted);">' + escapeHtml(d.keySymptoms || '-') + '</td>' +
        '<td style="color:var(--muted);">' + escapeHtml(d.notes || '-') + '</td>' +
        '<td class="text-end">' +
        '<button class="btn btn-outline btn-sm btn-edit" data-id="' + d.id + '"><i class="bi bi-pencil-fill"></i></button> ' +
        '<button class="btn btn-danger btn-sm btn-delete" data-id="' + d.id + '"><i class="bi bi-trash-fill"></i></button>' +
        '</td>' +
        '</tr>';
    }).join('');
  }

  function loadDiseases() {
    window.AdminUI.authFetch('/api/v1/diseases')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        diseases = data;
        render();
      })
      .catch(function () {
        window.AdminUI.toast('ရောဂါစာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load diseases.', 'bi-exclamation-triangle-fill');
      });
  }

  function resetForm() {
    editingId = null;
    document.getElementById('diseaseIdInput').value = '';
    document.getElementById('diseaseNameInput').value = '';
    document.getElementById('diseaseSymptomsInput').value = '';
    document.getElementById('diseaseNotesInput').value = '';
    document.getElementById('diseaseImageInput').value = '';
    document.getElementById('diseaseImagePreviewWrap').style.display = 'none';
  }

  function openAddModal() {
    resetForm();
    document.getElementById('diseaseModalTitle').textContent =
      lang() === 'en' ? 'Add New Disease' : 'ရောဂါအသစ်ထည့်ရန်';
    diseaseModal.show();
  }

  function openEditModal(disease) {
    resetForm();
    editingId = disease.id;
    document.getElementById('diseaseIdInput').value = disease.id;
    document.getElementById('diseaseNameInput').value = disease.name || '';
    document.getElementById('diseaseSymptomsInput').value = disease.keySymptoms || '';
    document.getElementById('diseaseNotesInput').value = disease.notes || '';
    if (disease.imageUrl) {
      document.getElementById('diseaseImagePreview').src = disease.imageUrl;
      document.getElementById('diseaseImagePreviewWrap').style.display = 'block';
    }
    document.getElementById('diseaseModalTitle').textContent =
      lang() === 'en' ? 'Edit Disease' : 'ရောဂါ ပြင်ဆင်ရန်';
    diseaseModal.show();
  }

  function saveDisease() {
    var name = document.getElementById('diseaseNameInput').value.trim();
    if (!name) {
      window.AdminUI.toast('ရောဂါအမည် ဖြည့်စွက်ပါ။', 'Disease name is required.', 'bi-exclamation-triangle-fill');
      return;
    }

    var formData = new FormData();
    formData.append('name', name);
    formData.append('keySymptoms', document.getElementById('diseaseSymptomsInput').value.trim());
    formData.append('notes', document.getElementById('diseaseNotesInput').value.trim());
    var imageFile = document.getElementById('diseaseImageInput').files[0];
    if (imageFile) formData.append('image', imageFile);

    var url = editingId ? '/api/v1/diseases/' + editingId : '/api/v1/diseases';
    var method = editingId ? 'PUT' : 'POST';
    var saveBtn = document.getElementById('saveDiseaseBtn');
    saveBtn.disabled = true;

    window.AdminUI.authFetch(url, { method: method, body: formData })
      .then(function (response) {
        return response.json().catch(function () { return {}; }).then(function (body) {
          return { ok: response.ok, body: body };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var message = (result.body && result.body.message) ||
            (lang() === 'en' ? 'Could not save disease.' : 'ရောဂါ သိမ်းဆည်း၍ မရပါ။');
          window.AdminUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        diseaseModal.hide();
        loadDiseases();
        window.AdminUI.toast('ရောဂါ သိမ်းဆည်းပြီးပါပြီ။', 'Disease saved.');
      })
      .catch(function () {
        window.AdminUI.toast('ရောဂါ သိမ်းဆည်း၍ မရပါ။', 'Could not save disease.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function confirmDelete() {
    if (deletingId == null) return;
    window.AdminUI.authFetch('/api/v1/diseases/' + deletingId, { method: 'DELETE' })
      .then(function (response) {
        if (!response.ok) throw new Error('delete failed');
        deleteModal.hide();
        loadDiseases();
        window.AdminUI.toast('ရောဂါကို ဖျက်သိမ်းပြီးပါပြီ။', 'Disease deleted.', 'bi-trash-fill');
      })
      .catch(function () {
        window.AdminUI.toast('ရောဂါ ဖျက်သိမ်း၍ မရပါ။', 'Could not delete disease.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadDiseases();

    document.getElementById('addDiseaseBtn').addEventListener('click', openAddModal);
    document.getElementById('saveDiseaseBtn').addEventListener('click', saveDisease);
    document.getElementById('confirmDeleteDisease').addEventListener('click', confirmDelete);

    document.getElementById('diseasesTableBody').addEventListener('click', function (e) {
      var editBtn = e.target.closest('.btn-edit');
      if (editBtn) {
        var disease = diseases.find(function (d) { return d.id === Number(editBtn.getAttribute('data-id')); });
        if (disease) openEditModal(disease);
        return;
      }
      var delBtn = e.target.closest('.btn-delete');
      if (delBtn && deleteModal) {
        deletingId = Number(delBtn.getAttribute('data-id'));
        deleteModal.show();
      }
    });

    document.addEventListener('adminlangchange', render);
  });
})();
