import { useState, useEffect } from 'react'
import { verificationApi } from '../../api/verificationApi'
import { useAuthStore } from '../../store/authStore'
import { useToast } from '../common/ToastProvider'
import '../../styles/Verification.css'

/**
 * F16 — Host predaje zahtjev za verifikaciju identiteta i vidi status.
 * Prikazuje se kao kartica na vrhu Host Dashboard-a.
 */
export default function HostVerification() {
  const { user } = useAuthStore()
  const { showToast } = useToast()
  const [verifications, setVerifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [documentType, setDocumentType] = useState('LIČNA_KARTA')
  const [documentNumber, setDocumentNumber] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const load = async () => {
    if (!user?.id) return
    try {
      const data = await verificationApi.getByUser(user.id)
      setVerifications(data)
    } catch {
      // tiho
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [user?.id])

  const latest = verifications[0]
  const isApproved = verifications.some((v) => v.status === 'APPROVED')
  const isPending = !isApproved && verifications.some((v) => v.status === 'PENDING')

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!documentNumber.trim()) return
    setSubmitting(true)
    try {
      await verificationApi.submit(user.id, documentType, documentNumber.trim())
      showToast({ type: 'success', title: 'Zahtjev poslan', message: 'Vaš zahtjev za verifikaciju čeka odobrenje administratora.' })
      setDocumentNumber('')
      load()
    } catch (err) {
      // Backend ponekad vraća message kao objekt {fieldName: "poruka"} (validation),
      // a Toast očekuje string — bez ove normalizacije puca React #31.
      const raw = err.response?.data?.message
      let msg = 'Slanje nije uspjelo.'
      if (typeof raw === 'string') msg = raw
      else if (raw && typeof raw === 'object') msg = Object.values(raw).join(' ')
      showToast({ type: 'error', title: 'Greška', message: msg })
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return null

  return (
    <section className={`verification-card ${isApproved ? 'verified' : ''}`}>
      <div className="verification-header">
        <h2>🪪 Verifikacija identiteta</h2>
        {isApproved && <span className="verif-badge verif-approved">✅ Verifikovan</span>}
        {isPending && <span className="verif-badge verif-pending">⏳ Na čekanju</span>}
        {!isApproved && !isPending && latest?.status === 'REJECTED' && (
          <span className="verif-badge verif-rejected">❌ Odbijen</span>
        )}
      </div>

      {isApproved ? (
        <p className="verification-hint">
          Vaš identitet je verifikovan — gosti vide oznaku povjerenja na vašim objektima.
        </p>
      ) : isPending ? (
        <p className="verification-hint">
          Zahtjev je predan i čeka pregled administratora. Bit ćete obaviješteni o ishodu.
        </p>
      ) : (
        <>
          <p className="verification-hint">
            Verifikujte identitet prije objave objekata. Administrator pregleda zahtjev.
            {latest?.status === 'REJECTED' && ' Prethodni zahtjev je odbijen — pošaljite novi.'}
          </p>
          <form className="verification-form" onSubmit={handleSubmit}>
            <select value={documentType} onChange={(e) => setDocumentType(e.target.value)} disabled={submitting}>
              <option value="LIČNA_KARTA">Lična karta</option>
              <option value="PASOŠ">Pasoš</option>
              <option value="VOZAČKA_DOZVOLA">Vozačka dozvola</option>
            </select>
            <input
              type="text"
              placeholder="Broj dokumenta"
              value={documentNumber}
              onChange={(e) => setDocumentNumber(e.target.value)}
              disabled={submitting}
            />
            <button type="submit" className="btn-primary" disabled={submitting || !documentNumber.trim()}>
              {submitting ? 'Šaljem...' : 'Pošalji na verifikaciju'}
            </button>
          </form>
        </>
      )}
    </section>
  )
}
