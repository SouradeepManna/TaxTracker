import { useCallback, useEffect, useState } from "react";
import { api, extractError } from "../api/client";
import { Field } from "../components/Field";
import type { InsightsResponse } from "../types";

const FY_OPTIONS = ["", "2022-2023", "2023-2024", "2024-2025"];

const inr = (n: number) =>
  "₹" + (n ?? 0).toLocaleString("en-IN", { maximumFractionDigits: 0 });

const MONTH_NAMES: Record<string, string> = {
  "01": "Jan", "02": "Feb", "03": "Mar", "04": "Apr", "05": "May", "06": "Jun",
  "07": "Jul", "08": "Aug", "09": "Sep", "10": "Oct", "11": "Nov", "12": "Dec", "00": "—",
};

export function Insights() {
  const [data, setData] = useState<InsightsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [financialYear, setFinancialYear] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const res = await api.getInsights(financialYear || undefined);
      setData(res.data);
    } catch (err) {
      setData(null);
      setError(extractError(err));
    } finally {
      setLoading(false);
    }
  }, [financialYear]);

  useEffect(() => {
    load();
  }, [load]);

  const maxTypeTax = data ? Math.max(1, ...data.taxByType.map((t) => t.taxAmount)) : 1;
  const maxOrgTax = data ? Math.max(1, ...data.topOrganizations.map((o) => o.taxAmount)) : 1;
  const maxMonth = data
    ? Math.max(1, ...data.monthlyTrend.map((m) => m.taxAmount), data.projectedNextMonthTax)
    : 1;

  return (
    <div className="tt-app-page">
      <div className="tt-page-head">
        <div>
          <span className="tt-page-sub">Smart analytics</span>
          <h1 className="tt-page-title">Tax insights</h1>
        </div>
        <div style={{ minWidth: 200 }}>
          <Field label="Financial year">
            <select
              className="tt-input"
              value={financialYear}
              onChange={(e) => setFinancialYear(e.target.value)}
            >
              {FY_OPTIONS.map((fy) => (
                <option key={fy || "all"} value={fy}>
                  {fy || "All years"}
                </option>
              ))}
            </select>
          </Field>
        </div>
      </div>

      {error && <div className="tt-form-error">{error}</div>}
      {loading && <div className="tt-loading">Crunching your numbers…</div>}

      {!loading && data && data.totalTransactions === 0 && (
        <div className="tt-empty">No transactions found for this period.</div>
      )}

      {!loading && data && data.totalTransactions > 0 && (
        <>
          <div className="tt-stat-grid">
            <div className="tt-stat-card">
              <div className="tt-stat-label">Transactions</div>
              <div className="tt-stat-value">{data.totalTransactions}</div>
            </div>
            <div className="tt-stat-card">
              <div className="tt-stat-label">Total amount</div>
              <div className="tt-stat-value">{inr(data.totalAmount)}</div>
            </div>
            <div className="tt-stat-card">
              <div className="tt-stat-label">Total tax</div>
              <div className="tt-stat-value">{inr(data.totalTax)}</div>
            </div>
            <div className="tt-stat-card">
              <div className="tt-stat-label">Projected next month</div>
              <div className="tt-stat-value">{inr(data.projectedNextMonthTax)}</div>
              <div className="tt-stat-sub">linear regression</div>
            </div>
          </div>

          <div className="tt-panel-grid">
            <div className="tt-panel">
              <h2 className="tt-panel-title">Tax by type (TDS vs TCS)</h2>
              {data.taxByType.map((t, i) => (
                <div className="tt-bar-row" key={t.type}>
                  <span className="tt-bar-label">{t.type}</span>
                  <span className="tt-bar-track">
                    <span
                      className={`tt-bar-fill ${i % 2 ? "alt" : ""}`}
                      style={{ width: `${(t.taxAmount / maxTypeTax) * 100}%` }}
                    />
                  </span>
                  <span className="tt-bar-value">{inr(t.taxAmount)}</span>
                </div>
              ))}
            </div>

            <div className="tt-panel">
              <h2 className="tt-panel-title">Top organizations</h2>
              {data.topOrganizations.map((o) => (
                <div className="tt-bar-row" key={o.organizationName}>
                  <span className="tt-bar-label" title={o.organizationName}>
                    {o.organizationName}
                  </span>
                  <span className="tt-bar-track">
                    <span
                      className="tt-bar-fill"
                      style={{ width: `${(o.taxAmount / maxOrgTax) * 100}%` }}
                    />
                  </span>
                  <span className="tt-bar-value">{inr(o.taxAmount)}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="tt-panel">
            <h2 className="tt-panel-title">Monthly tax trend</h2>
            <div className="tt-col-chart">
              {data.monthlyTrend.map((m) => (
                <div className="tt-col" key={m.month}>
                  <span
                    className="tt-col-bar"
                    style={{ height: `${(m.taxAmount / maxMonth) * 100}%` }}
                    title={inr(m.taxAmount)}
                  />
                  <span className="tt-col-label">{MONTH_NAMES[m.month] ?? m.month}</span>
                </div>
              ))}
              {/* projected next month column */}
              <div className="tt-col">
                <span
                  className="tt-col-bar projected"
                  style={{ height: `${(data.projectedNextMonthTax / maxMonth) * 100}%` }}
                  title={`Projected: ${inr(data.projectedNextMonthTax)}`}
                />
                <span className="tt-col-label">Next</span>
              </div>
            </div>
            <div className="tt-insight-callout">➡️ {data.trendNarrative}</div>
          </div>
        </>
      )}
    </div>
  );
}
