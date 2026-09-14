export function formatCurrency(value) {
  const number = typeof value === 'string' ? parseFloat(value) : value
  return new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' }).format(number ?? 0)
}

export function formatDate(isoString) {
  if (!isoString) return ''
  return new Intl.DateTimeFormat('it-IT', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(isoString))
}

export function formatDateShort(isoString) {
  if (!isoString) return ''
  return new Intl.DateTimeFormat('it-IT', { day: '2-digit', month: 'short' }).format(
    new Date(isoString),
  )
}

export function formatIban(iban) {
  if (!iban) return ''
  return iban.replace(/(.{4})/g, '$1 ').trim()
}

export function resultLabel(result) {
  switch (result) {
    case 'SUCCESS':
      return 'Completato'
    case 'REJECT':
      return 'Rifiutato'
    default:
      return 'In attesa'
  }
}

export const CATEGORIES = [
  { value: 'SPESA', label: 'Spesa', icon: '🛒' },
  { value: 'CASA', label: 'Casa', icon: '🏠' },
  { value: 'TRASPORTI', label: 'Trasporti', icon: '🚆' },
  { value: 'SVAGO', label: 'Svago', icon: '🎬' },
  { value: 'SALUTE', label: 'Salute', icon: '💊' },
  { value: 'STIPENDIO', label: 'Stipendio', icon: '💼' },
  { value: 'RICARICA', label: 'Ricarica', icon: '⬆️' },
  { value: 'ALTRO', label: 'Altro', icon: '•' },
]

const CATEGORY_MAP = Object.fromEntries(CATEGORIES.map((c) => [c.value, c]))

export function categoryInfo(value) {
  return CATEGORY_MAP[value] || CATEGORY_MAP.ALTRO
}
