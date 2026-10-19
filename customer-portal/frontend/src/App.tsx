import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import CustomerPage from "./features/customers/page";
import RegisteredCustomerPage from "./features/registeredcustomer/page";
import LoginPage from "./features/auth/login/page";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<CustomerPage />} />
        <Route path="/customers" element={<CustomerPage />} />
        <Route path="/registeredcustomers" element={<RegisteredCustomerPage />} />
        <Route path="/login" element={<LoginPage />} />
      </Routes>
    </Router>
  );
}

export default App;
