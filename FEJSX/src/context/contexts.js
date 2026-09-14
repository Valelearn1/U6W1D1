import { createContext, useContext } from 'react'

/**
 * I context e i relativi hook stanno in un file senza componenti.
 * Così ogni file .jsx esporta solo componenti e il Fast Refresh di Vite
 * continua a funzionare durante lo sviluppo.
 */
export const AuthContext = createContext(null)
export const ThemeContext = createContext(null)
export const ToastContext = createContext(null)
export const AccountContext = createContext(null)

function useRequiredContext(context, nome) {
  const value = useContext(context)
  if (!value) throw new Error(`${nome} deve essere usato dentro il relativo Provider`)
  return value
}

export const useAuth = () => useRequiredContext(AuthContext, 'useAuth')
export const useTheme = () => useRequiredContext(ThemeContext, 'useTheme')
export const useToast = () => useRequiredContext(ToastContext, 'useToast')
export const useAccount = () => useRequiredContext(AccountContext, 'useAccount')
