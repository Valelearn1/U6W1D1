import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/contexts'

/** Blocca l'accesso alle pagine interne se non si e' autenticati. */
export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="splash">
        <div className="spinner" />
      </div>
    )
  }

  if (!user) {
    // "state" ricorda dove voleva andare, per tornarci dopo il login
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return children
}
