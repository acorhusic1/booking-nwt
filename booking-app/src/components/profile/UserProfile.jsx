import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { useAuthStore } from '../../store/authStore'
import { userApi } from '../../api/userApi'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'
import '../../styles/UserProfile.css'

const profileSchema = z.object({
  firstName: z.string().min(2, 'Ime mora imati najmanje 2 karaktera'),
  lastName: z.string().min(2, 'Prezime mora imati najmanje 2 karaktera'),
  phone: z.string().min(6, 'Neispravan broj telefona').optional().or(z.literal(''))
})

export default function UserProfile() {
  const { user, setAuth, token } = useAuthStore()
  const { showToast } = useToast()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      phone: ''
    }
  })

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await userApi.getById(user.id)
        setProfile(data)
        reset({
          firstName: data.firstName || '',
          lastName: data.lastName || '',
          phone: data.phone || ''
        })
      } catch {
        showToast({ type: 'error', title: 'Greška', message: 'Greška pri učitavanju profila' })
      } finally {
        setLoading(false)
      }
    }
    if (user?.id) fetchProfile()
  }, [user?.id, reset, showToast])

  const onSubmit = async (formData) => {
    setSaving(true)
    try {
      const updated = await userApi.patch(user.id, formData)
      setProfile(updated)
      setAuth({ ...user, ...updated }, token)
      setEditing(false)
      showToast({ type: 'success', title: 'Uspjeh', message: 'Profil uspješno ažuriran!' })
    } catch (err) {
      showToast({ type: 'error', title: 'Greška', message: err.response?.data?.message || 'Greška pri ažuriranju profila' })
    } finally {
      setSaving(false)
    }
  }

  const handleCancel = () => {
    reset({
      firstName: profile?.firstName || '',
      lastName: profile?.lastName || '',
      phone: profile?.phone || ''
    })
    setEditing(false)
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
          <form className="profile-edit-form" onSubmit={handleSubmit(onSubmit)}>
            <div className="form-group">
              <label>Ime:</label>
              <input
                {...register('firstName')}
                id="profile-firstName"
                className={errors.firstName ? 'error' : ''}
              />
              {errors.firstName && <span className="error-message" style={{color: 'var(--error-text)', fontSize: '0.85rem', marginTop: '4px'}}>{errors.firstName.message}</span>}
            </div>
            <div className="form-group">
              <label>Prezime:</label>
              <input
                {...register('lastName')}
                id="profile-lastName"
                className={errors.lastName ? 'error' : ''}
              />
              {errors.lastName && <span className="error-message" style={{color: 'var(--error-text)', fontSize: '0.85rem', marginTop: '4px'}}>{errors.lastName.message}</span>}
            </div>
            <div className="form-group">
              <label>Telefon:</label>
              <input
                {...register('phone')}
                id="profile-phone"
                className={errors.phone ? 'error' : ''}
              />
              {errors.phone && <span className="error-message" style={{color: 'var(--error-text)', fontSize: '0.85rem', marginTop: '4px'}}>{errors.phone.message}</span>}
            </div>
            <div className="form-actions">
              <button type="submit" disabled={saving} className="save-btn" id="profile-save-btn">
                {saving ? 'Spremanje...' : '💾 Spremi'}
              </button>
              <button type="button" onClick={handleCancel} className="cancel-btn">Otkaži</button>
            </div>
          </form>
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
