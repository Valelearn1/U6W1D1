import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import AuthLayout from '../components/layout/AuthLayout'
import Alert from '../components/Alert'
import { api, ApiError } from '../api/client'
import { useToast } from '../context/contexts'

/**
 * Il link nella mail di registrazione punta a /verifica?email=...&codice=...
 * Con il router i parametri si leggono direttamente dall'URL.
 */
export default function VerifyPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const toast = useToast()

  const [email, setEmail] = useState(params.get('email') || '')
  const [codice, setCodice] = useState(params.get('codice') || '')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.verify({ email, codice })
      toast.success('Account attivato: ora puoi accedere')
      navigate('/login', { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Errore di connessione al server')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      title="Verifica il tuo account"
      subtitle="Inserisci il codice ricevuto via email, oppure apri il link dalla mail"
      footer={
        <p>
          Già verificato? <Link to="/login">Accedi</Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit} className="form">
        <Alert>{error}</Alert>
        <div className="field">
          <label htmlFor="verify-email">Email</label>
          <input id="verify-email" type="email" value={email} required autoComplete="email"
            onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="verify-code">Codice di verifica</label>
          <input id="verify-code" value={codice} required className="mono"
            onChange={(e) => setCodice(e.target.value)} />
        </div>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Verifica in corso…' : 'Attiva account'}
        </button>
      </form>
    </AuthLayout>
  )
}
