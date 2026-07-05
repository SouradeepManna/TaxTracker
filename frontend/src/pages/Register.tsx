import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, extractError } from "../api/client";
import { useToast } from "../context/ToastContext";
import { AuthShell, Field } from "../components/Field";

const STATES = [
  "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat",
  "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh",
  "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan",
  "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
  "Delhi", "Jammu and Kashmir",
];

interface RegisterForm {
  name: string;
  email: string;
  password: string;
  mobNumber: string;
  addressLine1: string;
  addressLine2: string;
  area: string;
  city: string;
  state: string;
  pin: string;
}

export function Register() {
  const navigate = useNavigate();
  const { showToast } = useToast();

  // step 1 = details form, step 2 = OTP entry
  const [step, setStep] = useState<1 | 2>(1);

  const [form, setForm] = useState<RegisterForm>({
    name: "", email: "", password: "", mobNumber: "",
    addressLine1: "", addressLine2: "", area: "", city: "", state: "Karnataka", pin: "",
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<string[]>([]);

  // OTP step state
  const [otp, setOtp] = useState("");
  const [otpError, setOtpError] = useState("");

  const set =
    (k: keyof RegisterForm) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setForm({ ...form, [k]: e.target.value });

  const validate = (): string[] => {
    const errs: string[] = [];
    if (!/^[A-Za-z]+( [A-Za-z]+)*$/.test(form.name))
      errs.push("Name should only contain alphabets with single spaces between words.");
    if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.(in|com)$/.test(form.email))
      errs.push("Email must be valid and end in .in or .com");
    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{7,20}$/.test(form.password))
      errs.push("Password needs upper, lower, a digit, a special character, 7–20 chars.");
    if (!/^[6-9][0-9]{9}$/.test(form.mobNumber))
      errs.push("Mobile number must start with 6–9 and be 10 digits.");
    if (!form.addressLine1) errs.push("Address line 1 is required.");
    if (!form.area) errs.push("Area is required.");
    if (!form.city) errs.push("City is required.");
    if (!/^[0-9]{6}$/.test(form.pin)) errs.push("PIN code must be 6 digits.");
    return errs;
  };

  // Step 1: submit details -> backend emails an OTP
  const requestOtp = async () => {
    const errs = validate();
    setErrors(errs);
    if (errs.length) return;
    setLoading(true);
    try {
      await api.register({
        name: form.name,
        email: form.email,
        password: form.password,
        mobNumber: form.mobNumber,
        address: {
          addressLine1: form.addressLine1,
          addressLine2: form.addressLine2,
          area: form.area,
          city: form.city,
          state: form.state,
          pin: form.pin,
        },
      });
      showToast("OTP sent to your email. Enter it to finish.", "success");
      setStep(2);
    } catch (err) {
      setErrors([extractError(err)]);
    } finally {
      setLoading(false);
    }
  };

  // Step 2: verify OTP -> account created + confirmation email
  const verifyOtp = async () => {
    setOtpError("");
    if (!/^[0-9]{6}$/.test(otp)) {
      setOtpError("Enter the 6-digit OTP sent to your email.");
      return;
    }
    setLoading(true);
    try {
      await api.verifyOtp({ email: form.email, otp });
      showToast("Account verified — please log in.", "success");
      navigate("/login");
    } catch (err) {
      setOtpError(extractError(err));
    } finally {
      setLoading(false);
    }
  };

  // Resend a fresh OTP by re-initiating registration.
  const resendOtp = async () => {
    setOtpError("");
    setLoading(true);
    try {
      await api.register({
        name: form.name,
        email: form.email,
        password: form.password,
        mobNumber: form.mobNumber,
        address: {
          addressLine1: form.addressLine1,
          addressLine2: form.addressLine2,
          area: form.area,
          city: form.city,
          state: form.state,
          pin: form.pin,
        },
      });
      showToast("A new OTP has been sent.", "info");
    } catch (err) {
      setOtpError(extractError(err));
    } finally {
      setLoading(false);
    }
  };

  if (step === 2) {
    return (
      <AuthShell
        title="Verify your email"
        subtitle={`Enter the 6-digit code we sent to ${form.email}.`}
        footer={
          <span>
            Entered the wrong details?{" "}
            <button className="tt-linklike" onClick={() => setStep(1)}>
              Go back
            </button>
          </span>
        }
      >
        {otpError && <div className="tt-form-error">{otpError}</div>}
        <Field label="One-Time Password">
          <input
            className="tt-input"
            value={otp}
            onChange={(e) => setOtp(e.target.value.replace(/\D/g, "").slice(0, 6))}
            placeholder="6-digit code"
            inputMode="numeric"
            onKeyDown={(e) => e.key === "Enter" && verifyOtp()}
          />
        </Field>
        <button className="tt-btn tt-btn-solid full" onClick={verifyOtp} disabled={loading}>
          {loading ? "Verifying…" : "Verify & create account"}
        </button>
        <button className="tt-btn tt-btn-ghost full" onClick={resendOtp} disabled={loading}>
          Resend OTP
        </button>
      </AuthShell>
    );
  }

  return (
    <AuthShell
      wide
      title="Create your account"
      subtitle="A few details and you're ready to track your tax year."
      footer={
        <span>
          Already registered? <Link to="/login">Log in</Link>
        </span>
      }
    >
      {errors.length > 0 && (
        <div className="tt-form-error">
          <ul>
            {errors.map((e, i) => (
              <li key={i}>{e}</li>
            ))}
          </ul>
        </div>
      )}
      <div className="tt-grid-2">
        <Field label="Full name">
          <input className="tt-input" value={form.name} onChange={set("name")} placeholder="First Middle Last" />
        </Field>
        <Field label="Email">
          <input className="tt-input" type="email" value={form.email} onChange={set("email")} placeholder="you@example.com" />
        </Field>
        <Field label="Password">
          <input className="tt-input" type="password" value={form.password} onChange={set("password")} placeholder="Strong password" />
        </Field>
        <Field label="Mobile number">
          <input className="tt-input" value={form.mobNumber} onChange={set("mobNumber")} placeholder="10 digits" />
        </Field>
      </div>

      <div className="tt-fieldset-label">Address</div>
      <div className="tt-grid-2">
        <Field label="Address line 1">
          <input className="tt-input" value={form.addressLine1} onChange={set("addressLine1")} placeholder="House / flat no." />
        </Field>
        <Field label="Address line 2">
          <input className="tt-input" value={form.addressLine2} onChange={set("addressLine2")} placeholder="Street, landmark" />
        </Field>
        <Field label="Area">
          <input className="tt-input" value={form.area} onChange={set("area")} placeholder="Area" />
        </Field>
        <Field label="City">
          <input className="tt-input" value={form.city} onChange={set("city")} placeholder="City" />
        </Field>
        <Field label="State">
          <select className="tt-input" value={form.state} onChange={set("state")}>
            {STATES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </Field>
        <Field label="PIN code">
          <input className="tt-input" value={form.pin} onChange={set("pin")} placeholder="6 digits" />
        </Field>
      </div>

      <button className="tt-btn tt-btn-solid full" onClick={requestOtp} disabled={loading}>
        {loading ? "Sending OTP…" : "Continue"}
      </button>
    </AuthShell>
  );
}
