import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Header from '../../../components/common/Header'

vi.mock('../../../api/notificationApi', () => ({
  notificationApi: { countUnread: vi.fn().mockResolvedValue(0) }
}))
vi.mock('../../../components/auth/LogoutButton', () => ({
  default: () => <button>Logout</button>
}))

const authState = { isAuthenticated: false, user: null }
vi.mock('../../../store/authStore', () => ({
  useAuthStore: () => authState
}))

function wrap(ui) { return render(<MemoryRouter>{ui}</MemoryRouter>) }

describe('Header', () => {
  beforeEach(() => {
    authState.isAuthenticated = false
    authState.user = null
  })

  it('neauth → prikazuje Prijava + Registracija', () => {
    wrap(<Header />)
    expect(screen.getByText('Prijava')).toBeInTheDocument()
    expect(screen.getByText('Registracija')).toBeInTheDocument()
  })

  it('GUEST vidi Liste zelja + Poruke', () => {
    authState.isAuthenticated = true
    authState.user = { id: 7, role: 'GUEST', email: 'g@b.c' }
    wrap(<Header />)
    expect(screen.getByText(/Liste želja/)).toBeInTheDocument()
    expect(screen.getByText(/Poruke/)).toBeInTheDocument()
    expect(screen.queryByText(/Admin/)).toBeNull()
  })

  it('HOST vidi Moji smjestaji + Poruke (NE Liste zelja)', () => {
    authState.isAuthenticated = true
    authState.user = { id: 3, role: 'HOST', email: 'h@b.c' }
    wrap(<Header />)
    expect(screen.getByText(/Moji smještaji/)).toBeInTheDocument()
    expect(screen.queryByText(/Liste želja/)).toBeNull()
  })

  it('ADMIN vidi Admin link', () => {
    authState.isAuthenticated = true
    authState.user = { id: 1, role: 'ADMIN', email: 'a@b.c' }
    wrap(<Header />)
    expect(screen.getByText(/Admin/)).toBeInTheDocument()
  })

  it('badge se prikazuje kad unread > 0', async () => {
    const { notificationApi } = await import('../../../api/notificationApi')
    notificationApi.countUnread.mockResolvedValueOnce(5)
    authState.isAuthenticated = true
    authState.user = { id: 7, role: 'GUEST', email: 'g@b.c' }
    const { container } = wrap(<Header />)
    await waitFor(() => {
      expect(container.querySelector('.bell-badge')).toBeInTheDocument()
      expect(container.querySelector('.bell-badge').textContent).toBe('5')
    })
  })

  it('badge 9+ za vise od 9 nepročitanih', async () => {
    const { notificationApi } = await import('../../../api/notificationApi')
    notificationApi.countUnread.mockResolvedValueOnce(42)
    authState.isAuthenticated = true
    authState.user = { id: 7, role: 'GUEST', email: 'g@b.c' }
    const { container } = wrap(<Header />)
    await waitFor(() => {
      expect(container.querySelector('.bell-badge').textContent).toBe('9+')
    })
  })

  it('logo linka na home', () => {
    const { container } = wrap(<Header />)
    const logo = container.querySelector('.logo')
    expect(logo.getAttribute('href')).toBe('/')
  })
})
