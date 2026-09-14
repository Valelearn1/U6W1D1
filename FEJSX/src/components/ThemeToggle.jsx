import { useTheme } from '../context/contexts'

export default function ThemeToggle() {
  const { theme, toggle } = useTheme()
  const scuro = theme === 'dark'
  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={toggle}
      aria-label={scuro ? 'Passa al tema chiaro' : 'Passa al tema scuro'}
      title={scuro ? 'Tema chiaro' : 'Tema scuro'}
    >
      {scuro ? '☀' : '☾'}
    </button>
  )
}
