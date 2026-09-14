import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/layout/AuthLayout'
import Alert from '../components/Alert'
import { api, ApiError } from '../api/client'
import { useToast } from '../context/contexts'

export default function RegisterPage() {
  const navigate = useNavigate()
  const toast = useToast()
  const [form, setForm] = useState({ nome: '', cognome: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }))

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.register(form)
      toast.success('Registrazione completata: controlla la tua email')
      navigate(`/verifica?email=${encodeURIComponent(form.email)}`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Errore di connessione al server')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      title="Crea il tuo conto"
      subtitle="Ti invieremo un'email per attivare l'account"
      footer={
        <p>
          Hai già un account? <Link to="/login">Accedi</Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit} className="form">
        <Alert>{error}</Alert>
        <div className="field-row">
          <div className="field">
            <label htmlFor="nome">Nome</label>
            <input id="nome" value={form.nome} onChange={update('nome')} required autoComplete="given-name" />
          </div>
          <div className="field">
            <label htmlFor="cognome">Cognome</label>
            <input id="cognome" value={form.cognome} onChange={update('cognome')} required autoComplete="family-name" />
          </div>
        </div>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input id="email" type="email" value={form.email} onChange={update('email')} required autoComplete="email" />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input id="password" type="password" value={form.password} onChange={update('password')}
            required minLength={8} autoComplete="new-password" />
          <span className="field-hint">Almeno 8 caratteri</span>
        </div>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Creazione in corso…' : 'Registrati'}
        </button>
      </form>
    </AuthLayout>
  )
}
