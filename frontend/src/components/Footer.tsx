export function Footer() {
  return (
    <footer className="tt-footer">
      <div className="tt-footer-inner">
        <div>
          <span className="tt-brand-mark small">₹</span> TaxTracker
        </div>
        <div className="tt-footer-meta">
          A capstone project · Built with Spring Boot &amp; React · {new Date().getFullYear()}
        </div>
      </div>
    </footer>
  );
}
