import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ConfirmModal from '../../../components/common/ConfirmModal'

describe('ConfirmModal', () => {
  it('renderuje message i custom button text-ove', () => {
    render(<ConfirmModal open onClose={() => {}} onConfirm={() => {}} message="Sigurno?" confirmText="Da" cancelText="Ne" />)
    expect(screen.getByText('Sigurno?')).toBeInTheDocument()
    expect(screen.getByText('Da')).toBeInTheDocument()
    expect(screen.getByText('Ne')).toBeInTheDocument()
  })

  it('poziva onConfirm pri klik na Potvrdi', async () => {
    const onConfirm = vi.fn().mockResolvedValue()
    render(<ConfirmModal open onClose={() => {}} onConfirm={onConfirm} message="Sigurno?" confirmText="Da" cancelText="Ne" />)
    await userEvent.click(screen.getByText('Da'))
    expect(onConfirm).toHaveBeenCalled()
  })

  it('poziva onClose pri klik na Otkazi', async () => {
    const onClose = vi.fn()
    render(<ConfirmModal open onClose={onClose} onConfirm={() => {}} message="?" confirmText="Da" cancelText="Ne" />)
    await userEvent.click(screen.getByText('Ne'))
    expect(onClose).toHaveBeenCalled()
  })
})
