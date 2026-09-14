import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../context/contexts'
import ThemeToggle from '../ThemeToggle'

const LINKS = [
  { to: '/', label: 'Panoramica', end: true },
  { to: '/movimenti', label: 'Movimenti' },
  { to: '/bonifico', label: 'Bonifico' },
  { to: '/rubrica', label: 'Rubrica' },
]

export default function AppLayout() {
  const { user, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">
          <span className="brand-mark">N</span>
          <span className="brand-name">Nexa Bank</span>
        </div>

        <nav className="app-nav">
          {LINKS.map((l) => (
            <NavLink
              key={l.to}
              to={l.to}
              end={l.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              {l.label}
            </NavLink>
          ))}
        </nav>

        <div className="app-user">
          <ThemeToggle />
          <span className="app-user-name">{user?.nome}</span>
          <button type="button" className="btn-ghost" onClick={logout}>
            Esci
          </button>
        </div>
      </header>

      <main>
        <Outlet />
      </main>
    </div>
  )
}
