import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client'
import { AuthContext } from './contexts'

const TOKEN_KEY = 'nexabank_token'

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState(null)
  // Se non c'e' un token da verificare non c'e' nulla da caricare:
  // partiamo gia' con loading a false, senza toccarlo dentro l'effect.
  const [loading, setLoading] = useState(() => Boolean(localStorage.getItem(TOKEN_KEY)))

  const loadUser = useCallback(async (currentToken) => {
    try {
      setUser(await api.me(currentToken))
    } catch {
      localStorage.removeItem(TOKEN_KEY)
      setToken(null)
      setUser(null)
    }
  }, [])

  useEffect(() => {
    const saved = localStorage.getItem(TOKEN_KEY)
    if (!saved) return

    let annullato = false
    // La regola scoraggia il fetch dentro useEffect (suggerisce una libreria
    // di data-fetching o Suspense). Qui e' pero' esattamente il caso previsto
    // dalle eccezioni: sincronizziamo con un sistema esterno (l'API) e lo stato
    // viene impostato in una callback asincrona, non nel corpo dell'effect.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadUser(saved).finally(() => {
      if (!annullato) setLoading(false)
    })
    return () => {
      annullato = true
    }
  }, [loadUser])

  const login = useCallback(
    async (authResponse) => {
      localStorage.setItem(TOKEN_KEY, authResponse.token)
      setToken(authResponse.token)
      await loadUser(authResponse.token)
    },
    [loadUser],
  )

  const logout = useCallback(async () => {
    try {
      if (token) await api.logout(token)
    } catch {
      // Anche se la revoca lato server fallisce, ripuliamo comunque la sessione locale
    }
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUser(null)
  }, [token])

  const refreshUser = useCallback(() => {
    if (!token) return Promise.resolve()
    return loadUser(token)
  }, [token, loadUser])

  return (
    <AuthContext.Provider value={{ token, user, loading, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}
