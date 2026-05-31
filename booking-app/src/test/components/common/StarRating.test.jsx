import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import StarRating from '../../../components/common/StarRating'

describe('StarRating', () => {
  it('renderuje 5 zvjezdica', () => {
    const { container } = render(<StarRating value={0} readOnly />)
    expect(container.querySelectorAll('.star').length).toBe(5)
  })

  it('zaokruzuje 4.7 na 5 punih (BUG G)', () => {
    const { container } = render(<StarRating value={4.7} readOnly />)
    const stars = container.querySelectorAll('.star')
    expect(Array.from(stars).filter(s => s.classList.contains('filled')).length).toBe(5)
  })

  it('zaokruzuje 4.1 na 4 pune (BUG G)', () => {
    const { container } = render(<StarRating value={4.1} readOnly />)
    const stars = container.querySelectorAll('.star')
    expect(Array.from(stars).filter(s => s.classList.contains('filled')).length).toBe(4)
  })

  it('interactive mode poziva onChange kad se klikne', async () => {
    const onChange = vi.fn()
    const { container } = render(<StarRating value={0} onChange={onChange} />)
    const stars = container.querySelectorAll('.star')
    await userEvent.click(stars[3])
    expect(onChange).toHaveBeenCalledWith(4)
  })

  it('readOnly ignorise onChange clickove', async () => {
    const onChange = vi.fn()
    const { container } = render(<StarRating value={3} onChange={onChange} readOnly />)
    const stars = container.querySelectorAll('.star')
    await userEvent.click(stars[0])
    expect(onChange).not.toHaveBeenCalled()
  })
})
