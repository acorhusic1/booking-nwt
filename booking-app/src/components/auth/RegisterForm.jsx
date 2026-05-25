import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { authApi } from '../../api/authApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useToast } from '../common/ToastProvider'
import '../../styles/RegisterForm.css'

const registerSchema = z.object({
  firstName: z.string().min(2, 'Ime mora imati najmanje 2 karaktera'),
  lastName: z.string().min(2, 'Prezime mora imati najmanje 2 karaktera'),
  email: z.string().email('Nevaljani email'),
  password: z.string().min(6, 'Lozinka mora imati najmanje 6 karaktera'),
  confirmPassword: z.string(),
  phone: z.string().min(6, 'Neispravan broj telefona'),
  role: z.enum(['GUEST', 'HOST'], { required_error: 'Odaberite ulogu' })
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Lozinke se ne podudaraju',
  path: ['confirmPassword']
})

export default function RegisterForm() {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: { role: 'GUEST' }
  })
  const setAuth = useAuthStore((state) => state.setAuth)
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(false)

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      const { confirmPassword, ...userData } = data
      await authApi.register(userData)
      // Auto-login after successful registration
      const loginResponse = await authApi.login(data.email, data.password)
      setAuth(
        { email: loginResponse.email, role: loginResponse.role, id: loginResponse.id },
        loginResponse.accessToken,
        loginResponse.refreshToken
      )
      showToast({ type: 'success', title: 'Registracija uspješna', message: 'Vaš korisnički račun je kreiran!' })
      navigate('/dashboard')
    } catch (err) {
      showToast({ type: 'error', title: 'Greška pri registraciji', message: err.response?.data?.message || 'Pokušajte ponovo' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="register-form" id="register-form">
      <div className="form-row">
        <div className="form-group">
          <label>Ime:</label>
          <input
            {...register('firstName')}
            type="text"
            placeholder="Vaše ime"
            id="register-firstName"
          />
          {errors.firstName && <span className="error-message">{errors.firstName.message}</span>}
        </div>

        <div className="form-group">
          <label>Prezime:</label>
          <input
            {...register('lastName')}
            type="text"
            placeholder="Vaše prezime"
            id="register-lastName"
          />
          {errors.lastName && <span className="error-message">{errors.lastName.message}</span>}
        </div>
      </div>

      <div className="form-group">
        <label>Email:</label>
        <input
          {...register('email')}
          type="email"
          placeholder="vasaemail@example.com"
          id="register-email"
        />
        {errors.email && <span className="error-message">{errors.email.message}</span>}
      </div>

      <div className="form-group">
        <label>Telefon:</label>
        <input
          {...register('phone')}
          type="tel"
          placeholder="+387 61 000 000"
          id="register-phone"
        />
        {errors.phone && <span className="error-message">{errors.phone.message}</span>}
      </div>

      <div className="form-row">
        <div className="form-group">
          <label>Lozinka:</label>
          <input
            {...register('password')}
            type="password"
            placeholder="••••••••"
            id="register-password"
          />
          {errors.password && <span className="error-message">{errors.password.message}</span>}
        </div>

        <div className="form-group">
          <label>Potvrdi lozinku:</label>
          <input
            {...register('confirmPassword')}
            type="password"
            placeholder="••••••••"
            id="register-confirmPassword"
          />
          {errors.confirmPassword && <span className="error-message">{errors.confirmPassword.message}</span>}
        </div>
      </div>

      <div className="form-group">
        <label>Uloga:</label>
        <div className="role-selector">
          <label className="role-option">
            <input {...register('role')} type="radio" value="GUEST" id="role-guest" />
            <span className="role-card">
              <span className="role-icon">🧳</span>
              <span className="role-name">Gost</span>
              <span className="role-desc">Tražim smještaj</span>
            </span>
          </label>
          <label className="role-option">
            <input {...register('role')} type="radio" value="HOST" id="role-host" />
            <span className="role-card">
              <span className="role-icon">🏠</span>
              <span className="role-name">Domaćin</span>
              <span className="role-desc">Nudim smještaj</span>
            </span>
          </label>
        </div>
        {errors.role && <span className="error-message">{errors.role.message}</span>}
      </div>

      <button type="submit" disabled={loading} id="register-submit">
        {loading ? 'Registracija...' : 'Registruj se'}
      </button>
    </form>
  )
}
