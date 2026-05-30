import { useState, useEffect } from 'react'
import { verificationApi } from '../../api/verificationApi'
import { useAuthStore } from '../../store/authStore'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'
import '../../styles/Verification.css'

/**
 * F16 — Admin pregled svih zahtjeva za verifikaciju + approve/reject.
 * Renderuje se kao tab u AdminDashboard-u.
 */
export default function AdminVerifications() {
  const { user } = useAuthStore()
  const { showToast } = useToast()
  const [verifications, setVerifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [processingId, setProcessingId] = useState(null)

  const load = async () => {
    setLoading(true)
    try {
      const data = await verificationApi.getAll()
      setVerifications(data)
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Greška pri učitavanju verifikacija.' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleDecision = async (id, status) => {
    setProcessingId(id)
    try {
      await verificationApi.updateStatus(id, status, user.id)
      showToast({ type: 'success', title: status === 'APPROVED' ? 'Odobreno' : 'Odbijeno',
        message: `Verifikacija #${id} je ${status === 'APPROVED' ? 'odobrena' : 'odbijena'}.` })
      load()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Promjena statusa nije uspjela.' })
    } finally {
      setProcessingId(null)
    }
  }

  const badgeClass = (s) => s === 'APPROVED' ? 'verif-approved' : s === 'REJECTED' ? 'verif-rejected' : 'verif-pending'

  if (loading) return <Spinner label="Učitavanje verifikacija..." />

  return (
    <div className="admin-verifications">
      <h2>Zahtjevi za verifikaciju identiteta</h2>
      {verifications.length === 0 ? (
        <p className="no-data">Nema zahtjeva za verifikaciju.</p>
      ) : (
        <table className="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Korisnik</th>
              <th>Tip dokumenta</th>
              <th>Broj</th>
              <th>Status</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            {verifications.map((v) => (
              <tr key={v.id}>
                <td>#{v.id}</td>
                <td>User #{v.userId}</td>
                <td>{v.documentType}</td>
                <td>{v.documentNumber}</td>
                <td><span className={`verif-badge ${badgeClass(v.status)}`}>{v.status}</span></td>
                <td>
                  {v.status === 'PENDING' ? (
                    <div className="verif-actions">
                      <button
                        className="verif-approve-btn"
                        disabled={processingId === v.id}
                        onClick={() => handleDecision(v.id, 'APPROVED')}
                      >
                        ✅ Odobri
                      </button>
                      <button
                        className="verif-reject-btn"
                        disabled={processingId === v.id}
                        onClick={() => handleDecision(v.id, 'REJECTED')}
                      >
                        ❌ Odbij
                      </button>
                    </div>
                  ) : (
                    <span className="verif-done">—</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
