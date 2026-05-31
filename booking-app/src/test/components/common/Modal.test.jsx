import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Modal from '../../../components/common/Modal'

describe('Modal', () => {
  it('ne renderuje nista kad nije open', () => {
    const { container } = render(<Modal open={false} onClose={() => {}}><p>hi</p></Modal>)
    expect(container.querySelector('.modal-overlay')).toBeNull()
  })

  it('renderuje children + title kad je open', () => {
    render(<Modal open onClose={() => {}} title="Test"><p>sadrzaj</p></Modal>)
    expect(screen.getByText('Test')).toBeInTheDocument()
    expect(screen.getByText('sadrzaj')).toBeInTheDocument()
  })

  it('zatvara se klikom na backdrop kad je closeOnBackdrop true', () => {
    const onClose = vi.fn()
    render(<Modal open onClose={onClose}><p>x</p></Modal>)
    const overlay = document.querySelector('.modal-overlay')
    // fireEvent dispatches click sa target === currentTarget == overlay (sto Modal trazi)
    fireEvent.click(overlay)
    expect(onClose).toHaveBeenCalled()
  })

  it('NE zatvara se klikom unutar modala (BUG E)', async () => {
    const onClose = vi.fn()
    render(<Modal open onClose={onClose}><button>btn unutra</button></Modal>)
    await userEvent.click(screen.getByText('btn unutra'))
    expect(onClose).not.toHaveBeenCalled()
  })

  it('klik unutar modala ne bubbla parentu (BUG E — portal fix)', async () => {
    const parentClick = vi.fn()
    render(
      <div onClick={parentClick}>
        <Modal open onClose={() => {}}>
          <button>inner</button>
        </Modal>
      </div>
    )
    await userEvent.click(screen.getByText('inner'))
    // Bez stopPropagation u Modal-u, parentClick bi se zvao zbog React portal event tree
    expect(parentClick).not.toHaveBeenCalled()
  })

  it('zatvara se na Escape tipku', async () => {
    const onClose = vi.fn()
    render(<Modal open onClose={onClose}><p>x</p></Modal>)
    await userEvent.keyboard('{Escape}')
    expect(onClose).toHaveBeenCalled()
  })
})
