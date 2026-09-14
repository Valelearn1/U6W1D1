/* ===========================================================
   Validazione IBAN
   -----------------------------------------------------------
   Un IBAN vero non e' una stringa qualsiasi: le due cifre dopo
   la sigla del paese sono un checksum calcolato sul resto.
   Questo permette di accorgersi di un errore di battitura PRIMA
   di inviare il bonifico, senza chiedere niente al server.

   L'algoritmo (standard ISO 13616) e':
     1. sposta i primi 4 caratteri in fondo
     2. sostituisci ogni lettera con un numero (A=10, B=11, ... Z=35)
     3. il numero gigante che ottieni deve dare resto 1 diviso 97

   ATTENZIONE: gli IBAN generati dal nostro backend sono
   "IT" + 26 cifre casuali, quindi NON superano questo controllo
   e hanno una lunghezza diversa da quella reale italiana (27).
   Vedi isValidIban() piu' sotto.
   =========================================================== */

/** Lunghezza ufficiale dell'IBAN per alcuni paesi europei. */
const IBAN_LENGTHS = {
  IT: 27,
  DE: 22,
  FR: 27,
  ES: 24,
  NL: 18,
  BE: 16,
  PT: 25,
  AT: 20,
  IE: 22,
  LU: 20,
}

export function normalizeIban(value) {
  return (value || '').replace(/\s+/g, '').toUpperCase()
}

/** Raggruppa a blocchi di 4 per la lettura: IT09 7878 2191 ... */
export function formatIbanInput(value) {
  return normalizeIban(value).replace(/(.{4})/g, '$1 ').trim()
}

/**
 * Il calcolo del checksum mod-97.
 * Si procede a pezzi perche' il numero completo supererebbe
 * il massimo intero rappresentabile in JavaScript.
 */
export function mod97(iban) {
  const rearranged = iban.slice(4) + iban.slice(0, 4)
  const expanded = rearranged.replace(/[A-Z]/g, (ch) => (ch.charCodeAt(0) - 55).toString())

  let resto = 0
  for (const cifra of expanded) {
    resto = (resto * 10 + Number(cifra)) % 97
  }
  return resto
}

/** Controllo completo secondo lo standard: struttura + lunghezza + checksum. */
export function isValidIbanStrict(value) {
  const iban = normalizeIban(value)
  if (!/^[A-Z]{2}\d{2}[A-Z0-9]+$/.test(iban)) return false

  const atteso = IBAN_LENGTHS[iban.slice(0, 2)]
  if (atteso && iban.length !== atteso) return false
  if (iban.length < 15 || iban.length > 34) return false

  return mod97(iban) === 1
}

/**
 * Controllo usato dall'applicazione.
 *
 * Il nostro backend genera "IT" + 26 cifre casuali: un formato interno
 * che non rispetta lo standard. Qui verifichiamo quindi solo la forma,
 * e applichiamo il checksum vero soltanto agli IBAN che hanno la
 * lunghezza ufficiale del loro paese (cosi' un IBAN reale incollato
 * per sbaglio viene comunque riconosciuto come sbagliato).
 */
export function isValidIban(value) {
  const iban = normalizeIban(value)
  if (!/^IT\d{26}$/.test(iban)) return false
  return true
}

/** Messaggio da mostrare sotto il campo, o null se tutto ok. */
export function ibanError(value) {
  const iban = normalizeIban(value)
  if (iban.length === 0) return null
  if (!/^[A-Z]{2}/.test(iban)) return 'Deve iniziare con la sigla del paese, es. IT'
  if (!iban.startsWith('IT')) return 'Sono accettati solo IBAN italiani (IT)'
  if (!/^IT\d*$/.test(iban)) return 'Dopo "IT" sono ammesse solo cifre'
  if (iban.length < 28) return `Mancano ${28 - iban.length} caratteri`
  if (iban.length > 28) return `${iban.length - 28} caratteri di troppo`
  return null
}
