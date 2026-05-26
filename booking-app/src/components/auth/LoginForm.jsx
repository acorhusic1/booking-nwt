import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { authApi } from '../../api/authApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useToast } from '../common/ToastProvider'
import '../../styles/LoginForm.css'

const loginSchema = z.object({
  email: z.string().email('Nevaljani email'),
  password: z.string().min(6, 'Lozinka mora imati najmanje 6 karaktera')
})

export default function LoginForm() {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(loginSchema)
  })
  const setAuth = useAuthStore((state) => state.setAuth)
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(false)

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      const response = await authApi.login(data.email, data.password)
      setAuth(
        { id: response.id, email: response.email, role: response.role },
        response.accessToken,
        response.refreshToken
      )
      showToast({ type: 'success', title: 'Prijava uspješna', message: 'Dobrodošli nazad!' })
      navigate('/dashboard')
    } catch (err) {
      showToast({ type: 'error', title: 'Greška pri prijavi', message: err.response?.data?.message || 'Provjerite email i lozinku' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="login-form">
      <div className="form-group">
        <label>Email:</label>
        <input
          {...register('email')}
          type="email"
          placeholder="vasaemail@example.com"
          className={errors.email ? 'error' : ''}
        />
        {errors.email && <span className="error-message">{errors.email.message}</span>}
      </div>

      <div className="form-group">
        <label>Lozinka:</label>
        <input
          {...register('password')}
          type="password"
          placeholder="••••••••"
          className={errors.password ? 'error' : ''}
        />
        {errors.password && <span className="error-message">{errors.password.message}</span>}
      </div>

      <button type="submit" disabled={loading}>
        {loading ? 'Prijavljivanje...' : 'Prijavi se'}
      </button>
    </form>
  )
}

