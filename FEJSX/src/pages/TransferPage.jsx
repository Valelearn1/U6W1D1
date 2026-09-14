import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Alert from '../components/Alert'
import { api, ApiError } from '../api/client'
import { useAuth, useAccount, useToast } from '../context/contexts'
import { CATEGORIES, formatCurrency, formatIban } from '../utils/format'
import { formatIbanInput, ibanError, normalizeIban } from '../utils/iban'

export default function TransferPage() {
  const { user, token } = useAuth()
  const { beneficiaries } = useAccount()
  const toast = useToast()
  const navigate = useNavigate()

  const [iban, setIban] = useState('')
  const [importo, setImporto] = useState('')
  const [categoria, setCategoria] = useState('ALTRO')
  const [descrizione, setDescrizione] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // La validazione dell'IBAN avviene mentre si scrive, prima di chiamare il server
  const erroreIban = ibanError(iban)
  const ibanCompleto = normalizeIban(iban).length === 28 && !erroreIban
  const saldoSufficiente = !importo || Number(importo) <= Number(user.saldo)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const tx = await api.newTransaction(
        {
          ibanDestinatario: normalizeIban(iban),
          importo: Number(importo),
          categoria,
          descrizione: descrizione.trim() || null,
        },
        token,
      )
      toast.info('Ti abbiamo inviato un codice di conferma via email')
      navigate(`/bonifico/conferma/${tx.id}`, { state: { transaction: tx } })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Errore di connessione al server')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page page-narrow">
      <div className="panel">
        <h2>Nuovo bonifico</h2>
        <p className="panel-subtitle">
          Dal tuo conto <span className="mono">{formatIban(user.iban)}</span> · disponibili{' '}
          <strong>{formatCurrency(user.saldo)}</strong>
        </p>

        {beneficiaries.length > 0 && (
          <div className="beneficiary-picker">
            <span className="picker-label">Dalla rubrica</span>
            <div className="chip-row">
              {beneficiaries.map((b) => (
                <button
                  key={b.id}
                  type="button"
                  className={normalizeIban(iban) === b.iban ? 'chip chip-active' : 'chip'}
                  onClick={() => setIban(formatIbanInput(b.iban))}
                >
                  {b.nome}
                </button>
              ))}
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="form">
          <Alert>{error}</Alert>

          <div className="field">
            <label htmlFor="iban-dest">IBAN destinatario</label>
            <input
              id="iban-dest"
              value={iban}
              onChange={(e) => setIban(formatIbanInput(e.target.value))}
              required
              placeholder="IT00 0000 0000 0000 0000 0000 0000"
              className={`mono ${erroreIban ? 'input-error' : ibanCompleto ? 'input-ok' : ''}`}
              aria-invalid={Boolean(erroreIban)}
              aria-describedby="iban-hint"
            />
            <span id="iban-hint" className={erroreIban ? 'field-error' : 'field-hint'}>
              {erroreIban || (ibanCompleto ? '✓ Formato corretto' : 'IT seguito da 26 cifre')}
            </span>
          </div>

          <div className="field">
            <label htmlFor="importo">Importo (€)</label>
            <input
              id="importo" type="number" min="0.01" step="0.01" required
              value={importo} onChange={(e) => setImporto(e.target.value)}
              className={!saldoSufficiente ? 'input-error' : ''}
            />
            {!saldoSufficiente && <span className="field-error">Saldo insufficiente</span>}
          </div>

          <div className="field">
            <label htmlFor="categoria">Categoria</label>
            <select id="categoria" value={categoria} onChange={(e) => setCategoria(e.target.value)}>
              {CATEGORIES.filter((c) => c.value !== 'RICARICA').map((c) => (
                <option key={c.value} value={c.value}>
                  {c.icon} {c.label}
                </option>
              ))}
            </select>
          </div>

          <div className="field">
            <label htmlFor="descrizione">Causale (facoltativa)</label>
            <input
              id="descrizione" value={descrizione} maxLength={140}
              onChange={(e) => setDescrizione(e.target.value)}
              placeholder="es. Affitto settembre"
            />
          </div>

          <div className="form-actions">
            <button type="button" className="btn-ghost" onClick={() => navigate('/')}>
              Annulla
            </button>
            <button type="submit" className="btn-primary" disabled={loading || !ibanCompleto || !saldoSufficiente}>
              {loading ? 'Invio in corso…' : 'Continua'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
