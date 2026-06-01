import { useState, useEffect } from 'react'
import Modal from '../common/Modal'
import { wishlistApi } from '../../api/wishlistApi'
import { useAuthStore } from '../../store/authStore'
import { useToast } from '../common/ToastProvider'

/**
 * Modal koji se otvori kad gost klikne ❤ na property kartici.
 * Pokazuje sve gostove liste sa checkbox-om — gost vidi u kojim je vec,
 * moze ih dodati u dodatne ili izbaciti, te kreirati novu listu inline.
 */
export default function WishlistPicker({ open, onClose, propertyId, onChanged }) {
  const { user } = useAuthStore()
  const { showToast } = useToast()
  const [lists, setLists] = useState([])
  const [itemsByList, setItemsByList] = useState({}) // listId -> itemId (postoji ako je property u toj listi)
  const [busy, setBusy] = useState(false)
  const [newListName, setNewListName] = useState('')

  useEffect(() => {
    if (!open || !user?.id) return
    let cancelled = false
    setBusy(true)
    wishlistApi.getByGuest(user.id)
      .then(async (ls) => {
        if (cancelled) return
        setLists(ls)
        const map = {}
        await Promise.all(ls.map(async (l) => {
          const items = await wishlistApi.getItems(l.id).catch(() => [])
          const existing = items.find(it => it.propertyId === propertyId)
          if (existing) map[l.id] = existing.id
        }))
        if (!cancelled) setItemsByList(map)
      })
      .catch(() => setLists([]))
      .finally(() => { if (!cancelled) setBusy(false) })
    return () => { cancelled = true }
  }, [open, user?.id, propertyId])

  const toggleList = async (list) => {
    const existingItemId = itemsByList[list.id]
    setBusy(true)
    try {
      if (existingItemId) {
        await wishlistApi.removeItem(list.id, existingItemId)
        setItemsByList(prev => { const n = { ...prev }; delete n[list.id]; return n })
      } else {
        const item = await wishlistApi.addItem(list.id, propertyId)
        setItemsByList(prev => ({ ...prev, [list.id]: item.id }))
      }
      onChanged?.()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Promjena liste nije uspjela.' })
    } finally {
      setBusy(false)
    }
  }

  const createNewList = async () => {
    if (!newListName.trim()) return
    setBusy(true)
    try {
      const created = await wishlistApi.create(user.id, newListName.trim())
      // Odmah dodaj property u novu listu
      const item = await wishlistApi.addItem(created.id, propertyId)
      setLists(prev => [...prev, created])
      setItemsByList(prev => ({ ...prev, [created.id]: item.id }))
      setNewListName('')
      onChanged?.()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Kreiranje liste nije uspjelo.' })
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Sačuvaj u listu želja" size="sm">
      {lists.length === 0 && !busy ? (
        <p style={{ color: 'var(--text-tertiary)', marginBottom: '14px' }}>
          Nemate još nijednu listu želja. Kreirajte prvu ispod.
        </p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '14px' }}>
          {lists.map(list => {
            const checked = !!itemsByList[list.id]
            return (
              <label key={list.id} style={{
                display: 'flex', alignItems: 'center', gap: '10px',
                padding: '8px 10px', borderRadius: '6px',
                background: checked ? 'rgba(139, 92, 246, 0.1)' : 'transparent',
                cursor: busy ? 'wait' : 'pointer'
              }}>
                <input type="checkbox" checked={checked} disabled={busy}
                  onChange={() => toggleList(list)} />
                <span style={{ fontSize: '0.95em' }}>
                  {checked ? '❤️' : '🤍'} <strong>{list.name}</strong>
                </span>
              </label>
            )
          })}
        </div>
      )}

      <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '12px' }}>
        <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.9em' }}>Nova lista:</label>
        <div style={{ display: 'flex', gap: '8px' }}>
          <input type="text" placeholder="npr. Ljeto 2027"
            value={newListName} onChange={(e) => setNewListName(e.target.value)}
            disabled={busy}
            style={{ flex: 1 }} />
          <button type="button" className="btn-primary" disabled={busy || !newListName.trim()}
                  onClick={createNewList}>+ Kreiraj</button>
        </div>
      </div>

      <div className="modal-actions" style={{ marginTop: '14px' }}>
        <button type="button" className="btn-secondary" onClick={onClose}>Gotovo</button>
      </div>
    </Modal>
  )
}
