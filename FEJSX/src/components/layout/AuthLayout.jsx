import ThemeToggle from '../ThemeToggle'

export default function AuthLayout({ title, subtitle, children, footer }) {
  return (
    <div className="auth-shell">
      <div className="auth-backdrop" aria-hidden="true" />
      <div className="auth-theme">
        <ThemeToggle />
      </div>
      <div className="auth-card">
        <div className="brand">
          <span className="brand-mark">N</span>
          <span className="brand-name">Nexa Bank</span>
        </div>
        <h1>{title}</h1>
        {subtitle && <p className="auth-subtitle">{subtitle}</p>}
        {children}
        {footer && <div className="auth-footer">{footer}</div>}
      </div>
    </div>
  )
}
