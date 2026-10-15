import { useEffect, useState } from "react";
import { CarFront, ShieldCheck, UserRound, LogOut, MapPin, Menu, X } from "lucide-react";
import api, { getErrorMessage } from "./api/axios";
import RiderDashboard from "./components/RiderDashboard";
import DriverDashboard from "./components/DriverDashboard";
import AdminDashboard from "./components/AdminDashboard";

function normalizeUser(raw) {
  const user = raw?.user || raw?.data?.user || raw?.data || raw || {};
  const token = raw?.token || raw?.accessToken || raw?.jwt || raw?.data?.token || raw?.data?.accessToken || raw?.data?.jwt;
  return { user: { ...user, role: String(user.role || user.authorities?.[0]?.authority || "RIDER").replace(/^ROLE_/, "").toUpperCase() }, token };
}

export default function App() {
  const [currentUser, setCurrentUser] = useState(() => {
    try { return JSON.parse(sessionStorage.getItem("cabUser") || "null"); } catch { return null; }
  });
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ fullName: "", email: "", password: "", role: "RIDER" });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [mobileMenu, setMobileMenu] = useState(false);

  useEffect(() => {
    if (currentUser) sessionStorage.setItem("cabUser", JSON.stringify(currentUser));
    else sessionStorage.removeItem("cabUser");
  }, [currentUser]);

  function changeField(e) { setForm((old) => ({ ...old, [e.target.name]: e.target.value })); }

  async function submitAuth(e) {
    e.preventDefault(); setBusy(true); setError(""); setNotice("");
    try {
      const endpoint = mode === "login" ? "/auth/login" : "/auth/register";
      const payload = mode === "login"
          ? { email: form.email.trim(), password: form.password }
          : { fullName: form.fullName.trim(), email: form.email.trim(), password: form.password, role: form.role };
      const response = await api.post(endpoint, payload);
      const { user, token } = normalizeUser(response.data);
      if (!token) throw new Error("The server response did not include a JWT token. Check the login/register response in your backend.");
      sessionStorage.setItem("cabToken", token);
      const savedUser = { ...user, email: user.email || form.email.trim(), fullName: user.fullName || form.fullName || user.name || form.email.trim().split("@")[0] };
      sessionStorage.setItem("cabUser", JSON.stringify(savedUser));
      setCurrentUser(savedUser); setNotice("Welcome! You are signed in.");
    } catch (err) { setError(getErrorMessage(err)); }
    finally { setBusy(false); }
  }

  function logout() {
    sessionStorage.removeItem("cabToken"); sessionStorage.removeItem("cabUser");
    setCurrentUser(null); setForm({ fullName: "", email: "", password: "", role: "RIDER" });
    setMode("login"); setError(""); setNotice("");
  }

  const role = String(currentUser?.role || "RIDER").replace(/^ROLE_/, "").toUpperCase();

  if (currentUser) {
    return <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#top" onClick={(e) => e.preventDefault()}><span className="brand-icon"><CarFront size={23}/></span><span>Cab<span className="brand-accent">Swift</span><small>YOUR RIDE, YOUR WAY</small></span></a>
        <button className="mobile-menu-button" onClick={() => setMobileMenu(!mobileMenu)} aria-label="Toggle menu">{mobileMenu ? <X/> : <Menu/>}</button>
        <div className={`topbar-right ${mobileMenu ? "show" : ""}`}>
          <span className="role-chip"><ShieldCheck size={15}/> {role}</span>
          <div className="user-chip"><span className="avatar">{(currentUser.fullName || currentUser.name || currentUser.email || "U").charAt(0).toUpperCase()}</span><span><b>{currentUser.fullName || currentUser.name || "Welcome"}</b><small>{currentUser.email}</small></span></div>
          <button className="button button-quiet" onClick={logout}><LogOut size={16}/> Logout</button>
        </div>
      </header>
      <main className="main-content" id="top">
        {role === "ADMIN" ? <AdminDashboard user={currentUser}/> : role === "DRIVER" ? <DriverDashboard user={currentUser}/> : <RiderDashboard user={currentUser}/>}
      </main>
      <footer className="footer"><CarFront size={16}/> CabSwift <span>•</span> Ride safe, arrive happy.</footer>
    </div>;
  }

  return <div className="auth-page">
    <section className="auth-visual">
      <div className="auth-visual-top"><span className="brand-icon brand-icon-light"><CarFront size={25}/></span><span className="visual-brand">CabSwift</span></div>
      <div className="visual-copy"><span className="eyebrow"><MapPin size={14}/> YOUR CITY, YOUR JOURNEY</span><h1>Every journey<br/>starts <em>here.</em></h1><p>Simple, reliable rides. A smoother way to get where life takes you.</p>
        <div className="visual-stats"><div><b>Easy</b><span>Ride booking</span></div><div><b>Secure</b><span>Sign in</span></div><div><b>Reliable</b><span>Ride tracking</span></div></div>
      </div>
      <div className="visual-road"><div className="road-line"/><div className="road-car"><CarFront size={38}/></div><span className="road-pin pin-one"><MapPin size={19}/></span><span className="road-pin pin-two"><MapPin size={19}/></span></div>
      <span className="visual-footnote">MADE FOR THE WAY YOU MOVE</span>
    </section>
    <section className="auth-panel"><div className="auth-card">
      <div className="auth-heading"><span className="auth-mobile-icon"><CarFront size={22}/></span><span className="eyebrow">WELCOME TO CABSWIFT</span><h2>{mode === "login" ? "Welcome back" : "Create your account"}</h2><p>{mode === "login" ? "Sign in to continue your journey." : "A few details and you're ready to go."}</p></div>
      <div className="auth-tabs"><button className={mode === "login" ? "active" : ""} onClick={() => { setMode("login"); setError(""); }}>Sign in</button><button className={mode === "register" ? "active" : ""} onClick={() => { setMode("register"); setError(""); }}>Create account</button></div>
      {error && <div className="alert alert-error" role="alert">{error}</div>}{notice && <div className="alert alert-success">{notice}</div>}
      <form onSubmit={submitAuth} className="auth-form">
        {mode === "register" && <label>Full name<input name="fullName" value={form.fullName} onChange={changeField} placeholder="Enter your full name" autoComplete="name" required/></label>}
        <label>Email address<input name="email" type="email" value={form.email} onChange={changeField} placeholder="you@example.com" autoComplete="email" required/></label>
        <label>Password<input name="password" type="password" value={form.password} onChange={changeField} placeholder="Enter your password" autoComplete={mode === "login" ? "current-password" : "new-password"} minLength={mode === "register" ? 6 : undefined} required/></label>
        {mode === "register" && <label>Account type<select name="role" value={form.role} onChange={changeField}><option value="RIDER">Rider</option><option value="DRIVER">Driver</option></select><span className="field-help">Choose Rider to book trips or Driver to manage assigned rides. Admin access is not available through public registration.</span></label>}
        <button className="button button-primary button-full" type="submit" disabled={busy}>{busy ? "Please wait…" : mode === "login" ? "Sign in to your account" : "Create account"}</button>
      </form>
      <p className="auth-switch">{mode === "login" ? "New to CabSwift?" : "Already have an account?"} <button onClick={() => { setMode(mode === "login" ? "register" : "login"); setError(""); }}>{mode === "login" ? "Create account" : "Sign in"}</button></p>
      <div className="secure-note"><ShieldCheck size={15}/> Your account information is sent securely to your backend.</div>
    </div><p className="auth-copyright">© {new Date().getFullYear()} CabSwift. Built for better journeys.</p></section>
  </div>;
}
