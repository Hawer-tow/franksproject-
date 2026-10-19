const themeBtn = document.getElementById('themeBtn');
const body = document.body;

themeBtn.addEventListener('click', () => {
  const isLight = body.classList.contains('light');

  // Flip body class
  body.classList.toggle('light', !isLight);
  body.classList.toggle('dark', isLight);

  // Update button text
  themeBtn.textContent = isLight ? 'Switch to Light Mode' : 'Switch to Dark Mode';

  // Swap images based on data attributes
  document.querySelectorAll('img[data-light][data-dark]').forEach(img => {
    img.src = isLight ? img.dataset.dark : img.dataset.light;
  });

  // Save choice
  localStorage.setItem('theme', body.className);
});

// Load saved theme on refresh
const savedTheme = localStorage.getItem('theme');
if (savedTheme) {
  body.className = savedTheme;
  themeBtn.textContent = savedTheme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode';
  document.querySelectorAll('img[data-light][data-dark]').forEach(img => {
    img.src = savedTheme === 'dark' ? img.dataset.dark : img.dataset.light;
  });
}


// Back to Top button
const backToTopBtn = document.getElementById('backToTop');

// Show button when user scrolls down
window.addEventListener('scroll', () => {
  if (document.body.scrollTop > 200 || document.documentElement.scrollTop > 200) {
    backToTopBtn.style.display = "block";
  } else {
    backToTopBtn.style.display = "none";
  }
});

// Scroll smoothly to top when clicked
backToTopBtn.addEventListener('click', () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
});

