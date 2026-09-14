import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client'
import { AccountContext, useAuth } from './contexts'

/**
 * Dati del conto condivisi fra le pagine (movimenti e rubrica),
 * così ogni schermata non rifà le stesse chiamate.
 */
export function AccountProvider({ children }) {
  const { token, user } = useAuth()
  const [transactions, setTransactions] = useState([])
  const [beneficiaries, setBeneficiaries] = useState([])
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    if (!token) return
    setLoading(true)
    try {
      const [tx, ben] = await Promise.all([api.transactions(token), api.beneficiaries(token)])
      setTransactions(tx)
      setBeneficiaries(ben)
    } catch {
      setTransactions([])
      setBeneficiaries([])
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    if (!user) return
    // Stesso motivo spiegato in AuthContext: caricamento dati da un sistema
    // esterno, con lo stato aggiornato dentro una callback asincrona.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    refresh()
  }, [user, refresh])

  return (
    <AccountContext.Provider value={{ transactions, beneficiaries, loading, refresh }}>
      {children}
    </AccountContext.Provider>
  )
}
