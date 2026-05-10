import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import * as z from 'zod'
import { authApi } from '../../api/authApi'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
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
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const onSubmit = async (data) => {
    setLoading(true)
    setError(null)
    try {
      const response = await authApi.login(data.email, data.password)
      setAuth(
        { email: response.email, role: response.role, id: response.id },
        response.accessToken
      )
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri prijavi')
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

      {error && <div className="error-alert">{error}</div>}

      <button type="submit" disabled={loading}>
        {loading ? 'Prijavljivanje...' : 'Prijavi se'}
      </button>
    </form>
  )
}

