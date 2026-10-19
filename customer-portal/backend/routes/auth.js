// backend/routes/auth.js
const express = require('express');
const router = express.Router();
const db = require('../db/pool');
const crypto = require('crypto');
const nodemailer = require('nodemailer');

// -------------------- LOGIN --------------------
router.post('/login', async (req, res) => {
  try {
    const { customer_Phone_No, customer_passwordID } = req.body;

    const [rows] = await db.query(
      'SELECT * FROM customers WHERE customer_Phone_No = ? AND customer_passwordID = ?',
      [customer_Phone_No, customer_passwordID]
    );

    if (!rows.length) {
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

// -------------------- LOGOUT --------------------
router.post('/logout', (req, res) => {
  req.session.destroy(err => {
    if (err) return res.status(500).json({ error: 'Logout failed' });
    res.clearCookie('connect.sid'); // remove session cookie
    res.json({ message: 'Logged out successfully' });
  });
});

// -------------------- RESET PASSWORD --------------------
router.post('/reset-password', async (req, res) => {
  try {
    const { customer_email } = req.body;
    if (!customer_email) return res.status(400).json({ error: "Email is required" });

    const [rows] = await db.query(
      'SELECT customer_ID FROM customers WHERE customer_email = ?',
      [customer_email]
    );
    if (!rows.length) return res.status(400).json({ error: "Email not found" });

    const customer_ID = rows[0].customer_ID;
    const randomString = crypto.randomBytes(8).toString('hex');
    const newPasswordID = `Fm/${randomString}/${customer_ID}`;

    const [updateResult] = await db.query(
      'UPDATE customers SET customer_passwordID = ? WHERE customer_ID = ?',
      [newPasswordID, customer_ID]
    );
    if (!updateResult.affectedRows) return res.status(500).json({ error: "Failed to update password" });

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

    await transporter.sendMail({
      from: "tnthawer1@gmail.com",
      to: customer_email,
      subject: "Your Reset Password",
      text: `Hello,\n\nYour new password is: ${newPasswordID}\n\nRegards,\nFrank Mpesa`
    });

    res.json({ message: "New password sent to your email" });
  } catch (err) {
    console.error("Reset password error:", err);
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
