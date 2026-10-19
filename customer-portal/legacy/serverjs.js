require('dotenv').config({ path: __dirname + '/../backend/.env' });
const crypto = require('crypto');
const express = require('express');
const mysql = require('mysql2/promise');
const path = require('path');

const app = express();
const PORT = 3000;

const db = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME
});

const session = require('express-session');

app.use(session({
  secret: 'your-secret-key',   // replace with a strong secret
  resave: false,
  saveUninitialized: false,
  cookie: {
    secure: false,             // true if HTTPS
    httpOnly: true,            // prevents JS access
    sameSite: 'lax'            // helps against CSRF
  }
}));

// Middleware to prevent caching of protected pages
app.use((req, res, next) => {
  res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, private');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');
  next();
});


// Middleware to parse JSON and form data
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve static files (HTML, CSS, JS)
app.use(express.static(path.join(__dirname, 'Customers')));

// Utility: get today’s date in YYYY-MM-DD
function getTodayDate() {
  const today = new Date();
  return today.toISOString().split('T')[0];
}

// Helper: format rows with placeholders
function formatRows(rows, today, dateField, rateField) {
  if (rows.length === 0) {
    return [{
      [dateField]: today,
      [rateField]: 'Rate not set',
      min_transactionvalue: 'Min not set',
      max_transactionValue: 'Max not set'
    }];
  }
  return rows.map(r => ({
    [dateField]: r[dateField],
    [rateField]: r[rateField] === 0 ? 'Rate not set' : r[rateField],
    min_transactionvalue: r.min_transactionvalue === 0 ? 'Min not set' : r.min_transactionvalue,
    max_transactionValue: r.max_transactionValue === 0 ? 'Max not set' : r.max_transactionValue
  }));
}

// -------------------- NEW CUSTOMER REGISTRATION ENDPOINT --------------------
app.post('/api/customers', async (req, res) => {
  try {
    const {
      customer_name,
      customer_Phone_No,
      customer_email,
      customer_primary_country,
      customer_secondary_country
    } = req.body;

    const [existing] = await db.query(
      'SELECT customer_ID FROM customers WHERE customer_Phone_No = ?',
      [customer_Phone_No]
    );

    if (existing.length > 0) {
      return res.status(400).json({
        error: 'You are already registered with this phone number'
      });
    }

    // Insert customer first
    const [result] = await db.query(
      `INSERT INTO customers 
       (customer_name, customer_Phone_No, customer_email, customer_primary_country, customer_secondary_country) 
       VALUES (?, ?, ?, ?, ?)`,
      [customer_name, customer_Phone_No, customer_email, customer_primary_country, customer_secondary_country]
    );

    const customer_ID = result.insertId;

    // Assign theme color cyclically
    const themeColors = ["purple", "blue", "orange", "teal", "pink"];
    const [countRows] = await db.query("SELECT COUNT(*) AS count FROM customers");
    const assignedColor = themeColors[countRows[0].count % themeColors.length];

    await db.query(
      'UPDATE customers SET theme_color = ? WHERE customer_ID = ?',
      [assignedColor, customer_ID]
    );

    // Generate password ID (your existing logic)
    const [[primaryRow]] = await db.query(
      'SELECT country_code FROM country_codes WHERE country_name = ?',
      [customer_primary_country]
    );
    const [[secondaryRow]] = await db.query(
      'SELECT country_code FROM country_codes WHERE country_name = ?',
      [customer_secondary_country]
    );

    if (!primaryRow || !secondaryRow) {
      return res.status(400).json({ error: 'Invalid country provided' });
    }

    const primaryCode = primaryRow.country_code;
    const secondaryCode = secondaryRow.country_code;

    const now = new Date();
    const year = now.getFullYear().toString().slice(-2);
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const hour = String(now.getHours()).padStart(2, '0');
    const minute = String(now.getMinutes()).padStart(2, '0');
    const yearMonthTime = `${year}${month}${hour}${minute}`;

    const customer_passwordID = `Fm/${yearMonthTime}/${primaryCode}6${secondaryCode}/${customer_ID}`;

    await db.query(
      'UPDATE customers SET customer_passwordID = ? WHERE customer_ID = ?',
      [customer_passwordID, customer_ID]
    );

    res.json({
      message: 'Customer registered successfully',
      customer_passwordID,
      theme_color: assignedColor
    });

  } catch (err) {
    console.error('Error registering customer:', err);
    res.status(500).json({ error: err.message });
  }
});


//---------------------Login endpoints ---------------------------------
app.post('/api/login', async (req, res) => {
  try {
    const { customer_Phone_No, customer_passwordID } = req.body;

    const [rows] = await db.query(
      'SELECT * FROM customers WHERE customer_Phone_No = ? AND customer_passwordID = ?',
      [customer_Phone_No, customer_passwordID]
    );

    if (rows.length === 0) {
      return res.status(400).json({ error: 'Invalid phone number or password' });
    }

    // Store customer ID in session
    req.session.customerId = rows[0].customer_ID;

    res.json({
      message: 'Login successful',
      customer_name: rows[0].customer_name,
      customer_Phone_No: rows[0].customer_Phone_No,
      theme_color: rows[0].theme_color
    });
  } catch (err) {
    console.error('Login error:', err);
    res.status(500).json({ error: err.message });
  }
});

