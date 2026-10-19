function formatDate(isoString) {
  const date = new Date(isoString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

async function loadRates() {
  try {
    const endpoints = [
      { url: '/api/kenya2ugxrates', table: '#kenya2ugxTable', date: 'kenya2Ugxratedate', rate: 'kenya2Ugxrate' },
      { url: '/api/ugx2kenyarates', table: '#ugx2kenyaTable', date: 'Ugx2kenyaratedate', rate: 'Ugx2kenyarate' },
      { url: '/api/ugx2tanzaniarates', table: '#ugx2tanzaniaTable', date: 'Ugx2tanzaniaratedate', rate: 'Ugx2tanzaniarate' },
      { url: '/api/tanzania2ugxrates', table: '#tanzania2ugxTable', date: 'tanzania2Ugxratedate', rate: 'tanzania2Ugxrate' },
      { url: '/api/ugx2congorates', table: '#ugx2congoTable', date: 'Ugx2congoratedate', rate: 'Ugx2congorate' },
      { url: '/api/congo2ugxrates', table: '#congo2ugxTable', date: 'congo2Ugxratedate', rate: 'congo2Ugxrate' },
      { url: '/api/ugx2rwandarates', table: '#ugx2rwandaTable', date: 'Ugx2rwandaratedate', rate: 'Ugx2rwandarate' },
      { url: '/api/rwanda2ugxrates', table: '#rwanda2ugxTable', date: 'rwanda2Ugxratedate', rate: 'rwanda2Ugxrate' }
    ];

    for (const ep of endpoints) {
      const res = await fetch(ep.url);
      const data = await res.json();
      const tbody = document.querySelector(`${ep.table} tbody`);
      tbody.innerHTML = '';
      data.forEach(row => {
        tbody.innerHTML += `
          <tr>
            <td data-label="Date">${formatDate(row[ep.date])}</td>
            <td data-label="Rate">${row[ep.rate]}</td>
            <td data-label="Min Transaction">${row.min_transactionvalue}</td>
            <td data-label="Max Transaction">${row.max_transactionValue}</td>
          </tr>`;
      });
    }
  } catch (error) {
    console.error('Error loading rates:', error);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  loadRates();

  // Theme toggle with persistence
  const themeBtn = document.getElementById('themeBtn');
  const body = document.body;
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme) {
    body.classList.remove('light', 'dark');
    body.classList.add(savedTheme);
    themeBtn.textContent = savedTheme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode';
  }
  themeBtn.addEventListener('click', () => {
    if (body.classList.contains('light')) {
      body.classList.remove('light');
      body.classList.add('dark');
      themeBtn.textContent = 'Switch to Light Mode';
      localStorage.setItem('theme', 'dark');
    } else {
      body.classList.remove('dark');
      body.classList.add('light');
      themeBtn.textContent = 'Switch to Dark Mode';
      localStorage.setItem('theme', 'light');
    }
  });

  // Modal logic
  const modal = document.getElementById('registrationModal');
  const openBtn = document.getElementById('openFormBtn');
  const closeBtn = document.getElementById('closeFormBtn');

  openBtn.addEventListener('click', () => {
    modal.style.display = 'block';
    document.body.classList.add('modal-open');
  });

  closeBtn.addEventListener('click', () => {
    modal.style.display = 'none';
    document.body.classList.remove('modal-open');
  });

  window.addEventListener('click', (event) => {
    if (event.target === modal) {
      modal.style.display = 'none';
      document.body.classList.remove('modal-open');
    }
  });




// Handle form submission
const form = document.getElementById('registrationForm');
form.addEventListener('submit', async (e) => {
  e.preventDefault();

  const formData = {
    customer_name: document.getElementById('customer_name').value,
    customer_Phone_No: document.getElementById('phone_No').value,
    customer_email: document.getElementById('customer_email').value,
    customer_primary_country: document.getElementById('primary_country').value,
    customer_secondary_country: document.getElementById('secondary_country').value
  };

  try {
    const res = await fetch('/api/customers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData)
    });

    const data = await res.json();

    if (data.error) {
      // Show server error message
      alert(data.error);
    } else {
      // Show success message with password only
      alert(`You are now a registered customer!\nYour new password is:\n${data.customer_passwordID}`);
      modal.style.display = 'none';
      form.reset();
      document.body.classList.remove('modal-open');
    }
  } catch (err) {
    console.error('Error submitting form:', err);
    alert('Failed to register customer');
  }

  
});

});



document.querySelectorAll('nav a').forEach(link => {
  link.addEventListener('click', e => {
    // Get the target section ID from the href (e.g. "#congosection")
    const targetId = link.getAttribute('href').substring(1);
    const targetSection = document.getElementById(targetId);

    if (targetSection) {
      // Remove any existing blink class
      targetSection.classList.remove('blink');

      // Force reflow so animation can restart
      void targetSection.offsetWidth;

      // Add blink class
      targetSection.classList.add('blink');

      // Remove blink class after animation ends
      setTimeout(() => {
        targetSection.classList.remove('blink');
      }, 3000); // matches 3 blinks at 1s each
    }
  });
});
