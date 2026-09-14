import { categoryInfo, formatCurrency, formatDate, formatIban, resultLabel } from '../utils/format'

export default function MovementRow({ tx }) {
  const inUscita = tx.direzione === 'OUT'
  const info = categoryInfo(tx.categoria)
  const controparte = inUscita ? tx.ibanDestinatario : tx.ibanMittente

  return (
    <li className="tx-item">
      <span className="tx-cat" aria-hidden="true" title={info.label}>
        {info.icon}
      </span>
      <div className="tx-item-main">
        <span className="tx-item-title">
          {tx.tipo === 'DEPOSIT'
            ? 'Ricarica conto'
            : `${inUscita ? 'Verso' : 'Da'} ${formatIban(controparte)}`}
        </span>
        <span className="tx-item-date">
          {formatDate(tx.createdAt)}
          {tx.descrizione && tx.tipo !== 'DEPOSIT' ? ` · ${tx.descrizione}` : ''}
        </span>
      </div>
      <div className="tx-item-right">
        <span className={inUscita ? 'tx-item-amount out' : 'tx-item-amount in'}>
          {inUscita ? '−' : '+'} {formatCurrency(tx.importo)}
        </span>
        <span className={`status-pill status-${tx.result.toLowerCase()}`}>
          {resultLabel(tx.result)}
        </span>
      </div>
    </li>
  )
}
