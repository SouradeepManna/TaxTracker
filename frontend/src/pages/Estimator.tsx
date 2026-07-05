import { useMemo, useState } from "react";
import { Field } from "../components/Field";

const inr = (n: number) =>
  "₹" + Math.round(n).toLocaleString("en-IN", { maximumFractionDigits: 0 });

/**
 * Income tax estimate for FY 2024-25 (AY 2025-26), individual < 60 yrs.
 *
 * New regime slabs (default):
 *   0–3L: nil, 3–7L: 5%, 7–10L: 10%, 10–12L: 15%, 12–15L: 20%, >15L: 30%
 *   Standard deduction ₹75,000 (salaried). 87A rebate if taxable ≤ 7L.
 * Old regime slabs:
 *   0–2.5L: nil, 2.5–5L: 5%, 5–10L: 20%, >10L: 30%
 *   Standard deduction ₹50,000 + user deductions (80C etc). 87A rebate if taxable ≤ 5L.
 * Health & education cess: 4% on tax.
 */
function slabTax(income: number, slabs: { upto: number; rate: number }[]): number {
  let tax = 0;
  let lower = 0;
  for (const s of slabs) {
    if (income > s.upto) {
      tax += (s.upto - lower) * s.rate;
      lower = s.upto;
    } else {
      tax += (income - lower) * s.rate;
      return tax;
    }
  }
  tax += (income - lower) * slabs[slabs.length - 1].rate;
  return tax;
}

const NEW_SLABS = [
  { upto: 300000, rate: 0 },
  { upto: 700000, rate: 0.05 },
  { upto: 1000000, rate: 0.1 },
  { upto: 1200000, rate: 0.15 },
  { upto: 1500000, rate: 0.2 },
  { upto: Infinity, rate: 0.3 },
];

const OLD_SLABS = [
  { upto: 250000, rate: 0 },
  { upto: 500000, rate: 0.05 },
  { upto: 1000000, rate: 0.2 },
  { upto: Infinity, rate: 0.3 },
];

function computeNew(gross: number) {
  const std = 75000;
  const taxable = Math.max(0, gross - std);
  let tax = slabTax(taxable, NEW_SLABS);
  if (taxable <= 700000) tax = 0; // 87A rebate
  const cess = tax * 0.04;
  return { taxable, tax, cess, total: tax + cess, std, deductions: 0 };
}

function computeOld(gross: number, deductions: number) {
  const std = 50000;
  const taxable = Math.max(0, gross - std - deductions);
  let tax = slabTax(taxable, OLD_SLABS);
  if (taxable <= 500000) tax = 0; // 87A rebate
  const cess = tax * 0.04;
  return { taxable, tax, cess, total: tax + cess, std, deductions };
}

export function Estimator() {
  const [income, setIncome] = useState("1200000");
  const [deductions, setDeductions] = useState("150000");

  const gross = Number(income) || 0;
  const ded = Number(deductions) || 0;

  const newR = useMemo(() => computeNew(gross), [gross]);
  const oldR = useMemo(() => computeOld(gross, ded), [gross, ded]);

  const newWins = newR.total <= oldR.total;
  const savings = Math.abs(newR.total - oldR.total);

  return (
    <div className="tt-app-page">
      <div className="tt-page-head">
        <div>
          <span className="tt-page-sub">Planning tool</span>
          <h1 className="tt-page-title">Tax estimator</h1>
        </div>
      </div>
      <p className="tt-page-intro">
        Estimate your income tax for FY 2024-25 and see which regime saves you more.
      </p>

      <div className="tt-panel">
        <div className="tt-grid-2">
          <Field label="Gross annual income (₹)">
            <input
              className="tt-input"
              value={income}
              onChange={(e) => setIncome(e.target.value.replace(/[^\d]/g, ""))}
              inputMode="numeric"
              placeholder="e.g. 1200000"
            />
          </Field>
          <Field label="Deductions for old regime (80C, 80D, HRA…) (₹)">
            <input
              className="tt-input"
              value={deductions}
              onChange={(e) => setDeductions(e.target.value.replace(/[^\d]/g, ""))}
              inputMode="numeric"
              placeholder="e.g. 150000"
            />
          </Field>
        </div>

        <div className="tt-regime-grid">
          <div className={`tt-regime-card ${newWins ? "winner" : ""}`}>
            <div className="tt-regime-name">New regime</div>
            <div className="tt-regime-tax">{inr(newR.total)}</div>
            <div className="tt-regime-line"><span>Standard deduction</span><span>{inr(newR.std)}</span></div>
            <div className="tt-regime-line"><span>Taxable income</span><span>{inr(newR.taxable)}</span></div>
            <div className="tt-regime-line"><span>Tax before cess</span><span>{inr(newR.tax)}</span></div>
            <div className="tt-regime-line"><span>Health &amp; edu cess (4%)</span><span>{inr(newR.cess)}</span></div>
            {newWins && <span className="tt-winner-pill">Saves you more</span>}
          </div>

          <div className={`tt-regime-card ${!newWins ? "winner" : ""}`}>
            <div className="tt-regime-name">Old regime</div>
            <div className="tt-regime-tax">{inr(oldR.total)}</div>
            <div className="tt-regime-line"><span>Standard deduction</span><span>{inr(oldR.std)}</span></div>
            <div className="tt-regime-line"><span>Your deductions</span><span>{inr(oldR.deductions)}</span></div>
            <div className="tt-regime-line"><span>Taxable income</span><span>{inr(oldR.taxable)}</span></div>
            <div className="tt-regime-line"><span>Tax before cess</span><span>{inr(oldR.tax)}</span></div>
            <div className="tt-regime-line"><span>Health &amp; edu cess (4%)</span><span>{inr(oldR.cess)}</span></div>
            {!newWins && <span className="tt-winner-pill">Saves you more</span>}
          </div>
        </div>

        <div className="tt-insight-callout">
          The <strong>{newWins ? "new" : "old"} regime</strong> saves you{" "}
          <strong>{inr(savings)}</strong> for this income.
          {savings === 0 && " Both regimes result in the same tax here."}
        </div>
        <p className="tt-fineprint">
          Estimates use FY 2024-25 slabs for individuals under 60, including the ₹75,000 (new) /
          ₹50,000 (old) standard deduction and the Section 87A rebate. Actual liability may vary —
          consult a tax professional before filing.
        </p>
      </div>
    </div>
  );
}
