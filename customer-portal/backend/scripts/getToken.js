// getToken.js - one-off helper to exchange a Google OAuth code for a Gmail refresh token.
// Usage (from customer-portal/backend):  node scripts/getToken.js "<code from the consent URL>"
// Needs: npm install googleapis   (not a regular dependency of the backend)
require('dotenv').config({ path: __dirname + '/../.env' });
const { google } = require('googleapis');

const CLIENT_ID = process.env.GMAIL_CLIENT_ID;
const CLIENT_SECRET = process.env.GMAIL_CLIENT_SECRET;
const REDIRECT_URI = process.env.GMAIL_REDIRECT_URI;

const oAuth2Client = new google.auth.OAuth2(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);

// The code you got from the consent URL, passed on the command line:
const code = process.argv[2];
if (!code) {
  console.error('Usage: node scripts/getToken.js "<code>"');
  process.exit(1);
}

async function getToken() {
  try {
    const { tokens } = await oAuth2Client.getToken(code);
    console.log("Access Token:", tokens.access_token);
    console.log("Refresh Token:", tokens.refresh_token);
    console.log("Tokens object:", tokens);
  } catch (err) {
    console.error("Error retrieving tokens:", err);
  }
}

getToken();