//---------------------logout------------------------------
app.post('/api/logout', (req, res) => {
  req.session.destroy(err => {
    if (err) {
      return res.status(500).json({ error: 'Logout failed' });
    }
    res.clearCookie('connect.sid'); // remove session cookie
    res.json({ message: 'Logged out successfully' });
  });
});
function requireLogin(req, res, next) {
  if (!req.session || !req.session.customerId) {
    return res.redirect('/loginhtmlformhtml.html');
  }
  next();
}

// Protect your customer page
app.get('/registeredcustomerhtml.html', requireLogin, (req, res) => {
  res.sendFile(path.join(__dirname, 'Customers', 'registeredcustomerhtml.html'));
});






//------------------------------ Example: server.js or routes/customers.js--------------------

app.get('/api/currentCustomer', async (req, res) => {
  try {
    // For demo purposes, assume you store the logged-in customer ID in session or JWT
    const customerId = req.session?.customerId || req.user?.customerId;

    if (!customerId) {
      return res.status(401).json({ error: 'Not logged in' });
    }

    // Query the database for this customer
    const [rows] = await db.query(
      'SELECT customer_name, customer_Phone_No, theme_color FROM customers WHERE customer_ID = ?',
      [customerId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ error: 'Customer not found' });
    }

    const customer = rows[0];

    res.json({
      customer_name: customer.customer_name,
      customer_Phone_No: customer.customer_Phone_No,
      theme_color: customer.theme_color
    });
  } catch (err) {
    console.error('Error retrieving current customer:', err);
    res.status(500).json({ error: 'Server error' });
  }
});


//---------------------Reset password ---------------------------------
const nodemailer = require('nodemailer');

app.post('/api/reset-password', async (req, res) => {
  try {
    const { customer_email } = req.body;

    if (!customer_email) {
      return res.status(400).json({ error: "Email is required" });
    }

    const [rows] = await db.query(
      'SELECT customer_ID FROM customers WHERE customer_email = ?',
      [customer_email]
    );

    if (!rows || rows.length === 0) {
      return res.status(400).json({ error: "Email not found" });
    }

    const customer_ID = rows[0].customer_ID;

    if (!customer_ID) {
      return res.status(400).json({ error: "Invalid customer record" });
    }

    const randomString = crypto.randomBytes(8).toString('hex');
    const newPasswordID = `Fm/${randomString}/${customer_ID}`;

    const [updateResult] = await db.query(
      'UPDATE customers SET customer_passwordID = ? WHERE customer_ID = ?',
      [newPasswordID, customer_ID]
    );

    if (!updateResult || updateResult.affectedRows === 0) {
      return res.status(500).json({ error: "Failed to update password" });
    }

    const transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        type: "OAuth2",
        user: "tnthawer1@gmail.com",
        clientId: process.env.GMAIL_CLIENT_ID,
        clientSecret: process.env.GMAIL_CLIENT_SECRET,
        refreshToken: process.env.GMAIL_REFRESH_TOKEN
      }
    });

    const mailOptions = {
      from: "tnthawer1@gmail.com",
      to: customer_email,
      subject: "Your Reset Password",
      text: `Hello,\n\nYour new password is: ${newPasswordID}\n\nRegards,\nFrank Mpesa`
    };

    await transporter.sendMail(mailOptions);

    res.json({ message: "New password sent to your email" });
  } catch (err) {
    console.error("Reset password error:", err);
    res.status(500).json({ error: err.message });
  }
});

// -------------------- Rate Endpoints --------------------
app.get('/api/kenya2ugxrates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM kenya2Ugxrates WHERE DATE(kenya2Ugxratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'kenya2Ugxratedate', 'kenya2Ugxrate'));
  } catch (err) {
    console.error("Error in /api/kenya2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

// Repeat this pattern for all other rate endpoints:
app.get('/api/ugx2kenyarates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM Ugx2kenyarates WHERE DATE(Ugx2kenyaratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'Ugx2kenyaratedate', 'Ugx2kenyarate'));
  } catch (err) {
    console.error("Error in /api/ugx2kenyarates:", err);
    res.status(500).json({ error: err.message });
  }
});

// ... do the same for ugx2congorates, congo2ugxrates, ugx2tanzaniarates, tanzania2ugxrates, ugx2rwandarates, rwanda2ugxrates, and all admin endpoints.

app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT}`);
});

// --- Admin endpoints (full dataset, raw values) ---
app.get('/api/admin/kenya2ugxrates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM kenya2Ugxrates ORDER BY kenya2Ugxratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/kenya2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/admin/ugx2kenyarates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM Ugx2kenyarates ORDER BY Ugx2kenyaratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/ugx2kenyarates:", err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/admin/ugx2congorates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM Ugx2congorates ORDER BY Ugx2congoratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/ugx2congorates:", err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/admin/congo2ugxrates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM congo2Ugxrates ORDER BY congo2Ugxratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/congo2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/admin/ugx2tanzaniarates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM Ugx2tanzaniarates ORDER BY Ugx2tanzaniaratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/ugx2tanzaniarates:", err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/admin/tanzania2ugxrates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM tanzania2Ugxrates ORDER BY tanzania2Ugxratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/tanzania2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/admin/ugx2rwandarates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM Ugx2rwandarates ORDER BY Ugx2rwandaratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/ugx2rwandarates:", err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/admin/rwanda2ugxrates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM rwanda2Ugxrates ORDER BY rwanda2Ugxratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/rwanda2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

