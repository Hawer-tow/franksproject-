// backend/server.js
require('dotenv').config({ path: __dirname + '/.env' });
const express = require('express');
const session = require('express-session');
const path = require('path');
const requireLogin = require('./middleware/requireLogin');  // <-- import middleware

const app = express();
const PORT = 3000;

// Middleware: parse JSON and form data
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Session setup
app.use(session({
  secret: 'your-secret-key',   // replace with a strong secret
  resave: false,
  saveUninitialized: false,
  cookie: {
    secure: false,             // true if HTTPS
    httpOnly: true,
    sameSite: 'lax'
  }
}));

// Prevent caching of protected pages
app.use((req, res, next) => {
  res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, private');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');
  next();
});

// Serve static files (HTML, CSS, JS) from the legacy Customers pages
app.use(express.static(path.join(__dirname, '../legacy/Customers')));

// Import modular routes
app.use('/api/customers', require('./routes/customers'));
app.use('/api/auth', require('./routes/auth'));
app.use('/api/rates', require('./routes/rates'));
app.use('/api/admin', require('./routes/admin'));

// Protect your customer page
app.get('/registeredcustomerhtml.html', requireLogin, (req, res) => {
  res.sendFile(path.join(__dirname, '../Customers', 'registeredcustomerhtml.html'));
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT}`);
});
