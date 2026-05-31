import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import PropertyCard from '../../../components/properties/PropertyCard'

function wrap(ui) { return render(<MemoryRouter>{ui}</MemoryRouter>) }

const base = {
  id: 5, name: 'Vila Plava', city: 'Sarajevo', country: 'BiH',
  description: 'Lijepa vila u centru.', maxGuests: 4, available: true
}

describe('PropertyCard', () => {
  it('prikazuje ime, grad, drzavu', () => {
    wrap(<PropertyCard property={base} />)
    expect(screen.getByText('Vila Plava')).toBeInTheDocument()
    expect(screen.getByText(/Sarajevo, BiH/)).toBeInTheDocument()
  })

  it('available → "Dostupno"', () => {
    wrap(<PropertyCard property={base} />)
    expect(screen.getByText('Dostupno')).toBeInTheDocument()
  })

  it('unavailable → "Nije dostupno"', () => {
    wrap(<PropertyCard property={{ ...base, available: false }} />)
    expect(screen.getByText('Nije dostupno')).toBeInTheDocument()
  })

  it('linka na /properties/:id', () => {
    const { container } = wrap(<PropertyCard property={base} />)
    expect(container.querySelector('a').getAttribute('href')).toBe('/properties/5')
  })

  it('truncira opis duzi od 100 karaktera (…)', () => {
    const long = 'a'.repeat(150)
    wrap(<PropertyCard property={{ ...base, description: long }} />)
    expect(screen.getByText(/aaaa.*…$/)).toBeInTheDocument()
  })

  it('wishlist heart se prikazuje samo kad showWishlist=true', () => {
    const { container, rerender } = wrap(<PropertyCard property={base} />)
    expect(container.querySelector('.wishlist-heart')).toBeNull()
    rerender(
      <MemoryRouter>
        <PropertyCard property={base} showWishlist onToggleWishlist={() => {}} />
      </MemoryRouter>
    )
    expect(container.querySelector('.wishlist-heart')).toBeInTheDocument()
  })

  it('klik na heart poziva onToggleWishlist sa property ID', async () => {
    const onToggle = vi.fn()
    wrap(<PropertyCard property={base} showWishlist onToggleWishlist={onToggle} />)
    await userEvent.click(screen.getByRole('button'))
    expect(onToggle).toHaveBeenCalledWith(5)
  })

  it('wishlisted=true prikazuje crveno srce', () => {
    const { container } = wrap(
      <PropertyCard property={base} showWishlist wishlisted onToggleWishlist={() => {}} />
    )
    expect(container.querySelector('.wishlist-heart').textContent).toBe('❤️')
  })
})
