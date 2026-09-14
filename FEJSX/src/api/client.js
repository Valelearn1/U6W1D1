const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

async function request(path, { method = 'GET', body, token } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  let data = null
  const text = await response.text()
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = null
    }
  }

  if (!response.ok) {
    throw new ApiError(data?.message || `Errore ${response.status}`, response.status)
  }

  return data
}

export const api = {
  // Autenticazione
  register: (dto) => request('/api/users/register', { method: 'POST', body: dto }),
  verify: (dto) => request('/api/users/verify', { method: 'POST', body: dto }),
  loginPassword: (dto) => request('/api/users/login/password', { method: 'POST', body: dto }),
  requestLoginCode: (dto) => request('/api/users/login/code/request', { method: 'POST', body: dto }),
  loginCode: (dto) => request('/api/users/login/code', { method: 'POST', body: dto }),
  logout: (token) => request('/api/users/logout', { method: 'POST', token }),
  me: (token) => request('/api/users/me', { token }),

  // Movimenti
  transactions: (token) => request('/api/transactions', { token }),
  newTransaction: (dto, token) => request('/api/transactions', { method: 'POST', body: dto, token }),
  executeTransaction: (dto, token) =>
    request('/api/transactions/execute', { method: 'PATCH', body: dto, token }),
  deposit: (dto, token) => request('/api/transactions/deposit', { method: 'POST', body: dto, token }),

  // Rubrica
  beneficiaries: (token) => request('/api/beneficiaries', { token }),
  addBeneficiary: (dto, token) => request('/api/beneficiaries', { method: 'POST', body: dto, token }),
  deleteBeneficiary: (id, token) =>
    request(`/api/beneficiaries/${id}`, { method: 'DELETE', token }),
}
