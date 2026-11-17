/* FrankMpesa customer site: shared behaviour (rates, theme, back-to-top, country highlight). */

const COUNTRIES = [
  {
    id: 'kenyasection', name: 'Kenya', img: 'img/kenya.jpg', currency: 'KES',
    directions: [
      { title: 'Kenya → Uganda', url: '/api/rates/kenya2ugxrates', date: 'kenya2Ugxratedate', rate: 'kenya2Ugxrate' },
      { title: 'Uganda → Kenya', url: '/api/rates/ugx2kenyarates', date: 'Ugx2kenyaratedate', rate: 'Ugx2kenyarate' }
    ]
  },
  {
    id: 'tanzaniasection', name: 'Tanzania', img: 'img/tanzania.jpg', currency: 'TZS',
    directions: [
      { title: 'Uganda → Tanzania', url: '/api/rates/ugx2tanzaniarates', date: 'Ugx2tanzaniaratedate', rate: 'Ugx2tanzaniarate' },
      { title: 'Tanzania → Uganda', url: '/api/rates/tanzania2ugxrates', date: 'tanzania2Ugxratedate', rate: 'tanzania2Ugxrate' }
    ]
  },
  {
    id: 'congosection', name: 'Congo', img: 'img/congo.jpg', currency: '',
    directions: [
      { title: 'Uganda → Congo', url: '/api/rates/ugx2congorates', date: 'Ugx2congoratedate', rate: 'Ugx2congorate' },
      { title: 'Congo → Uganda', url: '/api/rates/congo2ugxrates', date: 'congo2Ugxratedate', rate: 'congo2Ugxrate' }
    ]
  },
  {
    id: 'rwandasection', name: 'Rwanda', img: 'img/rwanda.jpg', currency: 'RWF',
    directions: [
      { title: 'Uganda → Rwanda', url: '/api/rates/ugx2rwandarates', date: 'Ugx2rwandaratedate', rate: 'Ugx2rwandarate' },
      { title: 'Rwanda → Uganda', url: '/api/rates/rwanda2ugxrates', date: 'rwanda2Ugxratedate', rate: 'rwanda2Ugxrate' }
    ]
  }
];

