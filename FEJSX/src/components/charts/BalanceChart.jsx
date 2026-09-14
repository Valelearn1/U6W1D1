import { useMemo, useState } from 'react'
import { formatCurrency, formatDateShort } from '../../utils/format'

const W = 720
const H = 220
const PAD = { top: 16, right: 16, bottom: 28, left: 56 }

/**
 * Andamento del saldo nel tempo.
 * Una sola serie: area a tinta unita, nessuna legenda (il titolo la nomina).
 * Il saldo viene ricostruito a ritroso partendo da quello attuale.
 */
export default function BalanceChart({ transactions, saldoAttuale }) {
  const [hover, setHover] = useState(null)

  const punti = useMemo(() => {
    const completate = transactions
      .filter((t) => t.result === 'SUCCESS')
      .slice()
      .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))

    if (completate.length === 0) return []

    // Saldo iniziale = saldo attuale meno tutti i movimenti andati a buon fine
    let saldo = completate.reduce(
      (acc, t) => acc - (t.direzione === 'OUT' ? -Number(t.importo) : Number(t.importo)),
      Number(saldoAttuale),
    )

    const serie = [{ data: completate[0].createdAt, saldo }]
    for (const t of completate) {
      saldo += t.direzione === 'OUT' ? -Number(t.importo) : Number(t.importo)
      serie.push({ data: t.createdAt, saldo })
    }
    return serie
  }, [transactions, saldoAttuale])

  if (punti.length < 2) {
    return (
      <p className="empty-state">
        Il grafico compare dopo il primo movimento completato sul conto.
      </p>
    )
  }

  const valori = punti.map((p) => p.saldo)
  const min = Math.min(...valori, 0)
  const max = Math.max(...valori)
  const span = max - min || 1

  const x = (i) => PAD.left + (i / (punti.length - 1)) * (W - PAD.left - PAD.right)
  const y = (v) => PAD.top + (1 - (v - min) / span) * (H - PAD.top - PAD.bottom)

  const linea = punti.map((p, i) => `${i === 0 ? 'M' : 'L'} ${x(i)} ${y(p.saldo)}`).join(' ')
  const area = `${linea} L ${x(punti.length - 1)} ${y(min)} L ${x(0)} ${y(min)} Z`

  // Tre riferimenti orizzontali: minimo, metà, massimo
  const ticks = [min, min + span / 2, max]

  return (
    <div className="chart">
      <svg
        viewBox={`0 0 ${W} ${H}`}
        className="chart-svg"
        role="img"
        aria-label={`Andamento del saldo: da ${formatCurrency(punti[0].saldo)} a ${formatCurrency(
          punti[punti.length - 1].saldo,
        )}`}
        onMouseLeave={() => setHover(null)}
      >
        <defs>
          <linearGradient id="areaFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--chart-1)" stopOpacity="0.28" />
            <stop offset="100%" stopColor="var(--chart-1)" stopOpacity="0.02" />
          </linearGradient>
        </defs>

        {ticks.map((t, i) => (
          <g key={i}>
            <line x1={PAD.left} y1={y(t)} x2={W - PAD.right} y2={y(t)} className="chart-gridline" />
            <text x={PAD.left - 10} y={y(t) + 4} className="chart-tick" textAnchor="end">
              {Math.round(t)}
            </text>
          </g>
        ))}

        <path d={area} fill="url(#areaFill)" />
        <path d={linea} className="chart-line" />

        {punti.map((p, i) => (
          <circle
            key={i}
            cx={x(i)}
            cy={y(p.saldo)}
            r={hover === i ? 5 : 0}
            className="chart-dot"
          />
        ))}

        {/* Zone invisibili piu' larghe dei punti, per un hover comodo */}
        {punti.map((p, i) => (
          <rect
            key={`h${i}`}
            x={x(i) - (W / punti.length) / 2}
            y={PAD.top}
            width={W / punti.length}
            height={H - PAD.top - PAD.bottom}
            fill="transparent"
            onMouseEnter={() => setHover(i)}
          />
        ))}

        {hover !== null && (
          <line
            x1={x(hover)}
            y1={PAD.top}
            x2={x(hover)}
            y2={H - PAD.bottom}
            className="chart-crosshair"
          />
        )}

        <text x={PAD.left} y={H - 8} className="chart-tick">
          {formatDateShort(punti[0].data)}
        </text>
        <text x={W - PAD.right} y={H - 8} className="chart-tick" textAnchor="end">
          {formatDateShort(punti[punti.length - 1].data)}
        </text>
      </svg>

      {hover !== null && (
        <div className="chart-tooltip" style={{ left: `${(x(hover) / W) * 100}%` }}>
          <strong>{formatCurrency(punti[hover].saldo)}</strong>
          <span>{formatDateShort(punti[hover].data)}</span>
        </div>
      )}
    </div>
  )
}
