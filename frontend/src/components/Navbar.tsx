import { Link, NavLink, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";

export function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await api.logout();
    } catch {
      // stateless logout — ignore network/API errors and clear locally anyway
    }
    logout();
    showToast("You have been logged out.", "info");
    navigate("/");
  };

  return (
    <nav className="tt-nav">
      <div className="tt-nav-inner">
        <Link to="/" className="tt-brand">
          <span className="tt-brand-mark">₹</span>
          <span className="tt-brand-text">TaxTracker</span>
        </Link>

        <div className="tt-nav-links">
          {isAuthenticated ? (
            <>
              <NavLink to="/dashboard">Dashboard</NavLink>
              <NavLink to="/insights">Insights</NavLink>
              <NavLink to="/estimator">Estimator</NavLink>
              <NavLink to="/form90c">Form 90C</NavLink>
              {user && <span className="tt-nav-user">{user.email}</span>}
              <button className="tt-btn tt-btn-ghost" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/" end>
                Home
              </NavLink>
              <NavLink to="/estimator">Estimator</NavLink>
              <NavLink to="/login">Log in</NavLink>
              <NavLink to="/register" className="tt-btn tt-btn-solid">
                Get started
              </NavLink>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
