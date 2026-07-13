/* ==========================================================================
   Broiler Farming System — Admin Frontend
   Breed Reference (breeds.jsp) — full CRUD against /api/v1/breeds,
   including photo upload, via AdminUI.authFetch.
   ========================================================================== */

(function () {
  var breeds = [];
  var editingId = null;
  var deletingId = null;

  var breedModalEl = document.getElementById('breedModal');
  var breedModal = breedModalEl ? new bootstrap.Modal(breedModalEl) : null;
  var deleteModalEl = document.getElementById('deleteBreedModal');
  var deleteModal = deleteModalEl ? new bootstrap.Modal(deleteModalEl) : null;

  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function escapeHtml(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function thumb(breed) {
    if (breed.imageUrl) {
      return '<img src="' + breed.imageUrl + '" alt="" style="width:44px;height:44px;object-fit:cover;border-radius:8px;">';
    }
    return '<div style="width:44px;height:44px;border-radius:8px;background:var(--line);"></div>';
  }

  function render() {
    var tbody = document.getElementById('breedsTableBody');

    if (breeds.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="padding:30px;color:var(--muted);">' +
        (lang() === 'en' ? 'No breeds yet.' : 'မျိုးရင်း မရှိသေးပါ။') + '</td></tr>';
      return;
    }

    tbody.innerHTML = breeds.map(function (b) {
      return '<tr data-id="' + b.id + '">' +
        '<td>' + thumb(b) + '</td>' +
        '<td style="font-weight:700;color:var(--emerald-900);">' + escapeHtml(b.name) + '</td>' +
        '<td style="color:var(--muted);">' + escapeHtml(b.origin || '-') + '</td>' +
        '<td>' + (b.avgMarketWeightKg != null ? b.avgMarketWeightKg + ' kg' : '-') + '</td>' +
        '<td>' + (b.growthPeriodDays != null ? b.growthPeriodDays : '-') + '</td>' +
        '<td class="text-end">' +
        '<button class="btn btn-outline btn-sm btn-edit" data-id="' + b.id + '"><i class="bi bi-pencil-fill"></i></button> ' +
        '<button class="btn btn-danger btn-sm btn-delete" data-id="' + b.id + '"><i class="bi bi-trash-fill"></i></button>' +
        '</td>' +
        '</tr>';
    }).join('');
  }

  function loadBreeds() {
    window.AdminUI.authFetch('/api/v1/breeds')
      .then(function (response) { return response.json(); })
      .then(function (data) {
        breeds = data;
        render();
      })
      .catch(function () {
        window.AdminUI.toast('မျိုးရင်းစာရင်း တင်ရန် မအောင်မြင်ပါ။', 'Failed to load breeds.', 'bi-exclamation-triangle-fill');
      });
  }

  function resetForm() {
    editingId = null;
    document.getElementById('breedIdInput').value = '';
    document.getElementById('breedNameInput').value = '';
    document.getElementById('breedOriginInput').value = '';
    document.getElementById('breedWeightInput').value = '';
    document.getElementById('breedGrowthInput').value = '';
    document.getElementById('breedDescriptionInput').value = '';
    document.getElementById('breedImageInput').value = '';
    document.getElementById('breedImagePreviewWrap').style.display = 'none';
  }

  function openAddModal() {
    resetForm();
    document.getElementById('breedModalTitle').textContent =
      lang() === 'en' ? 'Add New Breed' : 'မျိုးရင်းအသစ်ထည့်ရန်';
    breedModal.show();
  }

  function openEditModal(breed) {
    resetForm();
    editingId = breed.id;
    document.getElementById('breedIdInput').value = breed.id;
    document.getElementById('breedNameInput').value = breed.name || '';
    document.getElementById('breedOriginInput').value = breed.origin || '';
    document.getElementById('breedWeightInput').value = breed.avgMarketWeightKg != null ? breed.avgMarketWeightKg : '';
    document.getElementById('breedGrowthInput').value = breed.growthPeriodDays != null ? breed.growthPeriodDays : '';
    document.getElementById('breedDescriptionInput').value = breed.description || '';
    if (breed.imageUrl) {
      document.getElementById('breedImagePreview').src = breed.imageUrl;
      document.getElementById('breedImagePreviewWrap').style.display = 'block';
    }
    document.getElementById('breedModalTitle').textContent =
      lang() === 'en' ? 'Edit Breed' : 'မျိုးရင်း ပြင်ဆင်ရန်';
    breedModal.show();
  }

  function saveBreed() {
    var name = document.getElementById('breedNameInput').value.trim();
    if (!name) {
      window.AdminUI.toast('မျိုးရင်းအမည် ဖြည့်စွက်ပါ။', 'Breed name is required.', 'bi-exclamation-triangle-fill');
      return;
    }

    var formData = new FormData();
    formData.append('name', name);
    formData.append('origin', document.getElementById('breedOriginInput').value.trim());
    var weight = document.getElementById('breedWeightInput').value.trim();
    if (weight) formData.append('avgMarketWeightKg', weight);
    var growth = document.getElementById('breedGrowthInput').value.trim();
    if (growth) formData.append('growthPeriodDays', growth);
    formData.append('description', document.getElementById('breedDescriptionInput').value.trim());
    var imageFile = document.getElementById('breedImageInput').files[0];
    if (imageFile) formData.append('image', imageFile);

    var url = editingId ? '/api/v1/breeds/' + editingId : '/api/v1/breeds';
    var method = editingId ? 'PUT' : 'POST';
    var saveBtn = document.getElementById('saveBreedBtn');
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
            (lang() === 'en' ? 'Could not save breed.' : 'မျိုးရင်း သိမ်းဆည်း၍ မရပါ။');
          window.AdminUI.toast(message, message, 'bi-exclamation-triangle-fill');
          return;
        }
        breedModal.hide();
        loadBreeds();
        window.AdminUI.toast('မျိုးရင်း သိမ်းဆည်းပြီးပါပြီ။', 'Breed saved.');
      })
      .catch(function () {
        window.AdminUI.toast('မျိုးရင်း သိမ်းဆည်း၍ မရပါ။', 'Could not save breed.', 'bi-exclamation-triangle-fill');
      })
      .finally(function () {
        saveBtn.disabled = false;
      });
  }

  function confirmDelete() {
    if (deletingId == null) return;
    window.AdminUI.authFetch('/api/v1/breeds/' + deletingId, { method: 'DELETE' })
      .then(function (response) {
        if (!response.ok) throw new Error('delete failed');
        deleteModal.hide();
        loadBreeds();
        window.AdminUI.toast('မျိုးရင်းကို ဖျက်သိမ်းပြီးပါပြီ။', 'Breed deleted.', 'bi-trash-fill');
      })
      .catch(function () {
        window.AdminUI.toast('မျိုးရင်း ဖျက်သိမ်း၍ မရပါ။', 'Could not delete breed.', 'bi-exclamation-triangle-fill');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    loadBreeds();

    document.getElementById('addBreedBtn').addEventListener('click', openAddModal);
    document.getElementById('saveBreedBtn').addEventListener('click', saveBreed);
    document.getElementById('confirmDeleteBreed').addEventListener('click', confirmDelete);

    document.getElementById('breedsTableBody').addEventListener('click', function (e) {
      var editBtn = e.target.closest('.btn-edit');
      if (editBtn) {
        var breed = breeds.find(function (b) { return b.id === Number(editBtn.getAttribute('data-id')); });
        if (breed) openEditModal(breed);
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
