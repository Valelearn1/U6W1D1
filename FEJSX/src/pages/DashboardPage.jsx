import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth, useToast, useAccount } from '../context/contexts'
import { api, ApiError } from '../api/client'
import BalanceChart from '../components/charts/BalanceChart'
import CategoryChart from '../components/charts/CategoryChart'
import { SkeletonCard, SkeletonRows } from '../components/Skeleton'
import MovementRow from '../components/MovementRow'
import { formatCurrency, formatDate, formatIban } from '../utils/format'

export default function DashboardPage() {
  const { user, token, refreshUser } = useAuth()
  const { transactions, loading, refresh } = useAccount()
  const toast = useToast()
  const navigate = useNavigate()

  const [copied, setCopied] = useState(false)
  const [importo, setImporto] = useState('')
  const [depositing, setDepositing] = useState(false)

  async function copyIban() {
    try {
      await navigator.clipboard.writeText(user.iban)
      setCopied(true)
      toast.info('IBAN copiato negli appunti')
      setTimeout(() => setCopied(false), 1800)
    } catch {
      toast.error('Copia non riuscita: seleziona il testo manualmente')
    }
  }

  async function handleDeposit(e) {
    e.preventDefault()
    setDepositing(true)
    try {
      await api.deposit({ importo: Number(importo) }, token)
      toast.success(`Ricarica di ${formatCurrency(Number(importo))} completata`)
      setImporto('')
      await Promise.all([refreshUser(), refresh()])
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Errore di connessione')
    } finally {
      setDepositing(false)
    }
  }

  const ultimi = transactions.slice(0, 5)

  return (
    <div className="page">
      {loading && !user ? (
        <SkeletonCard />
      ) : (
        <section className="balance-card">
          <div className="balance-card-top">
            <span className="balance-label">Saldo disponibile</span>
            <span className={`status-pill ${user.active ? 'status-active' : 'status-inactive'}`}>
              {user.active ? 'Account attivo' : 'Non attivo'}
            </span>
          </div>
          <div className="balance-amount">{formatCurrency(user.saldo)}</div>
          <button type="button" className="balance-iban" onClick={copyIban}>
            <span className="iban-code">{formatIban(user.iban)}</span>
            <span className="iban-copy">{copied ? 'Copiato!' : 'Copia IBAN'}</span>
          </button>
        </section>
      )}

      <section className="quick-grid">
        <div className="panel panel-compact">
          <h2>Ricarica il conto</h2>
          <p className="panel-subtitle">Accredito immediato, massimo 5.000 € per operazione.</p>
          <form onSubmit={handleDeposit} className="inline-form">
            <input
              type="number" min="0.01" max="5000" step="0.01" required
              placeholder="0,00" value={importo} onChange={(e) => setImporto(e.target.value)}
              aria-label="Importo della ricarica"
            />
            <button type="submit" className="btn-primary" disabled={depositing}>
              {depositing ? '…' : 'Ricarica'}
            </button>
          </form>
          <div className="chip-row">
            {[50, 100, 250].map((v) => (
              <button key={v} type="button" className="chip" onClick={() => setImporto(String(v))}>
                +{v} €
              </button>
            ))}
          </div>
        </div>

        <div className="panel panel-compact">
          <h2>Invia denaro</h2>
          <p className="panel-subtitle">Scegli un contatto in rubrica o inserisci un IBAN.</p>
          <div className="stack-actions">
            <button type="button" className="btn-primary" onClick={() => navigate('/bonifico')}>
              Nuovo bonifico
            </button>
            <Link to="/rubrica" className="btn-ghost btn-block">
              Gestisci rubrica
            </Link>
          </div>
        </div>
      </section>

      <section className="panel">
        <h2>Andamento del saldo</h2>
        <p className="panel-subtitle">Ricostruito dai movimenti completati sul tuo conto.</p>
        {loading ? <SkeletonRows rows={2} /> : (
          <BalanceChart transactions={transactions} saldoAttuale={user.saldo} />
        )}
      </section>

      <section className="panel">
        <h2>Uscite per categoria</h2>
        <p className="panel-subtitle">Solo i bonifici completati in uscita.</p>
        {loading ? <SkeletonRows rows={3} /> : <CategoryChart transactions={transactions} />}
      </section>

      <section className="panel">
        <div className="panel-head">
          <h2>Ultimi movimenti</h2>
          <Link to="/movimenti" className="link-btn">
            Vedi tutti
          </Link>
        </div>
        {loading ? (
          <SkeletonRows rows={4} />
        ) : ultimi.length === 0 ? (
          <p className="empty-state">Nessun movimento sul tuo conto.</p>
        ) : (
          <ul className="tx-list">
            {ultimi.map((tx) => (
              <MovementRow key={tx.id} tx={tx} />
            ))}
          </ul>
        )}
      </section>

      <section className="panel">
        <h2>Il tuo profilo</h2>
        <dl className="detail-list">
          <div>
            <dt>Nome</dt>
            <dd>{user.nome} {user.cognome}</dd>
          </div>
          <div>
            <dt>Email</dt>
            <dd>{user.email}</dd>
          </div>
          <div>
            <dt>Cliente da</dt>
            <dd>{formatDate(user.createdAt)}</dd>
          </div>
        </dl>
      </section>
    </div>
  )
}
