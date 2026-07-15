/* ==========================================================================
   Broiler Farming System — Dashboard Home
   Personalizes the welcome banner using the logged-in user's profile.
   ========================================================================== */

(function () {
  function lang() {
    return document.documentElement.getAttribute('data-lang') || 'my';
  }

  function setWelcome(name) {
    var el = document.getElementById('dashboardWelcomeName');
    if (!el) return;
    el.textContent = (lang() === 'en' ? 'Welcome, ' : 'ကြိုဆိုပါသည်, ') + name + '!';
  }

  document.addEventListener('DOMContentLoaded', function () {
    var session = window.GuestUI && window.GuestUI.getSession();
    if (!session) return;

    window.GuestUI.authFetch('/api/v1/users/me')
      .then(function (response) {
        if (!response.ok) throw new Error('Failed to load profile');
        return response.json();
      })
      .then(function (profile) {
        setWelcome(profile.fullName || profile.username);
      })
      .catch(function () {
        if (session.user) setWelcome(session.user.username);
      });

    document.addEventListener('dashboardlangchange', function () {
      var session2 = window.GuestUI.getSession();
      if (session2 && session2.user) setWelcome(session2.user.username);
    });
  });
})();