function formatDate(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value || '—';
  return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

function isUnset(value) {
  return value === null || value === undefined || value === '' || /not set/i.test(String(value));
}

function formatNumber(value) {
  if (isUnset(value)) return null;
  const n = Number(value);
  if (!Number.isFinite(n)) return String(value);
  // small rates (e.g. 0.00027) need more decimals than large ones
  return n.toLocaleString('en-US', { maximumFractionDigits: Math.abs(n) < 1 ? 6 : 4 });
}

function el(tag, attrs = {}, text) {
  const node = document.createElement(tag);
  Object.entries(attrs).forEach(([k, v]) => node.setAttribute(k, v));
  if (text !== undefined) node.textContent = text;
  return node;
}

/** Builds the country cards (empty tables) into #ratesGrid and the jump chips into #countryChips. */
function buildRateCards() {
  const grid = document.getElementById('ratesGrid');
  const chips = document.getElementById('countryChips');
  if (!grid) return;

  COUNTRIES.forEach(c => {
    if (chips) {
      const chip = el('a', { href: `#${c.id}` });
      chip.append(el('img', { src: c.img, alt: '' }), document.createTextNode(c.name));
      chips.append(chip);
    }

    const card = el('article', { class: 'country-card', id: c.id });
    const head = el('div', { class: 'country-head' });
    const titles = el('div');
    titles.append(el('h3', {}, c.name), el('p', {}, 'Today\'s rates in both directions'));
    head.append(el('img', { src: c.img, alt: `${c.name} flag` }), titles);
    card.append(head);

    c.directions.forEach(d => {
      const wrap = el('div', { class: 'table-container' });
      const table = el('table');
      d.tbody = el('tbody');
      const thead = el('thead');
      const tr = el('tr');
      ['Date', 'Rate', 'Min amount', 'Max amount'].forEach(h => tr.append(el('th', { scope: 'col' }, h)));
      thead.append(tr);
      table.append(el('caption', {}, d.title), thead, d.tbody);
      d.tbody.append(placeholderRow('Loading…'));
      wrap.append(table);
      card.append(wrap);
    });
    grid.append(card);
  });
}

function placeholderRow(text) {
  const tr = el('tr');
  tr.append(el('td', { colspan: '4', class: 'empty', 'data-label': '' }, text));
  return tr;
}

/** Loads every direction's rates; returns the first available Kenya → Uganda row for the hero card. */
async function loadRates() {
  let featured = null;
  await Promise.all(COUNTRIES.flatMap(c => c.directions.map(async d => {
    try {
      const res = await fetch(d.url, { headers: { Accept: 'application/json' } });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const rows = await res.json();
      d.tbody.replaceChildren();
      if (!Array.isArray(rows) || rows.length === 0) {
        d.tbody.append(placeholderRow('No rate published today'));
        return;
      }
      rows.forEach(row => {
        const tr = el('tr');
        const rate = formatNumber(row[d.rate]);
        const min = formatNumber(row.min_transactionvalue);
        const max = formatNumber(row.max_transactionValue);
        tr.append(
          el('td', { 'data-label': 'Date' }, formatDate(row[d.date])),
          el('td', { 'data-label': 'Rate', class: rate ? '' : 'empty' }, rate || 'Not set yet'),
          el('td', { 'data-label': 'Min amount', class: min ? '' : 'empty' }, min || '—'),
          el('td', { 'data-label': 'Max amount', class: max ? '' : 'empty' }, max || '—')
        );
        d.tbody.append(tr);
      });
      if (d.url.endsWith('/kenya2ugxrates') && !isUnset(rows[0][d.rate])) {
        featured = { rate: formatNumber(rows[0][d.rate]), date: formatDate(rows[0][d.date]), min: formatNumber(rows[0].min_transactionvalue) };
      }
    } catch (err) {
      console.error(`Could not load ${d.title}:`, err);
      d.tbody.replaceChildren(placeholderRow('Rates are unavailable right now'));
    }
  })));
  return featured;
}

/** Light/dark switch stored in localStorage('theme'). Pages with a personal theme handle it themselves. */
function setUpThemeToggle(button, onChange) {
  if (!button) return;
  const body = document.body;
  const saved = localStorage.getItem('theme');
  if (saved === 'dark' || saved === 'light') {
    body.classList.remove('light', 'dark');
    body.classList.add(saved);
  }
  const label = () => button.setAttribute('aria-label', body.classList.contains('dark') ? 'Switch to light mode' : 'Switch to dark mode');
  label();
  button.addEventListener('click', () => {
    const toDark = !body.classList.contains('dark');
    body.classList.remove('light', 'dark', 'personalized');
    body.classList.add(toDark ? 'dark' : 'light');
    localStorage.setItem('theme', toDark ? 'dark' : 'light');
    label();
    if (onChange) onChange(toDark ? 'dark' : 'light');
  });
}

function setUpBackToTop() {
  const btn = document.getElementById('backToTop');
  if (!btn) return;
  window.addEventListener('scroll', () => {
    btn.style.display = window.scrollY > 400 ? 'block' : 'none';
  }, { passive: true });
  btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
}

/** Briefly outlines a country card when you jump to it from a chip or link. */
function setUpCountryHighlight() {
  document.addEventListener('click', e => {
    const link = e.target.closest('a[href^="#"]');
    if (!link) return;
    const target = document.getElementById(link.getAttribute('href').slice(1));
    if (!target || !target.classList.contains('country-card')) return;
    target.classList.remove('highlight');
    void target.offsetWidth;
    target.classList.add('highlight');
    setTimeout(() => target.classList.remove('highlight'), 2000);
  });
}
