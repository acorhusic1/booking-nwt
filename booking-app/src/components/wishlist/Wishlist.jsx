import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { wishlistApi } from '../../api/wishlistApi'
import { propertyApi } from '../../api/propertyApi'
import { useAuthStore } from '../../store/authStore'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'
import ErrorState from '../common/ErrorState'
import '../../styles/Wishlist.css'

/**
 * F10 — Liste želja. Gost upravlja imenovanim listama (npr. "Ljeto 2026")
 * i u svakoj ima sačuvane smještaje.
 */
export default function Wishlist() {
  const { user } = useAuthStore()
  const { showToast } = useToast()
  const [wishlists, setWishlists] = useState([])
  const [itemsByList, setItemsByList] = useState({}) // { wishlistId: [{item, property}] }
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [newListName, setNewListName] = useState('')
  const [creating, setCreating] = useState(false)

  const load = useCallback(async () => {
    if (!user?.id) return
    setLoading(true)
    setError(null)
    try {
      const lists = await wishlistApi.getByGuest(user.id)
      setWishlists(lists)

      // Za svaku listu dohvati stavke + property detalje
      const map = {}
      for (const list of lists) {
        const items = await wishlistApi.getItems(list.id)
        const withProps = await Promise.all(items.map(async (it) => {
          const property = await propertyApi.getById(it.propertyId).catch(() => null)
          return { item: it, property }
        }))
        map[list.id] = withProps
      }
      setItemsByList(map)
    } catch {
      setError('Greška pri učitavanju lista želja')
    } finally {
      setLoading(false)
    }
  }, [user?.id])

  useEffect(() => { load() }, [load])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!newListName.trim()) return
    setCreating(true)
    try {
      await wishlistApi.create(user.id, newListName.trim())
      setNewListName('')
      showToast({ type: 'success', title: 'Lista kreirana', message: `"${newListName.trim()}" je dodana.` })
      load()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Lista nije kreirana.' })
    } finally {
      setCreating(false)
    }
  }

  const handleDeleteList = async (listId, name) => {
    try {
      await wishlistApi.remove(listId)
      showToast({ type: 'success', title: 'Lista obrisana', message: `"${name}" je uklonjena.` })
      load()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Lista nije obrisana.' })
    }
  }

  const handleRemoveItem = async (listId, itemId) => {
    try {
      await wishlistApi.removeItem(listId, itemId)
      setItemsByList((prev) => ({
        ...prev,
        [listId]: prev[listId].filter((x) => x.item.id !== itemId)
      }))
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Stavka nije uklonjena.' })
    }
  }

  if (loading) return <Spinner label="Učitavanje lista želja..." size="lg" />
  if (error) return <ErrorState message={error} onRetry={load} />

  return (
    <div className="wishlist-page">
      <h1>❤️ Moje liste želja</h1>

      <form className="wishlist-create" onSubmit={handleCreate}>
        <input
          type="text"
          placeholder="Naziv nove liste (npr. Ljeto 2026)"
          value={newListName}
          onChange={(e) => setNewListName(e.target.value)}
          disabled={creating}
        />
        <button type="submit" disabled={creating || !newListName.trim()} className="btn-primary">
          {creating ? 'Kreiram...' : '+ Nova lista'}
        </button>
      </form>

      {wishlists.length === 0 ? (
        <div className="no-data">
          Nemate liste želja. Kreirajte jednu gore, pa dodajte smještaje klikom na ❤️ u listi smještaja.
        </div>
      ) : (
        wishlists.map((list) => (
          <section key={list.id} className="wishlist-section">
            <div className="wishlist-section-header">
              <h2>{list.name}</h2>
              <button className="wishlist-delete-btn" onClick={() => handleDeleteList(list.id, list.name)}>
                🗑 Obriši listu
              </button>
            </div>

            {(itemsByList[list.id] || []).length === 0 ? (
              <p className="wishlist-empty">Lista je prazna.</p>
            ) : (
              <div className="wishlist-items">
                {(itemsByList[list.id] || []).map(({ item, property }) => (
                  <div key={item.id} className="wishlist-item">
                    {property ? (
                      <>
                        <Link to={`/properties/${property.id}`} className="wishlist-item-info">
                          <strong>{property.name}</strong>
                          <span>📍 {property.city}, {property.country}</span>
                        </Link>
                        <button className="wishlist-item-remove" onClick={() => handleRemoveItem(list.id, item.id)}>
                          ✕
                        </button>
                      </>
                    ) : (
                      <span className="wishlist-item-info">Smještaj #{item.propertyId} (nedostupan)</span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        ))
      )}
    </div>
  )
}
