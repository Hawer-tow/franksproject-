// src/features/auth/LoginPage.tsx
import React, { useState } from "react";
import "../../../styles/login.css";


export default function LoginPage() {
  const [errorMsg, setErrorMsg] = useState("");

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const loginData = {
      customer_Phone_No: formData.get("phone_No"),
      customer_passwordID: formData.get("passwordID"),
    };

    try {
      const res = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(loginData),
      });
      const data = await res.json();

      if (data.error) {
        setErrorMsg(data.error);
      } else {
        window.location.replace("/registeredcustomerhtml.html");
      }
    } catch (err) {
      console.error("Login error:", err);
      setErrorMsg("Login failed. Please try again.");
    }
  };

  const handleForgotPassword = async () => {
    const email = prompt("Enter your registered email:");
    if (!email) return;

    try {
      const res = await fetch("/api/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ customer_email: email }),
      });

      const text = await res.text();
      let data;
      try {
        data = JSON.parse(text);
      } catch {
        alert("Server did not return JSON. Raw response:\n" + text);
        return;
      }

      if (!res.ok) {
        alert("Error: " + (data.error || "Unknown error"));
      } else {
        alert(data.message || "Your new password has been sent to your email.");
      }
    } catch (err: any) {
      alert("Network or server error: " + err.message);
    }
  };

  return (
    <div className="login-box">
      <h2>Customer Login</h2>
      <form id="loginForm" onSubmit={handleSubmit}>
        <input type="text" name="phone_No" placeholder="Phone Number" required />
        <input type="text" name="passwordID" placeholder="Password ID" required />
        <button type="submit">Login</button>
      </form>
      {errorMsg && <div className="error">{errorMsg}</div>}
      <div className="forgot">
        <a href="#" onClick={handleForgotPassword}>Forgot Password?</a>
      </div>
    </div>
  );
}
