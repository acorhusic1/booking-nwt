import { createContext, useCallback, useContext, useState } from 'react'
import Toast from './Toast'

/**
 * Globalni Toast context. Bilo koja komponenta moze pozvati
 *   const { showToast } = useToast()
 *   showToast({ type: 'error', title: '...', message: '...' })
 *
 * Toast se renderuje na top-level (Portal u document.body kroz Toast.jsx),
 * tako da je vidljiv bez obzira gdje je komponenta u stablu.
 */
const ToastContext = createContext({ showToast: () => {} })

export function useToast() {
  return useContext(ToastContext)
}

export default function ToastProvider({ children }) {
  const [toast, setToast] = useState(null)

  const showToast = useCallback((data) => {
    setToast(null)
    setTimeout(() => setToast(data), 30)
  }, [])

  const hideToast = useCallback(() => setToast(null), [])

  return (
    <ToastContext.Provider value={{ showToast, hideToast }}>
      {children}
      <Toast
        open={!!toast}
        onClose={hideToast}
        type={toast?.type}
        title={toast?.title}
        message={toast?.message}
        duration={toast?.duration}
      />
    </ToastContext.Provider>
  )
}
