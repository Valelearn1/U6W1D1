import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/layout/AuthLayout'
import Alert from '../components/Alert'
import { api, ApiError } from '../api/client'
import { useAuth, useToast } from '../context/contexts'

export default function LoginPage() {
  const { login } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const destinazione = location.state?.from || '/'

  const [mode, setMode] = useState('password')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [codice, setCodice] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function switchMode(next) {
    setMode(next)
    setError('')
  }

  async function handle(fn) {
    setError('')
    setLoading(true)
    try {
      await fn()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Errore di connessione al server')
    } finally {
      setLoading(false)
    }
  }

  const submitPassword = (e) => {
    e.preventDefault()
    handle(async () => {
      await login(await api.loginPassword({ email, password }))
      toast.success('Bentornata!')
      navigate(destinazione, { replace: true })
    })
  }

  const submitRequestCode = (e) => {
    e.preventDefault()
    handle(async () => {
      await api.requestLoginCode({ email })
      toast.info('Se l’indirizzo è registrato, riceverai un codice via email')
      setMode('code-verify')
    })
  }

  const submitCode = (e) => {
    e.preventDefault()
    handle(async () => {
      await login(await api.loginCode({ email, codice }))
      toast.success('Bentornata!')
      navigate(destinazione, { replace: true })
    })
  }

  return (
    <AuthLayout
      title="Bentornata"
      subtitle="Accedi al tuo conto Nexa Bank"
      footer={
        <p>
          Non hai un account? <Link to="/registrati">Registrati</Link>
        </p>
      }
    >
      <div className="tabs">
        <button
          type="button"
          className={mode === 'password' ? 'tab active' : 'tab'}
          onClick={() => switchMode('password')}
        >
          Password
        </button>
        <button
          type="button"
          className={mode !== 'password' ? 'tab active' : 'tab'}
          onClick={() => switchMode('code-request')}
        >
          Codice via email
        </button>
      </div>

      {mode === 'password' && (
        <form onSubmit={submitPassword} className="form">
          <Alert>{error}</Alert>
          <div className="field">
            <label htmlFor="login-email">Email</label>
            <input id="login-email" type="email" value={email} required autoComplete="email"
              onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="login-password">Password</label>
            <input id="login-password" type="password" value={password} required autoComplete="current-password"
              onChange={(e) => setPassword(e.target.value)} />
          </div>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Accesso in corso…' : 'Accedi'}
          </button>
        </form>
      )}

      {mode === 'code-request' && (
        <form onSubmit={submitRequestCode} className="form">
          <Alert>{error}</Alert>
          <div className="field">
            <label htmlFor="code-email">Email</label>
            <input id="code-email" type="email" value={email} required autoComplete="email"
              onChange={(e) => setEmail(e.target.value)} />
          </div>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Invio in corso…' : 'Invia codice via email'}
          </button>
        </form>
      )}

      {mode === 'code-verify' && (
        <form onSubmit={submitCode} className="form">
          <Alert>{error}</Alert>
          <div className="field">
            <label htmlFor="code-verify">Codice ricevuto</label>
            <input id="code-verify" value={codice} required inputMode="numeric" maxLength={6}
              placeholder="123456" className="mono" onChange={(e) => setCodice(e.target.value)} />
          </div>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Verifica in corso…' : 'Accedi'}
          </button>
          <button type="button" className="link-btn" onClick={() => switchMode('code-request')}>
            Richiedi un altro codice
          </button>
        </form>
      )}
    </AuthLayout>
  )
}
