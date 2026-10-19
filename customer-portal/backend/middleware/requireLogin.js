// backend/middleware/requireLogin.js

function requireLogin(req, res, next) {
  if (!req.session || !req.session.customerId) {
    // If not logged in, redirect to login page
    return res.redirect('/loginhtmlformhtml.html');
  }
  next();
}

module.exports = requireLogin;
