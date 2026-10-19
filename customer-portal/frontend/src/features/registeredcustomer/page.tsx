// src/features/RegisteredCustomer/Page.tsx
import React, { useEffect, useState } from "react";
import "../../styles/registeredcustomercss.css";

function formatDate(isoString: string) {
  const date = new Date(isoString);
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

export default function RegisteredCustomerPage() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
  const [rates, setRates] = useState<Record<string, any[]>>({});
  const [customer, setCustomer] = useState<any>(null);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);

  // Load rates
  useEffect(() => {
    async function loadRates() {
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
          date: formatDate(row[ep.key + "date"] || row.date),
          rate: row[ep.key + "rate"] || row.rate,
          min: row.min_transactionvalue,
          max: row.max_transactionValue,
          caption: ep.caption,
          section: ep.section,
        }));
      }
      setRates(results);
    }
    loadRates();
  }, []);

  // Load customer info
  useEffect(() => {
    async function fetchCustomer() {
      try {
        const res = await fetch("/api/currentCustomer");
        const data = await res.json();
        setCustomer(data);
        if (data.theme_color) {
          localStorage.setItem("themeColor", data.theme_color);
          setTheme("personalized");
        }
      } catch (err) {
        console.error("Failed to load customer info:", err);
      }
    }
    fetchCustomer();
  }, []);

  // Theme toggle
  useEffect(() => {
    document.body.className = theme;
    localStorage.setItem("theme", theme);
  }, [theme]);

  const toggleTheme = () => {
    if (theme === "personalized") setTheme("light");
    else if (theme === "light") setTheme("dark");
    else if (theme === "dark") setTheme("personalized");
  };

  // Logout
  const handleLogout = async () => {
    await fetch("/api/logout", { method: "POST" }).catch(() => {});
    localStorage.clear();
    sessionStorage.clear();
    window.location.replace("/loginhtmlformhtml.html");
  };

  return (
    <div>
      <header>
        <h1>Welcome To Frank Mpesa</h1>
        <h1>Personal Rates</h1>
        <div className="header-buttons">
          <div id="customerProfile" className="customer-icon" onClick={() => setProfileMenuOpen(!profileMenuOpen)}>
            <span id="customerInitials">
              {customer ? customer.customer_name.split(" ").map((w: string) => w[0]).join("").toUpperCase() : "CU"}
            </span>
            <div className="customer-info">
              <p id="customerName">{customer ? customer.customer_name : "Customer Name"}</p>
              <p id="customerPhone">{customer ? customer.customer_Phone_No : "+254700000000"}</p>
            </div>
            {profileMenuOpen && (
              <div id="profileMenu" className="profile-menu">
                <button id="logoutBtn" onClick={handleLogout}>Logout</button>
              </div>
            )}
          </div>
          <button id="themeBtn" onClick={toggleTheme}>
            {theme === "light" ? "Switch to Dark Mode" : theme === "dark" ? "Switch to Personalized Theme" : "Switch to Light Theme"}
          </button>
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
    </div>
  );
}
