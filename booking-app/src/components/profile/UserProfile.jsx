import { useState, useEffect } from 'react'
import { useAuthStore } from '../../store/authStore'
import { userApi } from '../../api/userApi'
import Spinner from '../common/Spinner'
import '../../styles/UserProfile.css'

export default function UserProfile() {
  const { user, setAuth, token } = useAuthStore()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: ''
  })

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await userApi.getById(user.id)
        setProfile(data)
        setFormData({
          firstName: data.firstName || '',
          lastName: data.lastName || '',
          phone: data.phone || ''
        })
      } catch {
        setError('Greška pri učitavanju profila')
      } finally {
        setLoading(false)
      }
    }
    if (user?.id) fetchProfile()
  }, [user?.id])

  const handleSave = async () => {
    setSaving(true)
    setError(null)
    setSuccess(null)
    try {
      const updated = await userApi.patch(user.id, formData)
      setProfile(updated)
      setAuth({ ...user, ...updated }, token)
      setEditing(false)
      setSuccess('Profil uspješno ažuriran!')
      setTimeout(() => setSuccess(null), 3000)
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri ažuriranju profila')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <Spinner label="Učitavanje profila..." size="lg" />

  return (
    <div className="profile-page">
      <div className="profile-header">
        <div className="avatar-circle">
          {profile?.firstName?.charAt(0)}{profile?.lastName?.charAt(0)}
        </div>
        <div>
          <h1>{profile?.firstName} {profile?.lastName}</h1>
          <span className={`role-badge role-${user?.role?.toLowerCase()}`}>{user?.role}</span>
        </div>
      </div>

      {success && <div className="success-alert">✅ {success}</div>}
      {error && <div className="error-alert">❌ {error}</div>}

      <div className="profile-card glass-panel">
        <div className="profile-card-header">
          <h2>Lični podaci</h2>
          {!editing && (
            <button onClick={() => setEditing(true)} className="edit-btn" id="profile-edit-btn">
              ✏️ Uredi
            </button>
          )}
        </div>

        {editing ? (
          <div className="profile-edit-form">
            <div className="form-group">
              <label>Ime:</label>
              <input
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                id="profile-firstName"
              />
            </div>
            <div className="form-group">
              <label>Prezime:</label>
              <input
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                id="profile-lastName"
              />
            </div>
            <div className="form-group">
              <label>Telefon:</label>
              <input
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                id="profile-phone"
              />
            </div>
            <div className="form-actions">
              <button onClick={handleSave} disabled={saving} className="save-btn" id="profile-save-btn">
                {saving ? 'Spremanje...' : '💾 Spremi'}
              </button>
              <button onClick={() => setEditing(false)} className="cancel-btn">Otkaži</button>
            </div>
          </div>
        ) : (
          <div className="profile-info">
            <div className="info-row">
              <span className="info-label">📧 Email</span>
              <span className="info-value">{profile?.email}</span>
            </div>
            <div className="info-row">
              <span className="info-label">👤 Ime</span>
              <span className="info-value">{profile?.firstName} {profile?.lastName}</span>
            </div>
            <div className="info-row">
              <span className="info-label">📱 Telefon</span>
              <span className="info-value">{profile?.phone || 'Nije postavljeno'}</span>
            </div>
            <div className="info-row">
              <span className="info-label">🔑 Uloga</span>
              <span className="info-value">{profile?.role}</span>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
