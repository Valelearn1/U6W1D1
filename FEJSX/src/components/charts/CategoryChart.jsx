import { useMemo, useState } from 'react'
import { categoryInfo, formatCurrency } from '../../utils/format'

/**
 * Spese per categoria, barre orizzontali ordinate dalla piu' alta.
 *
 * Una sola tinta di proposito: la grandezza e' gia' codificata dalla
 * lunghezza della barra, dare un colore diverso a ogni categoria
 * aggiungerebbe rumore senza aggiungere informazione.
 */
export default function CategoryChart({ transactions }) {
  const [hover, setHover] = useState(null)

  const righe = useMemo(() => {
    const totali = new Map()
    transactions
      .filter((t) => t.result === 'SUCCESS' && t.direzione === 'OUT')
      .forEach((t) => {
        totali.set(t.categoria, (totali.get(t.categoria) || 0) + Number(t.importo))
      })
    return [...totali.entries()]
      .map(([categoria, totale]) => ({ categoria, totale }))
      .sort((a, b) => b.totale - a.totale)
  }, [transactions])

  if (righe.length === 0) {
    return <p className="empty-state">Nessuna spesa registrata: il riepilogo compare qui.</p>
  }

  const max = Math.max(...righe.map((r) => r.totale))
  const totaleGenerale = righe.reduce((acc, r) => acc + r.totale, 0)

  return (
    <div className="cat-chart">
      {righe.map((r, i) => {
        const info = categoryInfo(r.categoria)
        const perc = (r.totale / max) * 100
        return (
          <div
            key={r.categoria}
            className="cat-row"
            onMouseEnter={() => setHover(i)}
            onMouseLeave={() => setHover(null)}
          >
            <div className="cat-label">
              <span aria-hidden="true">{info.icon}</span>
              <span>{info.label}</span>
            </div>
            <div className="cat-track">
              <div
                className="cat-bar"
                style={{ width: `${Math.max(perc, 2)}%`, opacity: hover === null || hover === i ? 1 : 0.55 }}
              />
            </div>
            <div className="cat-value">{formatCurrency(r.totale)}</div>
          </div>
        )
      })}
      <div className="cat-total">
        <span>Totale uscite</span>
        <strong>{formatCurrency(totaleGenerale)}</strong>
      </div>
    </div>
  )
}
