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


document.addEventListener('DOMContentLoaded', async () => {
  loadRates();

  const themeBtn = document.getElementById('themeBtn');
  const body = document.body;

  // Load customer info automatically from backend
try {
  const res = await fetch('/api/currentCustomer');
  const customer = await res.json();

  if (customer) {
    document.getElementById('customerName').textContent = customer.customer_name;
    document.getElementById('customerPhone').textContent = customer.customer_Phone_No;

    const initials = customer.customer_name
      .split(' ')
      .map(word => word[0])
      .join('')
      .toUpperCase();
    document.getElementById('customerInitials').textContent = initials;

    // Save theme color for this user
    if (customer.theme_color) {
      localStorage.setItem('themeColor', customer.theme_color);
      applyPersonalizedTheme(customer.theme_color);
      body.classList.remove('light', 'dark');
      body.classList.add('personalized');
      themeBtn.textContent = 'Switch to Light Theme';
      localStorage.setItem('theme', 'personalized');
    }
  }
} catch (err) {
  console.error('Failed to load customer info:', err);
}




  // Load saved theme or personalized color
  const savedTheme = localStorage.getItem('theme');
  const savedColor = localStorage.getItem('themeColor'); // personalized color from DB

  if (savedColor) {
    // Start in personalized mode if a DB color exists
    body.classList.remove('light', 'dark');
    body.classList.add('personalized');
    applyPersonalizedTheme(savedColor);
    themeBtn.textContent = 'Switch to Light Theme';
    localStorage.setItem('theme', 'personalized');
  } else if (savedTheme) {
    body.classList.remove('light', 'dark', 'personalized');
    body.classList.add(savedTheme);
    if (savedTheme === 'dark') {
      document.documentElement.style.setProperty('--accent-color', '#00ff99');
      document.documentElement.style.setProperty('--bg-color', '#121212');
      document.documentElement.style.setProperty('--text-color', '#f0f0f0');
      themeBtn.textContent = 'Switch to Personalized Theme';
    } else {
      document.documentElement.style.setProperty('--accent-color', 'green');
      document.documentElement.style.setProperty('--bg-color', '#ffffff');
      document.documentElement.style.setProperty('--text-color', '#1C2A42');
      themeBtn.textContent = 'Switch to Dark Theme';
    }
  } else {
    // Default to light theme
    body.classList.add('light');
    document.documentElement.style.setProperty('--accent-color', 'green');
    document.documentElement.style.setProperty('--bg-color', '#ffffff');
    document.documentElement.style.setProperty('--text-color', '#1C2A42');
    themeBtn.textContent = 'Switch to Dark Theme';
    localStorage.setItem('theme', 'light');
  }

  // Theme toggle cycles through personalized → light → dark
  themeBtn.addEventListener('click', () => {
    if (body.classList.contains('personalized')) {
      body.classList.remove('personalized');
      body.classList.add('light');
      document.documentElement.style.setProperty('--bg-color', '#ffffff');
      document.documentElement.style.setProperty('--text-color', '#1C2A42');
      document.documentElement.style.setProperty('--accent-color', 'green');
      themeBtn.textContent = 'Switch to Dark Theme';
      localStorage.setItem('theme', 'light');
    } else if (body.classList.contains('light')) {
      body.classList.remove('light');
      body.classList.add('dark');
      document.documentElement.style.setProperty('--bg-color', '#121212');
      document.documentElement.style.setProperty('--text-color', '#f0f0f0');
      document.documentElement.style.setProperty('--accent-color', '#00ff99');
      themeBtn.textContent = 'Switch to Personalized Theme';
      localStorage.setItem('theme', 'dark');
    } else if (body.classList.contains('dark')) {
      body.classList.remove('dark');
      body.classList.add('personalized');
      const color = localStorage.getItem('themeColor') || '#deb7f4'; // fallback purple
      applyPersonalizedTheme(color);
      themeBtn.textContent = 'Switch to Light Theme';
      localStorage.setItem('theme', 'personalized');
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
        alert(data.error);
      } else {
        alert(`You are now a registered customer!\nYour new password is:\n${data.customer_passwordID}`);

        // Save and apply theme color from DB
        if (data.theme_color) {
          localStorage.setItem('themeColor', data.theme_color);
          applyPersonalizedTheme(data.theme_color);
          body.classList.remove('light', 'dark');
          body.classList.add('personalized');
          themeBtn.textContent = 'Switch to Light Theme';
          localStorage.setItem('theme', 'personalized');
        }

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

// Helper: apply personalized theme
function applyPersonalizedTheme(color) {
  document.documentElement.style.setProperty('--card-border', color);
  document.documentElement.style.setProperty('--accent-color', color);
  document.documentElement.style.setProperty('--bg-color','#deb7f4');
  document.documentElement.style.setProperty('--card-bg', '#ffffff');
 const isDark = isDarkColor(color);
  document.documentElement.style.setProperty('--text-color', isDark ? '#000000' : '#ffffff');


}

// Helper: check if color is dark
function isDarkColor(color) {
  let c = color.startsWith('#') ? color.substring(1) : color;
  let rgb = parseInt(c, 16);
  let r = (rgb >> 16) & 0xff;
  let g = (rgb >> 8) & 0xff;
  let b = (rgb >> 0) & 0xff;
  let luminance = 0.299*r + 0.587*g + 0.114*b;
  return luminance < 128;
}




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




const customerProfile = document.getElementById('customerProfile');
const profileMenu = document.getElementById('profileMenu');
const logoutBtn = document.getElementById('logoutBtn');

// Toggle menu when profile clicked
customerProfile.addEventListener('click', () => {
  profileMenu.style.display = profileMenu.style.display === 'block' ? 'none' : 'block';
});

// Logout logic

logoutBtn.addEventListener('click', async () => {
  await fetch('/api/logout', { method: 'POST' }).catch(() => {});
  localStorage.clear();
  sessionStorage.clear();
  window.location.replace('/loginhtmlformhtml.html'); 
});


// Close menu if clicking outside
document.addEventListener('click', (e) => {
  if (!customerProfile.contains(e.target)) {
    profileMenu.style.display = 'none';
  }
});


