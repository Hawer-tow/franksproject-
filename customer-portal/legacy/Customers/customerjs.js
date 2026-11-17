/* FrankMpesa home page: rates, theme and customer registration. Shared helpers live in common.js. */

document.addEventListener('DOMContentLoaded', async () => {
  buildRateCards();
  setUpThemeToggle(document.getElementById('themeBtn'));
  setUpBackToTop();
  setUpCountryHighlight();
  setUpRegistration();

  const featured = await loadRates();
  if (featured) {
    document.getElementById('featuredRate').textContent = featured.rate;
    document.getElementById('featuredDate').textContent = featured.date;
    document.getElementById('featuredMin').textContent = featured.min || '—';
  } else {
    document.getElementById('featuredRate').textContent = 'Not set yet';
  }
});

function setUpRegistration() {
  const modal = document.getElementById('registrationModal');
  const form = document.getElementById('registrationForm');
  const message = document.getElementById('registerMsg');
  const formBody = modal.querySelector('.form-body');
  const footer = modal.querySelector('.form-footer');

  const open = () => {
    modal.style.display = 'block';
    document.body.classList.add('modal-open');
    setTimeout(() => document.getElementById('customer_name')?.focus(), 50);
  };
  const close = () => {
    modal.style.display = 'none';
    document.body.classList.remove('modal-open');
  };

  document.getElementById('openFormBtn').addEventListener('click', open);
  document.querySelectorAll('[data-open-register]').forEach(b => b.addEventListener('click', open));
  document.getElementById('closeFormBtn').addEventListener('click', close);
  document.getElementById('closeFormBtn').addEventListener('keydown', e => { if (e.key === 'Enter') close(); });
  modal.addEventListener('click', e => { if (e.target === modal) close(); });
  document.addEventListener('keydown', e => { if (e.key === 'Escape' && modal.style.display === 'block') close(); });
  if (location.hash === '#register') open();

  form.addEventListener('submit', async e => {
    e.preventDefault();
    message.className = 'alert alert-error';
    message.textContent = '';

    const name = document.getElementById('customer_name').value.trim();
    const phone = document.getElementById('phone_No').value.trim();
    if (!name || !phone) {
      message.textContent = 'Please enter your full name and phone number.';
      return;
    }

    const submit = footer.querySelector('button[type="submit"]');
    submit.disabled = true;
    submit.textContent = 'Creating your account…';

    try {
      const res = await fetch('/api/customers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          customer_name: name,
          customer_Phone_No: phone,
          customer_email: document.getElementById('customer_email').value.trim(),
          customer_primary_country: document.getElementById('primary_country').value,
          customer_secondary_country: document.getElementById('secondary_country').value
        })
      });
      const data = await res.json().catch(() => ({}));
      if (!res.ok || data.error) {
        message.textContent = data.error || 'Registration failed. Please try again.';
        return;
      }
      showSuccess(data.customer_passwordID);
    } catch (err) {
      console.error('Registration error:', err);
      message.textContent = 'We could not reach the server. Check your connection and try again.';
    } finally {
      submit.disabled = false;
      submit.textContent = 'Create my account';
    }
  });

  function showSuccess(passwordId) {
    formBody.replaceChildren();
    const done = el('div', { class: 'alert alert-success' });
    done.append(el('strong', {}, 'You are registered. '), document.createTextNode('Keep your password ID safe: you need it to log in.'));
    const idBox = el('div', { class: 'field' });
    const idInput = el('input', { type: 'text', readonly: '', value: passwordId || '' });
    idBox.append(el('label', {}, 'Your password ID'), idInput);
    formBody.append(done, idBox);

    footer.replaceChildren();
    const login = el('a', { class: 'btn btn-primary btn-block', href: '/loginhtmlformhtml.html' }, 'Go to login');
    const again = el('button', { class: 'btn btn-ghost', type: 'button' }, 'Register someone else');
    again.addEventListener('click', () => {
      // fresh, empty form
      location.hash = 'register';
      location.reload();
    });
    footer.append(login, again);
    idInput.select();
  }
}
