import { useState } from 'react'
import { Navigate, useLocation, useNavigate, useParams } from 'react-router-dom'
import Alert from '../components/Alert'
import { api, ApiError } from '../api/client'
import { useAuth, useAccount, useToast } from '../context/contexts'
import { formatCurrency, formatIban } from '../utils/format'

export default function ConfirmTransferPage() {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const { token, refreshUser } = useAuth()
  const { refresh } = useAccount()
  const toast = useToast()

  const transaction = location.state?.transaction
  const [codice, setCodice] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Se si arriva qui con un link diretto non abbiamo i dati: torniamo al bonifico
  if (!transaction) return <Navigate to="/bonifico" replace />

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.executeTransaction({ transactionId: id, transactionToken: codice }, token)
      toast.success(`Bonifico di ${formatCurrency(transaction.importo)} completato`)
      await Promise.all([refreshUser(), refresh()])
      navigate('/', { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Errore di connessione al server')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page page-narrow">
      <div className="panel">
        <h2>Conferma il bonifico</h2>
        <div className="tx-summary">
          <div>
            <span className="tx-summary-label">Destinatario</span>
            <span className="mono">{formatIban(transaction.ibanDestinatario)}</span>
          </div>
          <div>
            <span className="tx-summary-label">Importo</span>
            <span className="tx-summary-amount">{formatCurrency(transaction.importo)}</span>
          </div>
        </div>
        <p className="panel-subtitle">
          Abbiamo inviato un codice a 6 cifre alla tua email. Scade tra 10 minuti e hai 5 tentativi.
        </p>
        <form onSubmit={handleSubmit} className="form">
          <Alert>{error}</Alert>
          <div className="field">
            <label htmlFor="tx-code">Codice di conferma</label>
            <input
              id="tx-code" value={codice} required inputMode="numeric" maxLength={6}
              placeholder="123456" className="mono code-input"
              onChange={(e) => setCodice(e.target.value.replace(/\D/g, ''))}
            />
          </div>
          <div className="form-actions">
            <button type="button" className="btn-ghost" onClick={() => navigate('/')}>
              Annulla
            </button>
            <button type="submit" className="btn-primary" disabled={loading || codice.length !== 6}>
              {loading ? 'Conferma in corso…' : 'Conferma bonifico'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
