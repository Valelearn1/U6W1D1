import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import AppLayout from './components/layout/AppLayout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import VerifyPage from './pages/VerifyPage'
import DashboardPage from './pages/DashboardPage'
import MovementsPage from './pages/MovementsPage'
import TransferPage from './pages/TransferPage'
import ConfirmTransferPage from './pages/ConfirmTransferPage'
import BeneficiariesPage from './pages/BeneficiariesPage'
import './App.css'

export default function App() {
  return (
    <Routes>
      {/* Pubbliche */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/registrati" element={<RegisterPage />} />
      <Route path="/verifica" element={<VerifyPage />} />

      {/* Protette: condividono header e navigazione */}
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<DashboardPage />} />
        <Route path="/movimenti" element={<MovementsPage />} />
        <Route path="/bonifico" element={<TransferPage />} />
        <Route path="/bonifico/conferma/:id" element={<ConfirmTransferPage />} />
        <Route path="/rubrica" element={<BeneficiariesPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
