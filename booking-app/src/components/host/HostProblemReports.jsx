import { useState, useEffect } from 'react'
import { problemReportApi } from '../../api/problemReportApi'
import { useAuthStore } from '../../store/authStore'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'

/**
 * BUG 5 — Host vidi sve prijave problema za svoje smjestaje + moze
 * promijeniti status (REPORTED → IN_PROGRESS → RESOLVED / CLOSED).
 */
const STATUSES = ['REPORTED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
const STATUS_LABELS = {
  REPORTED: '🆕 Prijavljen',
  IN_PROGRESS: '⏳ U obradi',
  RESOLVED: '✅ Riješen',
  CLOSED: '🔒 Zatvoren'
}
const CATEGORY_LABELS = {
  KVAR_UREDJAJA: 'Kvar uređaja',
  CISTOCA: 'Čistoća',
  'NEUSKLAÐENOST_OPISA': 'Neusklađenost sa opisom',
  BUKA: 'Buka',
  SIGURNOST: 'Sigurnost',
  OSTALO: 'Ostalo'
}

export default function HostProblemReports() {
  const { user } = useAuthStore()
  const { showToast } = useToast()
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [processingId, setProcessingId] = useState(null)

  const load = async () => {
    if (!user?.id) return
    setLoading(true)
    try {
      const data = await problemReportApi.getByHost(user.id)
      setReports(data)
    } catch {
      // tiho — sekcija je opciona
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [user?.id])

  const changeStatus = async (id, newStatus) => {
    setProcessingId(id)
    try {
      await problemReportApi.updateStatus(id, newStatus)
      showToast({ type: 'success', title: 'Status promijenjen', message: STATUS_LABELS[newStatus] })
      load()
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Promjena statusa nije uspjela.' })
    } finally {
      setProcessingId(null)
    }
  }

  if (loading) return <Spinner label="Učitavanje prijava..." />
  if (reports.length === 0) return null

  return (
    <section className="host-section glass-panel" style={{ marginTop: '20px' }}>
      <h2>⚠ Prijave problema ({reports.length})</h2>
      <div style={{ overflowX: 'auto' }}>
        <table className="reservations-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Rezervacija</th>
              <th>Kategorija</th>
              <th>Opis</th>
              <th>Status</th>
              <th>Akcija</th>
            </tr>
          </thead>
          <tbody>
            {reports.map(r => (
              <tr key={r.id}>
                <td>#{r.id}</td>
                <td>#{r.reservationId}</td>
                <td>{CATEGORY_LABELS[r.category] || r.category}</td>
                <td style={{ maxWidth: '300px', whiteSpace: 'normal' }}>{r.description}</td>
                <td>{STATUS_LABELS[r.status] || r.status}</td>
                <td>
                  <select
                    value={r.status}
                    onChange={(e) => changeStatus(r.id, e.target.value)}
                    disabled={processingId === r.id}
                    className="admin-select"
                  >
                    {STATUSES.map(s => (
                      <option key={s} value={s}>{STATUS_LABELS[s]}</option>
                    ))}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
