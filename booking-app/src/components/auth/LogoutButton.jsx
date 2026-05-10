import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

export default function LogoutButton() {
  const navigate = useNavigate()
  const logout = useAuthStore((state) => state.logout)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <button onClick={handleLogout} className="logout-btn">
      Odjava
    </button>
  )
}

