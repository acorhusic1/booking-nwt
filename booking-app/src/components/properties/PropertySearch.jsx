import { useState } from 'react'
import '../../styles/PropertySearch.css'

export default function PropertySearch({ onSearch }) {
  const [city, setCity] = useState('')
  const [priceRange, setPriceRange] = useState([0, 2000])

  const handleSearch = () => {
    onSearch({ city, minPrice: priceRange[0], maxPrice: priceRange[1] })
  }

  return (
    <div className="property-search">
      <h2>Pretraga smještaja</h2>
      <div className="search-controls">
        <input
          type="text"
          placeholder="Unesite grad..."
          value={city}
          onChange={(e) => setCity(e.target.value)}
        />

        <label>Cijena po noći:</label>
        <input
          type="range"
          min="0"
          max="2000"
          value={priceRange[0]}
          onChange={(e) => setPriceRange([Number(e.target.value), priceRange[1]])}
        />
        <span>${priceRange[0]} - ${priceRange[1]}</span>

        <button onClick={handleSearch}>Pretraži</button>
      </div>
    </div>
  )
}

