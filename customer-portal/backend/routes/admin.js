// backend/routes/admin.js
const express = require('express');
const router = express.Router();
const db = require('../db/pool');

// -------------------- Admin rate endpoints --------------------
router.get('/kenya2ugxrates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM kenya2Ugxrates ORDER BY kenya2Ugxratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/kenya2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

router.get('/ugx2kenyarates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM Ugx2kenyarates ORDER BY Ugx2kenyaratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/ugx2kenyarates:", err);
    res.status(500).json({ error: err.message });
  }
});

router.get('/ugx2congorates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM Ugx2congorates ORDER BY Ugx2congoratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/ugx2congorates:", err);
    res.status(500).json({ error: err.message });
  }
});

router.get('/congo2ugxrates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM congo2Ugxrates ORDER BY congo2Ugxratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/congo2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

router.get('/ugx2tanzaniarates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM Ugx2tanzaniarates ORDER BY Ugx2tanzaniaratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/ugx2tanzaniarates:", err);
    res.status(500).json({ error: err.message });
  }
});

router.get('/tanzania2ugxrates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM tanzania2Ugxrates ORDER BY tanzania2Ugxratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/tanzania2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

router.get('/ugx2rwandarates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM Ugx2rwandarates ORDER BY Ugx2rwandaratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/ugx2rwandarates:", err);
    res.status(500).json({ error: err.message });
  }
});

router.get('/rwanda2ugxrates', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM rwanda2Ugxrates ORDER BY rwanda2Ugxratedate DESC');
    res.json(rows);
  } catch (err) {
    console.error("Error in /api/admin/rwanda2ugxrates:", err);
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
