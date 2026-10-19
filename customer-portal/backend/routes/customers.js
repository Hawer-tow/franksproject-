// backend/routes/customers.js
const express = require('express');
const router = express.Router();
const db = require('../db/pool');

// Utility: get today’s date in YYYY-MM-DD
function getTodayDate() {
  return new Date().toISOString().split('T')[0];
}

// Helper: format rows with placeholders
function formatRows(rows, today, dateField, rateField) {
  if (!rows.length) {
    return [{
      [dateField]: today,
      [rateField]: 'Rate not set',
      min_transactionvalue: 'Min not set',
      max_transactionValue: 'Max not set'
    }];
  }
  return rows.map(r => ({
    [dateField]: r[dateField],
    [rateField]: r[rateField] || 'Rate not set',
    min_transactionvalue: r.min_transactionvalue || 'Min not set',
    max_transactionValue: r.max_transactionValue || 'Max not set'
  }));
}

// -------------------- NEW CUSTOMER REGISTRATION --------------------
router.post('/', async (req, res) => {
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
      return res.status(400).json({ error: 'Already registered with this phone number' });
    }

    // Insert customer
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

    // Generate password ID
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

// -------------------- CURRENT CUSTOMER --------------------
router.get('/currentCustomer', async (req, res) => {
  try {
    const customerId = req.session?.customerId || req.user?.customerId;
    if (!customerId) return res.status(401).json({ error: 'Not logged in' });

    const [rows] = await db.query(
      'SELECT customer_name, customer_Phone_No, theme_color FROM customers WHERE customer_ID = ?',
      [customerId]
    );
    if (!rows.length) return res.status(404).json({ error: 'Customer not found' });

    res.json(rows[0]);
  } catch (err) {
    console.error('Error retrieving current customer:', err);
    res.status(500).json({ error: 'Server error' });
  }
});

module.exports = router;
