import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import NotificationsList from '../../../components/notifications/NotificationsList'

vi.mock('../../../api/notificationApi', () => ({
  notificationApi: {
    getByUserId: vi.fn().mockResolvedValue([
      { id: 1, type: 'NOVA_REZERVACIJA', title: 'Nova', content: 'rez 5', isRead: false, createdAt: '2026-05-30T10:00:00', relatedReservationId: 5, relatedPropertyId: 9 },
      { id: 2, type: 'POTVRDA_REZERVACIJE', title: 'Potvrda', content: 'ok', isRead: true, createdAt: '2026-05-29T10:00:00' }
    ]),
    markAsRead: vi.fn().mockResolvedValue({})
  }
}))

const mockUser = { user: { id: 7, role: 'GUEST' } }
vi.mock('../../../store/authStore', () => ({
  useAuthStore: () => mockUser
}))

describe('NotificationsList', () => {
  it('renderuje notifikacije iz API-a', async () => {
    render(<MemoryRouter><NotificationsList /></MemoryRouter>)
    expect(await screen.findByText('rez 5')).toBeInTheDocument()
    expect(screen.getByText('ok')).toBeInTheDocument()
  })

  it('GUEST link na Rezervacija ide ka /dashboard?tab=reservations (BUG C)', async () => {
    render(<MemoryRouter><NotificationsList /></MemoryRouter>)
    const link = await screen.findByText(/Rezervacija #5/)
    expect(link.getAttribute('href')).toBe('/dashboard?tab=reservations')
  })

  it('HOST link vodi na /host/dashboard (BUG C)', async () => {
    mockUser.user = { id: 3, role: 'HOST' }
    render(<MemoryRouter><NotificationsList /></MemoryRouter>)
    const link = await screen.findByText(/Rezervacija #5/)
    expect(link.getAttribute('href')).toBe('/host/dashboard')
    mockUser.user = { id: 7, role: 'GUEST' }
  })

  it('poziva onUnreadChange sa brojem nepročitanih', async () => {
    const onUnreadChange = vi.fn()
    render(<MemoryRouter><NotificationsList onUnreadChange={onUnreadChange} /></MemoryRouter>)
    await waitFor(() => {
      expect(onUnreadChange).toHaveBeenCalledWith(1)
    })
  })
})
