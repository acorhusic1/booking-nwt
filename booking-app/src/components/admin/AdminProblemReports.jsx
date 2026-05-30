import { useState, useEffect } from 'react'
import { problemReportApi } from '../../api/problemReportApi'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'

/**
 * F17 — Admin pregled svih prijava problema sa mogućnošću promjene statusa.
 */
const STATUSES = ['REPORTED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
const STATUS_LABELS = {
  REPORTED: '🆕 Prijavljen',
  IN_PROGRESS: '⏳ U obradi',
  RESOLVED: '✅ Riješen',
  CLOSED: '🔒 Zatvoren'
}

export default function AdminProblemReports() {
  const { showToast } = useToast()
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [processingId, setProcessingId] = useState(null)

  const load = async () => {
    setLoading(true)
    try {
      const data = await problemReportApi.getAll()
      setReports(data)
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Greška pri učitavanju prijava.' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

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

  return (
    <div className="admin-problem-reports">
      <h2>Prijave problema</h2>
      {reports.length === 0 ? (
        <p className="no-data">Nema prijava problema.</p>
      ) : (
        <table className="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Rezervacija</th>
              <th>Prijavio</th>
              <th>Kategorija</th>
              <th>Opis</th>
              <th>Status</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            {reports.map((r) => (
              <tr key={r.id}>
                <td>#{r.id}</td>
                <td>#{r.reservationId}</td>
                <td>User #{r.reporterId}</td>
                <td>{r.category}</td>
                <td style={{ maxWidth: '300px', whiteSpace: 'normal' }}>{r.description}</td>
                <td>{STATUS_LABELS[r.status] || r.status}</td>
                <td>
                  <select
                    value={r.status}
                    onChange={(e) => changeStatus(r.id, e.target.value)}
                    disabled={processingId === r.id}
                  >
                    {STATUSES.map((s) => (
                      <option key={s} value={s}>{STATUS_LABELS[s]}</option>
                    ))}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
