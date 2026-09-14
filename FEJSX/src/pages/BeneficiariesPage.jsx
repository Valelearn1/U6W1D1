import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Alert from '../components/Alert'
import { SkeletonRows } from '../components/Skeleton'
import { api, ApiError } from '../api/client'
import { useAuth, useAccount, useToast } from '../context/contexts'
import { formatIban } from '../utils/format'
import { formatIbanInput, ibanError, normalizeIban } from '../utils/iban'

export default function BeneficiariesPage() {
  const { token } = useAuth()
  const { beneficiaries, loading, refresh } = useAccount()
  const toast = useToast()
  const navigate = useNavigate()

  const [nome, setNome] = useState('')
  const [iban, setIban] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  const erroreIban = ibanError(iban)
  const ibanCompleto = normalizeIban(iban).length === 28 && !erroreIban

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      await api.addBeneficiary({ nome, iban: normalizeIban(iban) }, token)
      toast.success(`${nome} aggiunto alla rubrica`)
      setNome('')
      setIban('')
      await refresh()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Errore di connessione al server')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(b) {
    try {
      await api.deleteBeneficiary(b.id, token)
      toast.info(`${b.nome} rimosso dalla rubrica`)
      await refresh()
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Errore di connessione')
    }
  }

  return (
    <div className="page">
      <div className="panel">
        <h2>Aggiungi un beneficiario</h2>
        <p className="panel-subtitle">
          Puoi salvare qualsiasi IBAN. I bonifici, però, sono possibili solo verso conti Nexa Bank:
          gli altri restano in rubrica contrassegnati come <em>esterni</em>.
        </p>
        <form onSubmit={handleSubmit} className="form">
          <Alert>{error}</Alert>
          <div className="field-row">
            <div className="field">
              <label htmlFor="ben-nome">Nome</label>
              <input id="ben-nome" value={nome} maxLength={60} required
                onChange={(e) => setNome(e.target.value)} placeholder="es. Marco Verdi" />
            </div>
            <div className="field">
              <label htmlFor="ben-iban">IBAN</label>
              <input
                id="ben-iban" value={iban} required
                className={`mono ${erroreIban ? 'input-error' : ibanCompleto ? 'input-ok' : ''}`}
                onChange={(e) => setIban(formatIbanInput(e.target.value))}
                placeholder="IT00 0000 …"
              />
              <span className={erroreIban ? 'field-error' : 'field-hint'}>
                {erroreIban || (ibanCompleto ? '✓ Formato corretto' : 'IT seguito da 26 cifre')}
              </span>
            </div>
          </div>
          <button type="submit" className="btn-primary" disabled={saving || !ibanCompleto}>
            {saving ? 'Salvataggio…' : 'Salva in rubrica'}
          </button>
        </form>
      </div>

      <div className="panel">
        <h2>Rubrica</h2>
        {loading ? (
          <SkeletonRows rows={3} />
        ) : beneficiaries.length === 0 ? (
          <p className="empty-state">Nessun beneficiario salvato.</p>
        ) : (
          <ul className="ben-list">
            {beneficiaries.map((b) => (
              <li key={b.id} className="ben-item">
                <span className="ben-avatar" aria-hidden="true">
                  {b.nome.charAt(0).toUpperCase()}
                </span>
                <div className="ben-main">
                  <span className="ben-name">
                    {b.nome}
                    {!b.contoInterno && (
                      <span
                        className="status-pill status-inactive"
                        title="Non è un conto Nexa Bank: da qui non puoi inviargli denaro"
                      >
                        Esterno
                      </span>
                    )}
                  </span>
                  <span className="ben-iban mono">{formatIban(b.iban)}</span>
                </div>
                <div className="ben-actions">
                  <button
                    type="button"
                    className="btn-ghost btn-sm"
                    disabled={!b.contoInterno}
                    title={b.contoInterno ? undefined : 'Conto non presente in Nexa Bank'}
                    onClick={() => navigate('/bonifico', { state: { iban: b.iban } })}
                  >
                    Invia
                  </button>
                  <button type="button" className="btn-icon-danger"
                    onClick={() => handleDelete(b)} aria-label={`Rimuovi ${b.nome}`}>
                    ×
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
