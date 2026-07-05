import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, extractError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { AuthShell, Field } from "../components/Field";

export function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { showToast } = useToast();

  const [emailId, setEmailId] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async () => {
    setError("");
    if (!emailId || !password) {
      setError("Enter your email and password to continue.");
      return;
    }
    setLoading(true);
    try {
      const res = await api.login(emailId, password);
      login(res.data);
      showToast("Login successful", "success");
      navigate("/dashboard");
    } catch (err) {
      setError(extractError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Log in to read your transactions and file Form 90C."
      footer={
        <span>
          New here? <Link to="/register">Create an account</Link>
        </span>
      }
    >
      {error && <div className="tt-form-error">{error}</div>}
      <Field label="Email">
        <input
          className="tt-input"
          type="email"
          value={emailId}
          onChange={(e) => setEmailId(e.target.value)}
          placeholder="you@example.com"
          onKeyDown={(e) => e.key === "Enter" && submit()}
        />
      </Field>
      <Field label="Password">
        <input
          className="tt-input"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Your password"
          onKeyDown={(e) => e.key === "Enter" && submit()}
        />
      </Field>
      <button className="tt-btn tt-btn-solid full" onClick={submit} disabled={loading}>
        {loading ? "Logging in…" : "Log in"}
      </button>
    </AuthShell>
  );
}
