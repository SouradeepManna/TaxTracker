import { useEffect, useState } from "react";
import { api, extractError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { Field } from "../components/Field";
import type { Form90CRequest } from "../types";

interface Row {
  organizationName: string;
  amount: string;
  taxAmount: string;
  type: string;
}

const emptyRow: Row = { organizationName: "", amount: "", taxAmount: "", type: "TDS" };

export function Form90C() {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [name, setName] = useState(user?.name ?? "");
  const [mobileNumber, setMobileNumber] = useState("");
  const [financialYear, setFinancialYear] = useState("2024-2025");
  const [rows, setRows] = useState<Row[]>([{ ...emptyRow }]);
  const [formId, setFormId] = useState<number | null>(null);
  const [status, setStatus] = useState("");
  const [documentName, setDocumentName] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const res = await api.getMyForm90C();
        const f = res.data;
        setFormId(f.formId);
        setName(f.name || user?.name || "");
        setMobileNumber(f.mobileNumber || "");
        setFinancialYear(f.financialYear || "2024-2025");
        setStatus(f.status || "");
        setDocumentName(f.documentName || "");
        if (f.transactionHistory && f.transactionHistory.length) {
          setRows(
            f.transactionHistory.map((r) => ({
              organizationName: r.organizationName,
              amount: String(r.amount),
              taxAmount: String(r.taxAmount),
              type: r.type,
            }))
          );
        }
      } catch {
        /* no existing form yet — fine */
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const setRow = (i: number, k: keyof Row, v: string) => {
    const next = rows.slice();
    next[i] = { ...next[i], [k]: v };
    setRows(next);
  };
  const addRow = () => setRows([...rows, { ...emptyRow }]);
  const removeRow = (i: number) => setRows(rows.filter((_, idx) => idx !== i));

  const buildPayload = (): Form90CRequest => ({
    name,
    mobileNumber,
    financialYear,
    transactionHistory: rows
      .filter((r) => r.organizationName)
      .map((r) => ({
        organizationName: r.organizationName,
        amount: parseFloat(r.amount) || 0,
        taxAmount: parseFloat(r.taxAmount) || 0,
        type: r.type,
      })),
  });

  const saveDraft = async () => {
    setBusy(true);
    try {
      const res = await api.saveForm90C(buildPayload(), "DRAFT");
      setFormId(res.data.formId);
      setStatus(res.data.status);
      showToast("Draft saved — you can finish later.", "success");
    } catch (err) {
      showToast(extractError(err), "error");
    } finally {
      setBusy(false);
    }
  };

  const onUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
      showToast("File size exceeds the 2MB limit.", "error");
      return;
    }
    setBusy(true);
    try {
      if (!formId) {
        const res = await api.saveForm90C(buildPayload(), "DRAFT");
        setFormId(res.data.formId);
      }
      const up = await api.uploadDocument(file);
      setDocumentName(up.data.fileName);
      showToast("File uploaded successfully.", "success");
    } catch (err) {
      showToast(extractError(err), "error");
    } finally {
      setBusy(false);
    }
  };

  const submit = async () => {
    setBusy(true);
    try {
      const saved = await api.saveForm90C(buildPayload(), "SUBMITTED");
      const id = saved.data.formId;
      setFormId(id);
      const res = await api.submitForm(id);
      setStatus("SUBMITTED");
      showToast(res.data.message || "Form 90C submitted successfully", "success");
    } catch (err) {
      showToast(extractError(err), "error");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="tt-app-page">
      <div className="tt-page-head">
        <div>
          <p className="tt-eyebrow">Form 90C</p>
          <h1 className="tt-page-title">File your Form 90C</h1>
          <p className="tt-page-sub">
            Fill the details, add your transaction rows, attach a document, and submit.
          </p>
        </div>
        {status && <span className={`tt-status-badge ${status.toLowerCase()}`}>{status}</span>}
      </div>

      <div className="tt-card">
        <div className="tt-grid-2">
          <Field label="Name of the person">
            <input className="tt-input" value={name} onChange={(e) => setName(e.target.value)} />
          </Field>
          <Field label="Mobile number">
            <input className="tt-input" value={mobileNumber} onChange={(e) => setMobileNumber(e.target.value)} placeholder="10 digits" />
          </Field>
          <Field label="Financial year">
            <select className="tt-input" value={financialYear} onChange={(e) => setFinancialYear(e.target.value)}>
              <option value="2022-2023">2022-2023</option>
              <option value="2023-2024">2023-2024</option>
              <option value="2024-2025">2024-2025</option>
            </select>
          </Field>
        </div>

        <div className="tt-fieldset-label">Transaction history</div>
        <div className="tt-table-wrap">
          <table className="tt-table tt-form-table">
            <thead>
              <tr>
                <th>Organization</th>
                <th className="num">Amount</th>
                <th className="num">Tax amount</th>
                <th>Type</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r, i) => (
                <tr key={i}>
                  <td>
                    <input className="tt-input sm" value={r.organizationName} onChange={(e) => setRow(i, "organizationName", e.target.value)} placeholder="Organization name" />
                  </td>
                  <td>
                    <input className="tt-input sm num" value={r.amount} onChange={(e) => setRow(i, "amount", e.target.value)} placeholder="0.00" />
                  </td>
                  <td>
                    <input className="tt-input sm num" value={r.taxAmount} onChange={(e) => setRow(i, "taxAmount", e.target.value)} placeholder="0.00" />
                  </td>
                  <td>
                    <select className="tt-input sm" value={r.type} onChange={(e) => setRow(i, "type", e.target.value)}>
                      <option value="TDS">TDS</option>
                      <option value="TCS">TCS</option>
                    </select>
                  </td>
                  <td>
                    <button className="tt-row-del" onClick={() => removeRow(i)} disabled={rows.length === 1} aria-label="Remove row">
                      ×
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <button className="tt-btn tt-btn-ghost sm" onClick={addRow}>
          + Add row
        </button>

        <div className="tt-fieldset-label">Supporting document</div>
        <div className="tt-upload">
          <label className="tt-upload-drop">
            <input type="file" accept=".pdf,.jpg,.jpeg" onChange={onUpload} hidden />
            <span className="tt-upload-icon">⇪</span>
            <span>{documentName ? "Replace document" : "Upload PDF or JPG (max 2MB)"}</span>
          </label>
          {documentName && (
            <div className="tt-upload-name">
              Attached: <strong>{documentName}</strong>
            </div>
          )}
        </div>

        <div className="tt-form-actions">
          <button className="tt-btn tt-btn-ghost" onClick={saveDraft} disabled={busy}>
            {busy ? "Saving…" : "Save for later"}
          </button>
          <button className="tt-btn tt-btn-solid" onClick={submit} disabled={busy || status === "SUBMITTED"}>
            {status === "SUBMITTED" ? "Submitted ✓" : "Submit Form 90C"}
          </button>
        </div>
      </div>
    </div>
  );
}
