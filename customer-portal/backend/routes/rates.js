// backend/routes/rates.js
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

// -------------------- User-facing rate endpoints --------------------
router.get('/kenya2ugxrates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM kenya2Ugxrates WHERE DATE(kenya2Ugxratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'kenya2Ugxratedate', 'kenya2Ugxrate'));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/ugx2kenyarates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM Ugx2kenyarates WHERE DATE(Ugx2kenyaratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'Ugx2kenyaratedate', 'Ugx2kenyarate'));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Repeat for other countries
router.get('/ugx2tanzaniarates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM Ugx2tanzaniarates WHERE DATE(Ugx2tanzaniaratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'Ugx2tanzaniaratedate', 'Ugx2tanzaniarate'));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/tanzania2ugxrates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM tanzania2Ugxrates WHERE DATE(tanzania2Ugxratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'tanzania2Ugxratedate', 'tanzania2Ugxrate'));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/ugx2congorates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM Ugx2congorates WHERE DATE(Ugx2congoratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'Ugx2congoratedate', 'Ugx2congorate'));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/congo2ugxrates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM congo2Ugxrates WHERE DATE(congo2Ugxratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'congo2Ugxratedate', 'congo2Ugxrate'));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/ugx2rwandarates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM Ugx2rwandarates WHERE DATE(Ugx2rwandaratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'Ugx2rwandaratedate', 'Ugx2rwandarate'));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/rwanda2ugxrates', async (req, res) => {
  try {
    const today = getTodayDate();
    const [rows] = await db.query(
      'SELECT * FROM rwanda2Ugxrates WHERE DATE(rwanda2Ugxratedate) = ?',
      [today]
    );
    res.json(formatRows(rows, today, 'rwanda2Ugxratedate', 'rwanda2Ugxrate'));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
