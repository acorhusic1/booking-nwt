import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ErrorState from '../../../components/common/ErrorState'

describe('ErrorState', () => {
  it('ne renderuje nista bez message', () => {
    const { container } = render(<ErrorState />)
    expect(container.firstChild).toBeNull()
  })

  it('prikazuje message', () => {
    render(<ErrorState message="Nesto je puklo" />)
    expect(screen.getByText('Nesto je puklo')).toBeInTheDocument()
  })

  it('bez onRetry NE prikazuje retry dugme', () => {
    render(<ErrorState message="x" />)
    expect(screen.queryByRole('button')).toBeNull()
  })

  it('sa onRetry prikazuje dugme i poziva ga na klik', async () => {
    const onRetry = vi.fn()
    render(<ErrorState message="x" onRetry={onRetry} />)
    await userEvent.click(screen.getByRole('button'))
    expect(onRetry).toHaveBeenCalled()
  })

  it('custom retryLabel', () => {
    render(<ErrorState message="x" onRetry={() => {}} retryLabel="Probaj ponovo" />)
    expect(screen.getByText(/Probaj ponovo/)).toBeInTheDocument()
  })

  it('ima role="alert" za a11y', () => {
    render(<ErrorState message="x" />)
    expect(screen.getByRole('alert')).toBeInTheDocument()
  })
})
