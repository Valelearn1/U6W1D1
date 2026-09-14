import { useCallback, useMemo, useRef, useState } from 'react'
import { ToastContext } from './contexts'

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const nextId = useRef(1)

  const dismiss = useCallback((id) => {
    setToasts((list) => list.filter((t) => t.id !== id))
  }, [])

  const push = useCallback(
    (message, variant, duration = 4000) => {
      const id = nextId.current++
      setToasts((list) => [...list, { id, message, variant }])
      if (duration > 0) setTimeout(() => dismiss(id), duration)
      return id
    },
    [dismiss],
  )

  const toast = useMemo(
    () => ({
      success: (m) => push(m, 'success'),
      error: (m) => push(m, 'error'),
      info: (m) => push(m, 'info'),
    }),
    [push],
  )

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="toast-stack" role="status" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.variant}`}>
            <span className="toast-icon" aria-hidden="true">
              {t.variant === 'success' ? '✓' : t.variant === 'error' ? '!' : 'i'}
            </span>
            <span className="toast-text">{t.message}</span>
            <button type="button" className="toast-close" onClick={() => dismiss(t.id)} aria-label="Chiudi">
              ×
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
