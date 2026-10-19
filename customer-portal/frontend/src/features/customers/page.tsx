// src/features/customers/Page.tsx
import React, { useEffect, useState } from "react";
import "../../styles/customercss.css";

function formatDate(isoString: string) {
  const date = new Date(isoString);
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

export default function CustomerPage() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
  const [rates, setRates] = useState<Record<string, any[]>>({});
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    async function loadRates() {
      try {
        const endpoints = [
          { url: "/api/kenya2ugxrates", key: "kenya2ugxTable", caption: "Kenya → Uganda Rates", section: "Kenya" },
          { url: "/api/ugx2kenyarates", key: "ugx2kenyaTable", caption: "Uganda → Kenya Rates", section: "Kenya" },
          { url: "/api/ugx2tanzaniarates", key: "ugx2tanzaniaTable", caption: "Uganda → Tanzania Rates", section: "Tanzania" },
          { url: "/api/tanzania2ugxrates", key: "tanzania2ugxTable", caption: "Tanzania → Uganda Rates", section: "Tanzania" },
          { url: "/api/ugx2congorates", key: "ugx2congoTable", caption: "Uganda → Congo Rates", section: "Congo" },
          { url: "/api/congo2ugxrates", key: "congo2ugxTable", caption: "Congo → Uganda Rates", section: "Congo" },
          { url: "/api/ugx2rwandarates", key: "ugx2rwandaTable", caption: "Uganda → Rwanda Rates", section: "Rwanda" },
          { url: "/api/rwanda2ugxrates", key: "rwanda2ugxTable", caption: "Rwanda → Uganda Rates", section: "Rwanda" },
        ];

        const results: Record<string, any[]> = {};
        for (const ep of endpoints) {
          const res = await fetch(ep.url);
          const data = await res.json();
          results[ep.key] = data.map((row: any) => ({
            date: formatDate(row.date || row.ratedate),
            rate: row.rate,
            min: row.min_transactionvalue,
            max: row.max_transactionValue,
            caption: ep.caption,
            section: ep.section,
          }));
        }
        setRates(results);
      } catch (error) {
        console.error("Error loading rates:", error);
      }
    }
    loadRates();
  }, []);

  useEffect(() => {
    document.body.className = theme;
    localStorage.setItem("theme", theme);
  }, [theme]);

  const toggleTheme = () => setTheme(theme === "light" ? "dark" : "light");

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const payload = {
      customer_name: formData.get("customer_name"),
      customer_Phone_No: formData.get("phone_No"),
      customer_email: formData.get("customer_email"),
      customer_primary_country: formData.get("primary_country"),
      customer_secondary_country: formData.get("secondary_country"),
    };

    try {
      const res = await fetch("/api/customers", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const data = await res.json();
      if (data.error) {
        alert(data.error);
      } else {
        alert(`You are now a registered customer!\nYour new password is:\n${data.customer_passwordID}`);
        setShowModal(false);
        e.currentTarget.reset();
      }
    } catch (err) {
      console.error("Error submitting form:", err);
      alert("Failed to register customer");
    }
  };

  return (
    <div>
      <header>
        <h1>Welcome To Frank Mpesa</h1>
        <div className="header-buttons">
          <button onClick={toggleTheme}>
            {theme === "light" ? "Switch to Dark Mode" : "Switch to Light Mode"}
          </button>
          <button onClick={() => setShowModal(true)}>Register Customer</button>
          <button onClick={() => (window.location.href = "/login")}>Login</button>
        </div>
      </header>

      <nav>
        <a href="#Tanzania">Visit Tanzania</a>
        <a href="#Congo">Visit Congo</a>
        <a href="#Rwanda">Visit Rwanda</a>
      </nav>

      <main>
        {["Kenya", "Tanzania", "Congo", "Rwanda"].map((country) => (
          <section id={country} key={country}>
            <center><h2>{country} Transactions</h2></center>
            <hr /><br />
            {Object.entries(rates)
              .filter(([_, rows]) => rows.length && rows[0].section === country)
              .map(([key, rows]) => (
                <div className="table-container" key={key}>
                  <table>
                    <caption>{rows[0].caption}</caption>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Rate</th>
                        <th>Min Transaction</th>
                        <th>Max Transaction</th>
                      </tr>
                    </thead>
                    <tbody>
                      {rows.map((row, idx) => (
                        <tr key={idx}>
                          <td>{row.date}</td>
                          <td>{row.rate}</td>
                          <td>{row.min}</td>
                          <td>{row.max}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ))}
          </section>
        ))}
      </main>

      <footer>
        <p>&copy; Winter Hawer 2026</p>
      </footer>

      <button id="backToTop" title="Go to top" onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}>
        ↑
      </button>

      {showModal && (
        <div className="modal">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Customer Registration</h2>
              <span className="close" onClick={() => setShowModal(false)}>&times;</span>
            </div>
            <div className="form-body">
              <form id="registrationForm" onSubmit={handleSubmit}>
                <label htmlFor="customer_name">Full Name:</label>
                <input type="text" id="customer_name" name="customer_name" required />

                <label htmlFor="customer_email">Email:</label>
                <input type="email" id="customer_email" name="customer_email" />

                <label htmlFor="phone_No">Phone:</label>
                <input type="tel" id="phone_No" name="phone_No" required />

                <label htmlFor="primary_country">Home Country:</label>
                <select id="primary_country" name="primary_country" required>
                  <option>Uganda</option>
                  <option>Kenya</option>
                  <option>Congo</option>
                  <option>Tanzania</option>
                  <option>Rwanda</option>
                </select>

                <label htmlFor="secondary_country">Secondary Country:</label>
                <select id="secondary_country" name="secondary_country">
                  <option>Uganda</option>
                  <option>Kenya</option>
                  <option>Congo</option>
                  <option>Tanzania</option>
                  <option>Rwanda</option>
                </select>

                <div className="form-footer">
                  <button type="submit">Register</button>
                  <br /><br />
                  <a href="/login">I have an account</a>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
