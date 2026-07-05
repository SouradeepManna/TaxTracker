import type { ReactNode } from "react";

export function Field({
  label,
  children,
  inline,
}: {
  label: string;
  children: ReactNode;
  inline?: boolean;
}) {
  return (
    <label className={`tt-field ${inline ? "inline" : ""}`}>
      <span className="tt-field-label">{label}</span>
      {children}
    </label>
  );
}

export function AuthShell({
  title,
  subtitle,
  children,
  footer,
  wide,
}: {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer: ReactNode;
  wide?: boolean;
}) {
  return (
    <div className="tt-auth">
      <div className={`tt-auth-card ${wide ? "wide" : ""}`}>
        <div className="tt-auth-mark">₹</div>
        <h1 className="tt-auth-title">{title}</h1>
        <p className="tt-auth-sub">{subtitle}</p>
        {children}
        <div className="tt-auth-footer">{footer}</div>
      </div>
    </div>
  );
}
