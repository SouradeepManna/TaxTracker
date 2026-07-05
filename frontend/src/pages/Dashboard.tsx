import { useCallback, useEffect, useState } from "react";
import { api, extractError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { Field } from "../components/Field";
import type {
  CreateTransactionRequest,
  InsightsResponse,
  TransactionPageResponse,
  TransactionQuery,
} from "../types";

const MONTHS = ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"];
const TYPES = ["TDS", "TCS"];

const emptyTxnForm: CreateTransactionRequest = {
  date: "",
  amount: 0,
  taxAmount: 0,
  type: "TDS",
  organizationName: "",
};

export function Dashboard() {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [data, setData] = useState<TransactionPageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [page, setPage] = useState(1);
  const pageSize = 10;
  const [financialYear, setFinancialYear] = useState("");
  const [month, setMonth] = useState("");
  const [organizationName, setOrganizationName] = useState("");
  const [type, setType] = useState("");
  const [summary, setSummary] = useState<InsightsResponse | null>(null);

  // Add-transaction form
  const [showAdd, setShowAdd] = useState(false);
  const [txnForm, setTxnForm] = useState<CreateTransactionRequest>(emptyTxnForm);
  const [saving, setSaving] = useState(false);
  const [addError, setAddError] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const params: TransactionQuery = { pageNumber: page, pageSize };
      if (financialYear) params.financialYear = financialYear;
      if (month) params.month = month;
      if (organizationName) params.organizationName = organizationName;
      if (type) params.type = type;
      const res = await api.getTransactions(params);
      setData(res.data);
    } catch (err) {
      setData(null);
      setError(extractError(err));
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, financialYear, month, organizationName, type]);

  useEffect(() => {
    load();
  }, [page]); // eslint-disable-line react-hooks/exhaustive-deps

  // Pull aggregate figures (cards + chart) for the selected FY.
  useEffect(() => {
    let active = true;
    api
      .getInsights(financialYear || undefined)
      .then((res) => {
        if (active) setSummary(res.data);
      })
      .catch(() => {
        if (active) setSummary(null);
      });
    return () => {
      active = false;
    };
  }, [financialYear]);

  const applyFilters = () => {
    setPage(1);
    load();
  };

  const clearFilters = () => {
    setFinancialYear("");
    setMonth("");
    setOrganizationName("");
    setType("");
    setPage(1);
  };

  const setTxn =
    (k: keyof CreateTransactionRequest) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setTxnForm({ ...txnForm, [k]: e.target.value });

  const submitTransaction = async () => {
    setAddError("");
    // Basic client-side validation
    if (!txnForm.date) return setAddError("Please pick a date.");
    if (!txnForm.organizationName.trim()) return setAddError("Organisation is required.");
    if (Number(txnForm.amount) < 0 || Number(txnForm.taxAmount) < 0)
      return setAddError("Amount and tax must be zero or positive.");

    setSaving(true);
    try {
      await api.createTransaction({
        date: txnForm.date,
        amount: Number(txnForm.amount),
        taxAmount: Number(txnForm.taxAmount),
        type: txnForm.type,
        organizationName: txnForm.organizationName.trim(),
      });
      showToast("Transaction added.", "success");
      setTxnForm(emptyTxnForm);
      setShowAdd(false);
      setPage(1);
      load();
    } catch (err) {
      setAddError(extractError(err));
    } finally {
      setSaving(false);
    }
  };

  const download = async (format: string) => {
    try {
      const res = await api.downloadTransactions(format, financialYear || undefined);
      const blob = new Blob([res.data as BlobPart]);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      const ext = format === "excel" ? "xlsx" : format;
      a.download = `transactions.${ext}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      showToast(`Download started: ${a.download}`, "success");
    } catch (err) {
      showToast(extractError(err), "error");
    }
  };

  const fmt = (n: number) =>
    "₹" + Number(n).toLocaleString("en-IN", { minimumFractionDigits: 2 });
  const totalPages = data ? Math.max(1, Math.ceil(data.totalRecords / data.pageSize)) : 1;

  return (
    <div className="tt-app-page">
      <div className="tt-page-head">
        <div>
          <p className="tt-eyebrow">Dashboard</p>
          <h1 className="tt-page-title">Hello, {user?.name?.split(" ")[0]}</h1>
          <p className="tt-page-sub">Your yearly transactions, tallied and sorted.</p>
        </div>
        <div className="tt-download-group">
          <button className="tt-btn tt-btn-solid sm" onClick={() => setShowAdd((v) => !v)}>
            {showAdd ? "Close" : "+ Add transaction"}
          </button>
          <span className="tt-download-label">Download</span>
          <button className="tt-btn tt-btn-ghost sm" onClick={() => download("json")}>
            JSON
          </button>
          <button className="tt-btn tt-btn-ghost sm" onClick={() => download("pdf")}>
            PDF
          </button>
          <button className="tt-btn tt-btn-ghost sm" onClick={() => download("excel")}>
            Excel
          </button>
        </div>
      </div>

      {showAdd && (
        <div className="tt-panel">
          <h2 className="tt-panel-title">Add a transaction</h2>
          {addError && <div className="tt-form-error">{addError}</div>}
          <div className="tt-grid-2">
            <Field label="Date">
              <input
                className="tt-input"
                type="date"
                value={txnForm.date}
                onChange={setTxn("date")}
              />
            </Field>
            <Field label="Organisation">
              <input
                className="tt-input"
                value={txnForm.organizationName}
                onChange={setTxn("organizationName")}
                placeholder="e.g. Infosys Limited"
              />
            </Field>
            <Field label="Type">
              <select className="tt-input" value={txnForm.type} onChange={setTxn("type")}>
                {TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
            </Field>
            <Field label="Amount (₹)">
              <input
                className="tt-input"
                type="number"
                min="0"
                step="0.01"
                value={txnForm.amount}
                onChange={setTxn("amount")}
              />
            </Field>
            <Field label="Tax amount (₹)">
              <input
                className="tt-input"
                type="number"
                min="0"
                step="0.01"
                value={txnForm.taxAmount}
                onChange={setTxn("taxAmount")}
              />
            </Field>
          </div>
          <button className="tt-btn tt-btn-solid" onClick={submitTransaction} disabled={saving}>
            {saving ? "Saving…" : "Save transaction"}
          </button>
        </div>
      )}

      {summary && summary.totalTransactions > 0 && (
        <>
          <div className="tt-stat-grid">
            <div className="tt-stat-card">
              <div className="tt-stat-label">Transactions</div>
              <div className="tt-stat-value">{summary.totalTransactions}</div>
              <div className="tt-stat-sub">{financialYear || "all years"}</div>
            </div>
            <div className="tt-stat-card">
              <div className="tt-stat-label">Total amount</div>
              <div className="tt-stat-value">
                {"₹" + Math.round(summary.totalAmount).toLocaleString("en-IN")}
              </div>
            </div>
            <div className="tt-stat-card">
              <div className="tt-stat-label">Total tax paid</div>
              <div className="tt-stat-value">
                {"₹" + Math.round(summary.totalTax).toLocaleString("en-IN")}
              </div>
            </div>
            <div className="tt-stat-card">
              <div className="tt-stat-label">Avg tax / txn</div>
              <div className="tt-stat-value">
                {"₹" +
                  Math.round(
                    summary.totalTransactions ? summary.totalTax / summary.totalTransactions : 0
                  ).toLocaleString("en-IN")}
              </div>
            </div>
          </div>

          {summary.monthlyTrend.length > 0 && (
            <div className="tt-panel">
              <h2 className="tt-panel-title">Monthly tax</h2>
              <div className="tt-col-chart">
                {summary.monthlyTrend.map((m) => {
                  const max = Math.max(1, ...summary.monthlyTrend.map((x) => x.taxAmount));
                  return (
                    <div className="tt-col" key={m.month}>
                      <span
                        className="tt-col-bar"
                        style={{ height: `${(m.taxAmount / max) * 100}%` }}
                        title={"₹" + Math.round(m.taxAmount).toLocaleString("en-IN")}
                      />
                      <span className="tt-col-label">{m.month}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </>
      )}

      <div className="tt-filters">
        <Field label="Financial year" inline>
          <select className="tt-input" value={financialYear} onChange={(e) => setFinancialYear(e.target.value)}>
            <option value="">All years</option>
            <option value="2022-2023">2022-2023</option>
            <option value="2023-2024">2023-2024</option>
            <option value="2024-2025">2024-2025</option>
          </select>
        </Field>
        <Field label="Month" inline>
          <select className="tt-input" value={month} onChange={(e) => setMonth(e.target.value)}>
            <option value="">All months</option>
            {MONTHS.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Organisation" inline>
          <input
            className="tt-input"
            value={organizationName}
            onChange={(e) => setOrganizationName(e.target.value)}
            placeholder="Any organisation"
          />
        </Field>
        <Field label="Type" inline>
          <select className="tt-input" value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">All types</option>
            {TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </Field>
        <button className="tt-btn tt-btn-solid sm" onClick={applyFilters}>
          Apply
        </button>
        <button className="tt-btn tt-btn-ghost sm" onClick={clearFilters}>
          Clear
        </button>
      </div>

      {loading && <div className="tt-empty">Loading transactions…</div>}
      {!loading && error && <div className="tt-empty tt-empty-error">{error}</div>}

      {!loading && !error && data && (
        <>
          <div className="tt-table-wrap">
            <table className="tt-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Organization</th>
                  <th>Type</th>
                  <th className="num">Amount</th>
                  <th className="num">Tax</th>
                  <th>FY</th>
                </tr>
              </thead>
              <tbody>
                {data.transactions.map((t) => (
                  <tr key={t.id}>
                    <td className="mono">{t.date}</td>
                    <td>{t.organizationName}</td>
                    <td>
                      <em className={`tt-chip ${(t.type || "").toLowerCase()}`}>{t.type}</em>
                    </td>
                    <td className="num mono">{fmt(t.amount)}</td>
                    <td className="num mono">{fmt(t.taxAmount)}</td>
                    <td className="mono">{t.financialYear}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="tt-pager">
            <span>
              {data.totalRecords} records · page {data.pageNumber} of {totalPages}
            </span>
            <div>
              <button className="tt-btn tt-btn-ghost sm" disabled={page <= 1} onClick={() => setPage(page - 1)}>
                ← Prev
              </button>
              <button
                className="tt-btn tt-btn-ghost sm"
                disabled={page >= totalPages}
                onClick={() => setPage(page + 1)}
              >
                Next →
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
