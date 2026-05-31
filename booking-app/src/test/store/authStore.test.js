import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore } from '../../store/authStore'

describe('authStore', () => {
  beforeEach(() => {
    localStorage.clear()
    useAuthStore.setState({ user: null, token: null, refreshToken: null, isAuthenticated: false })
  })

  it('setAuth perzistira u localStorage i state', () => {
    const user = { id: 1, email: 'a@b.c', role: 'GUEST' }
    useAuthStore.getState().setAuth(user, 'tok', 'refresh')
    const state = useAuthStore.getState()
    expect(state.user).toEqual(user)
    expect(state.token).toBe('tok')
    expect(state.isAuthenticated).toBe(true)
    expect(localStorage.getItem('jwt_token')).toBe('tok')
    expect(JSON.parse(localStorage.getItem('user'))).toEqual(user)
  })

  it('logout brise sve', () => {
    useAuthStore.getState().setAuth({ id: 1 }, 'tok', 'r')
    useAuthStore.getState().logout()
    expect(useAuthStore.getState().isAuthenticated).toBe(false)
    expect(localStorage.getItem('jwt_token')).toBeNull()
  })

  it('setAuth bez refreshToken-a koristi postojeci iz localStorage', () => {
    localStorage.setItem('jwt_refresh_token', 'old-refresh')
    useAuthStore.getState().setAuth({ id: 2 }, 'new-token', null)
    expect(useAuthStore.getState().refreshToken).toBe('old-refresh')
  })
})
