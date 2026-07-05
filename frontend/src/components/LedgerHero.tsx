import { useEffect, useState } from "react";

interface Row {
  org: string;
  type: "TDS" | "TCS";
  amount: number;
  tax: number;
}

const rows: Row[] = [
  { org: "Infosys Limited", type: "TDS", amount: 184250, tax: 4231 },
  { org: "HDFC Bank", type: "TCS", amount: 92000, tax: 1104 },
  { org: "Reliance Industries", type: "TDS", amount: 215600, tax: 6468 },
  { org: "Tata Motors", type: "TCS", amount: 73400, tax: 880 },
  { org: "Axis Bank", type: "TDS", amount: 128900, tax: 3222 },
];

// A "ledger that tallies itself" — rows stamp in and a running total counts up.
export function LedgerHero() {
  const [visibleCount, setVisibleCount] = useState(0);
  const [total, setTotal] = useState(0);

  useEffect(() => {
    let i = 0;
    const interval = setInterval(() => {
      i += 1;
      setVisibleCount(i);
      if (i >= rows.length) clearInterval(interval);
    }, 650);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const target = rows.slice(0, visibleCount).reduce((s, r) => s + r.tax, 0);
    let current = total;
    const step = Math.max(1, Math.round((target - current) / 18));
    const t = setInterval(() => {
      current += step;
      if (current >= target) {
        current = target;
        clearInterval(t);
      }
      setTotal(current);
    }, 20);
    return () => clearInterval(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visibleCount]);

  const fmt = (n: number) => "₹" + n.toLocaleString("en-IN");

  return (
    <div className="tt-ledger" aria-hidden="true">
      <div className="tt-ledger-head">
        <span>Organization</span>
        <span>Type</span>
        <span className="num">Amount</span>
        <span className="num">Tax</span>
      </div>
      <div className="tt-ledger-body">
        {rows.map((r, idx) => (
          <div key={idx} className={`tt-ledger-row ${idx < visibleCount ? "in" : ""}`}>
            <span>{r.org}</span>
            <span>
              <em className={`tt-chip ${r.type.toLowerCase()}`}>{r.type}</em>
            </span>
            <span className="num">{fmt(r.amount)}</span>
            <span className="num">{fmt(r.tax)}</span>
          </div>
        ))}
      </div>
      <div className="tt-ledger-total">
        <span>Tax tallied this year</span>
        <span className="num total">{fmt(total)}</span>
      </div>
    </div>
  );
}
