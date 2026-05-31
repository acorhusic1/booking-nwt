import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import Spinner from '../../../components/common/Spinner'

describe('Spinner', () => {
  it('default label "Učitavanje..."', () => {
    render(<Spinner />)
    expect(screen.getByText('Učitavanje...')).toBeInTheDocument()
  })

  it('custom label', () => {
    render(<Spinner label="Saljemo..." />)
    expect(screen.getByText('Saljemo...')).toBeInTheDocument()
  })

  it('size prop dodaje CSS klasu', () => {
    const { container } = render(<Spinner size="lg" />)
    expect(container.querySelector('.spinner-lg')).toBeInTheDocument()
  })

  it('inline prop dodaje spinner-inline klasu', () => {
    const { container } = render(<Spinner inline />)
    expect(container.querySelector('.spinner-inline')).toBeInTheDocument()
  })

  it('ima role="status" za a11y', () => {
    render(<Spinner />)
    expect(screen.getByRole('status')).toBeInTheDocument()
  })
})
