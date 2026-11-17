/* FrankMpesa Help Center: theme, search, topic highlighting and back-to-top. */

(() => {
  const body = document.body;

  // Theme (remembered)
  const themeBtn = document.getElementById('themeBtn');
  const saved = localStorage.getItem('theme');
  if (saved === 'dark' || saved === 'light') {
    body.classList.remove('light', 'dark');
    body.classList.add(saved);
  }
  const label = () => themeBtn.setAttribute('aria-label', body.classList.contains('dark') ? 'Switch to light mode' : 'Switch to dark mode');
  label();
  themeBtn.addEventListener('click', () => {
    const toDark = !body.classList.contains('dark');
    body.classList.toggle('dark', toDark);
    body.classList.toggle('light', !toDark);
    localStorage.setItem('theme', toDark ? 'dark' : 'light');
    label();
  });

  // Search: hide topics (and FAQ entries) that do not mention every typed word
  const search = document.getElementById('search');
  const topics = [...document.querySelectorAll('[data-topic]')];
  const faqs = [...document.querySelectorAll('#faq details')];
  const noResults = document.getElementById('noResults');
  search.addEventListener('input', () => {
    const words = search.value.toLowerCase().split(/\s+/).filter(Boolean);
    const matches = el => words.every(w => el.textContent.toLowerCase().includes(w));
    let shown = 0;
    topics.forEach(t => {
      const show = words.length === 0 || matches(t);
      t.hidden = !show;
      if (show && t.classList.contains('topic')) shown++;
    });
    faqs.forEach(d => {
      const show = words.length === 0 || matches(d);
      d.hidden = !show;
      if (words.length && show) d.open = true;
    });
    noResults.hidden = words.length === 0 || shown > 0;
  });

  // Highlight the topic currently on screen in the side list
  const links = new Map([...document.querySelectorAll('.toc a')].map(a => [a.getAttribute('href').slice(1), a]));
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (!entry.isIntersecting) return;
      links.forEach(a => a.classList.remove('active'));
      const link = links.get(entry.target.id);
      if (link) link.classList.add('active');
    });
  }, { rootMargin: '-40% 0px -55% 0px' });
  document.querySelectorAll('.topic[id]').forEach(s => observer.observe(s));

  // Back to top
  const backToTop = document.getElementById('backToTop');
  window.addEventListener('scroll', () => {
    backToTop.style.display = window.scrollY > 500 ? 'block' : 'none';
  }, { passive: true });
  backToTop.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
})();
