import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ToastProvider, { useToast } from '../../../components/common/ToastProvider'

function TriggerButton({ data }) {
  const { showToast } = useToast()
  return <button onClick={() => showToast(data)}>fire</button>
}

describe('ToastProvider', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('prikazuje toast nakon showToast', async () => {
    render(
      <ToastProvider>
        <TriggerButton data={{ type: 'success', title: 'OK', message: 'super' }} />
      </ToastProvider>
    )
    await act(async () => {
      screen.getByText('fire').click()
      // showToast koristi setTimeout(30) interno za reset
      vi.advanceTimersByTime(50)
    })
    expect(screen.getByText('OK')).toBeInTheDocument()
    expect(screen.getByText('super')).toBeInTheDocument()
  })

  it('auto-dismiss nakon duration', async () => {
    render(
      <ToastProvider>
        <TriggerButton data={{ type: 'info', title: 'X', message: 'y', duration: 1000 }} />
      </ToastProvider>
    )
    await act(async () => {
      screen.getByText('fire').click()
      vi.advanceTimersByTime(50)
    })
    expect(screen.getByText('X')).toBeInTheDocument()
    await act(async () => { vi.advanceTimersByTime(1500) })
    expect(screen.queryByText('X')).toBeNull()
  })
})
