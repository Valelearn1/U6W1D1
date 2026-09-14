export default function Alert({ variant = 'error', children }) {
  if (!children) return null
  return <div className={`alert alert-${variant}`}>{children}</div>
}
