/* FrankMpesa customer area: profile, personal theme, rates and logout. Shared helpers live in common.js. */

document.addEventListener('DOMContentLoaded', async () => {
  buildRateCards();
  setUpBackToTop();
  setUpCountryHighlight();
  setUpProfileMenu();

  const customer = await loadCustomer();
  applySavedTheme(customer && customer.theme_color);
  setUpThemeToggle(document.getElementById('themeBtn'));
  loadRates();
});

async function loadCustomer() {
  try {
    const res = await fetch('/api/customers/currentCustomer', { headers: { Accept: 'application/json' } });
    if (res.status === 401) {
      window.location.replace('/loginhtmlformhtml.html');
      return null;
    }
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const customer = await res.json();

    const name = (customer.customer_name || '').trim();
    document.getElementById('customerName').textContent = name || 'Customer';
    document.getElementById('customerPhone').textContent = customer.customer_Phone_No || '';
    document.getElementById('customerInitials').textContent =
      name.split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0].toUpperCase()).join('') || 'CU';
    const hour = new Date().getHours();
    const part = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
    document.getElementById('greeting').textContent = name ? `${part}, ${name.split(/\s+/)[0]}` : part;
    return customer;
  } catch (err) {
    console.error('Failed to load customer info:', err);
    document.getElementById('customerName').textContent = 'Customer';
    return null;
  }
}

/* ------------------------------------------------------------------ themes */

function applyPersonalizedTheme(color) {
  const root = document.body;
  root.style.setProperty('--accent', color);
  root.style.setProperty('--on-accent', isDarkColor(color) ? '#FFFFFF' : '#17212B');
}

function isDarkColor(color) {
  const hex = String(color).replace('#', '');
  if (!/^[0-9a-f]{6}$/i.test(hex)) return true;
  const r = parseInt(hex.slice(0, 2), 16), g = parseInt(hex.slice(2, 4), 16), b = parseInt(hex.slice(4, 6), 16);
  return (0.299 * r + 0.587 * g + 0.114 * b) < 150;
}

function setTheme(choice, personalColor) {
  const body = document.body;
  body.classList.remove('light', 'dark', 'personalized');
  if (choice === 'personalized' && personalColor) {
    applyPersonalizedTheme(personalColor);
    body.classList.add('light', 'personalized');
  } else {
    body.classList.add(choice === 'dark' ? 'dark' : 'light');
  }
  localStorage.setItem('theme', choice);
}

function applySavedTheme(personalColor) {
  if (personalColor) {
    localStorage.setItem('themeColor', personalColor);
    document.getElementById('personalThemeOption').hidden = false;
  }
  const color = personalColor || localStorage.getItem('themeColor');
  const saved = localStorage.getItem('theme');
  setTheme(saved || (color ? 'personalized' : 'light'), color);

  document.querySelectorAll('[data-theme-choice]').forEach(btn => {
    btn.addEventListener('click', () => {
      setTheme(btn.dataset.themeChoice, localStorage.getItem('themeColor'));
      closeProfileMenu();
    });
  });
}

/* ------------------------------------------------------------------ profile menu */

function setUpProfileMenu() {
  const chip = document.getElementById('customerProfile');
  const menu = document.getElementById('profileMenu');
  const toggle = () => {
    const open = menu.classList.toggle('open');
    chip.setAttribute('aria-expanded', String(open));
  };
  chip.addEventListener('click', toggle);
  chip.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); toggle(); } });
  document.addEventListener('click', e => {
    if (!e.target.closest('.profile')) closeProfileMenu();
  });
  document.addEventListener('keydown', e => { if (e.key === 'Escape') closeProfileMenu(); });

  document.getElementById('logoutBtn').addEventListener('click', async () => {
    await fetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
    window.location.replace('/loginhtmlformhtml.html');
  });
}

function closeProfileMenu() {
  document.getElementById('profileMenu').classList.remove('open');
  document.getElementById('customerProfile').setAttribute('aria-expanded', 'false');
}

// Coming back with the browser's back button after logging out should not show cached data
window.addEventListener('pageshow', e => {
  if (e.persisted) window.location.reload();
});
