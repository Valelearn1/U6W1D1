import { useMemo, useState } from 'react'
import { useAccount } from '../context/contexts'
import { SkeletonRows } from '../components/Skeleton'
import MovementRow from '../components/MovementRow'
import { CATEGORIES, formatCurrency } from '../utils/format'

const FILTRI_DIREZIONE = [
  { value: 'ALL', label: 'Tutti' },
  { value: 'IN', label: 'Entrate' },
  { value: 'OUT', label: 'Uscite' },
]

export default function MovementsPage() {
  const { transactions, loading } = useAccount()
  const [direzione, setDirezione] = useState('ALL')
  const [categoria, setCategoria] = useState('ALL')
  const [ricerca, setRicerca] = useState('')

  const filtrati = useMemo(() => {
    const q = ricerca.replace(/\s+/g, '').toUpperCase()
    return transactions.filter((t) => {
      if (direzione !== 'ALL' && t.direzione !== direzione) return false
      if (categoria !== 'ALL' && t.categoria !== categoria) return false
      if (q) {
        const testo = `${t.ibanMittente || ''}${t.ibanDestinatario}${t.descrizione || ''}`.toUpperCase()
        if (!testo.includes(q)) return false
      }
      return true
    })
  }, [transactions, direzione, categoria, ricerca])

  const totali = useMemo(() => {
    const ok = filtrati.filter((t) => t.result === 'SUCCESS')
    return {
      entrate: ok.filter((t) => t.direzione === 'IN').reduce((a, t) => a + Number(t.importo), 0),
      uscite: ok.filter((t) => t.direzione === 'OUT').reduce((a, t) => a + Number(t.importo), 0),
    }
  }, [filtrati])

  return (
    <div className="page">
      <div className="panel">
        <div className="panel-head">
          <h2>Movimenti</h2>
          <span className="count-badge">{filtrati.length}</span>
        </div>

        <div className="filter-bar">
          <div className="segmented">
            {FILTRI_DIREZIONE.map((f) => (
              <button
                key={f.value}
                type="button"
                className={direzione === f.value ? 'segment active' : 'segment'}
                onClick={() => setDirezione(f.value)}
              >
                {f.label}
              </button>
            ))}
          </div>

          <select value={categoria} onChange={(e) => setCategoria(e.target.value)} aria-label="Filtra per categoria">
            <option value="ALL">Tutte le categorie</option>
            {CATEGORIES.map((c) => (
              <option key={c.value} value={c.value}>
                {c.icon} {c.label}
              </option>
            ))}
          </select>

          <input
            type="search"
            placeholder="Cerca IBAN o causale"
            value={ricerca}
            onChange={(e) => setRicerca(e.target.value)}
            aria-label="Cerca nei movimenti"
          />
        </div>

        <div className="totals-row">
          <div className="total-chip in">
            <span>Entrate</span>
            <strong>{formatCurrency(totali.entrate)}</strong>
          </div>
          <div className="total-chip out">
            <span>Uscite</span>
            <strong>{formatCurrency(totali.uscite)}</strong>
          </div>
        </div>

        {loading ? (
          <SkeletonRows rows={6} />
        ) : filtrati.length === 0 ? (
          <p className="empty-state">Nessun movimento corrisponde ai filtri scelti.</p>
        ) : (
          <ul className="tx-list">
            {filtrati.map((tx) => (
              <MovementRow key={tx.id} tx={tx} />
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
