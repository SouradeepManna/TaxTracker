import { useNavigate } from "react-router-dom";
import { LedgerHero } from "../components/LedgerHero";

export function Landing() {
  const navigate = useNavigate();

  const features = [
    {
      k: "01",
      title: "See the whole year",
      body: "Every TDS and TCS entry, sorted by month and financial year. Filter to the slice you need, page through the rest.",
    },
    {
      k: "02",
      title: "Take the numbers with you",
      body: "Download your transactions as JSON, PDF, or Excel — formatted to read cleanly, ready for your accountant.",
    },
    {
      k: "03",
      title: "File Form 90C, line by line",
      body: "Add transaction rows, save a draft to finish later, attach a supporting document, and submit when it's complete.",
    },
  ];

  const steps = [
    { n: "Register", d: "Create your account with your details and address." },
    { n: "Review", d: "Open your dashboard and read your year of transactions." },
    { n: "File", d: "Fill Form 90C, upload your document, and submit." },
  ];

  return (
    <div className="tt-landing">
      <section className="tt-hero">
        <div className="tt-hero-grid">
          <div className="tt-hero-copy">
            <p className="tt-eyebrow">For individual taxpayers in India</p>
            <h1 className="tt-display">
              Your year of tax,
              <br />
              <span className="tt-underline">kept in order.</span>
            </h1>
            <p className="tt-lede">
              TaxTracker turns a year of scattered TDS and TCS entries into a clean,
              readable ledger — then helps you file Form 90C without losing the thread.
            </p>
            <div className="tt-hero-cta">
              <button className="tt-btn tt-btn-solid lg" onClick={() => navigate("/register")}>
                Start tracking
              </button>
              <button className="tt-btn tt-btn-ghost lg" onClick={() => navigate("/login")}>
                I have an account
              </button>
            </div>
            <p className="tt-demo-note">
              Demo login — <code>prajwal@taxtracker.com</code> / <code>Password@1</code>
            </p>
          </div>
          <div className="tt-hero-visual">
            <LedgerHero />
          </div>
        </div>
      </section>

      <section className="tt-section" id="features">
        <h2 className="tt-section-title">What it does</h2>
        <div className="tt-feature-grid">
          {features.map((f) => (
            <article className="tt-feature" key={f.k}>
              <span className="tt-feature-num">{f.k}</span>
              <h3>{f.title}</h3>
              <p>{f.body}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="tt-section tt-steps-section">
        <h2 className="tt-section-title">Three steps, start to filed</h2>
        <div className="tt-steps">
          {steps.map((s, i) => (
            <div className="tt-step" key={i}>
              <div className="tt-step-rule" />
              <h4>{s.n}</h4>
              <p>{s.d}</p>
            </div>
          ))}
        </div>
        <div className="tt-cta-band">
          <div>
            <h3>Ready to put this year in order?</h3>
            <p>It takes a minute to register.</p>
          </div>
          <button className="tt-btn tt-btn-solid lg" onClick={() => navigate("/register")}>
            Get started
          </button>
        </div>
      </section>
    </div>
  );
}
