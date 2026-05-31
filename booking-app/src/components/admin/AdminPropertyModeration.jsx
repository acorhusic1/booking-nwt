import { useState, useEffect } from 'react'
import { propertyApi } from '../../api/propertyApi'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'

/**
 * F2 — Admin pregled smjestaja na cekanju moderacije + odobri/odbij.
 * Tek nakon APPROVED smjestaj postaje vidljiv na public /properties listi.
 */
export default function AdminPropertyModeration() {
  const { showToast } = useToast()
  const [pending, setPending] = useState([])
  const [loading, setLoading] = useState(true)
  const [processingId, setProcessingId] = useState(null)

  const load = async () => {
    setLoading(true)
    try {
      const data = await propertyApi.getPendingModeration()
      setPending(Array.isArray(data) ? data : [])
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Nije moguće dohvatiti listu na čekanju.' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const decide = async (id, status) => {
    setProcessingId(id)
    try {
      await propertyApi.moderate(id, status)
      showToast({
        type: 'success',
        title: status === 'APPROVED' ? 'Odobreno' : 'Odbijeno',
        message: `Smještaj #${id} je ${status === 'APPROVED' ? 'odobren i sada je javno vidljiv' : 'odbijen'}.`
      })
      load()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Promjena statusa nije uspjela.' })
    } finally {
      setProcessingId(null)
    }
  }

  if (loading) return <Spinner label="Učitavanje smještaja na čekanju..." />

  return (
    <div className="admin-property-moderation">
      <h2>🛡 Smještaji na moderaciji</h2>
      {pending.length === 0 ? (
        <p className="no-data">Nema smještaja na čekanju.</p>
      ) : (
        <table className="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Naziv</th>
              <th>Host</th>
              <th>Lokacija</th>
              <th>Maks. gostiju</th>
              <th>Pravila</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            {pending.map(p => (
              <tr key={p.id}>
                <td>#{p.id}</td>
                <td>{p.name}</td>
                <td>#{p.hostId}</td>
                <td>{p.city}, {p.country}</td>
                <td>{p.maxGuests}</td>
                <td style={{ fontSize: '0.85em' }}>
                  {p.ruleNoSmoking && '🚭 '}
                  {p.rulePetsAllowed && '🐾 '}
                  {p.rulePartiesAllowed && '🎉 '}
                  {p.ruleChildrenAllowed && '👶 '}
                </td>
                <td>
                  <div className="verif-actions">
                    <button
                      className="verif-approve-btn"
                      disabled={processingId === p.id}
                      onClick={() => decide(p.id, 'APPROVED')}
                    >✅ Odobri</button>
                    <button
                      className="verif-reject-btn"
                      disabled={processingId === p.id}
                      onClick={() => decide(p.id, 'REJECTED')}
                    >❌ Odbij</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
