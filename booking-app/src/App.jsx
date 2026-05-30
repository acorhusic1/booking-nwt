import React, { useState } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate, useParams, Link } from 'react-router-dom'
import Header from './components/common/Header'
import ToastProvider from './components/common/ToastProvider'
import LoginForm from './components/auth/LoginForm'
import RegisterForm from './components/auth/RegisterForm'
import PropertyList from './components/properties/PropertyList'
import PropertyDetail from './components/properties/PropertyDetail'
import Dashboard from './components/Dashboard'
import ReservationForm from './components/reservations/ReservationForm'
import UserProfile from './components/profile/UserProfile'
import AdminDashboard from './components/admin/AdminDashboard'
import HostDashboard from './components/host/HostDashboard'
import Wishlist from './components/wishlist/Wishlist'
import Messages from './components/messages/Messages'
import { useAuthStore } from './store/authStore'

function App() {
  const { isAuthenticated, user } = useAuthStore()

  return (
    <Router>
      <ToastProvider>
      <Header />
      <main className="app-main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/properties" element={<PropertiesPage />} />
          <Route path="/properties/:id" element={<PropertyDetailPage />} />
          <Route
            path="/dashboard"
            element={isAuthenticated ? <DashboardPage /> : <Navigate to="/login" />}
          />
          <Route
            path="/reserve/:id"
            element={isAuthenticated ? <ReservationPage /> : <Navigate to="/login" />}
          />
          <Route
            path="/profile"
            element={isAuthenticated ? <ProfilePage /> : <Navigate to="/login" />}
          />
          <Route
            path="/wishlist"
            element={isAuthenticated ? <WishlistPage /> : <Navigate to="/login" />}
          />
          <Route
            path="/messages"
            element={isAuthenticated ? <MessagesPage /> : <Navigate to="/login" />}
          />
          <Route
            path="/admin"
            element={isAuthenticated && user?.role === 'ADMIN' ? <AdminPage /> : <Navigate to="/dashboard" />}
          />
          <Route
            path="/host/dashboard"
            element={isAuthenticated && user?.role === 'HOST' ? <HostPage /> : <Navigate to="/dashboard" />}
          />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      </ToastProvider>
    </Router>
  )
}

function HomePage() {
  return (
    <div className="home-page">
      <section className="hero">
        <h1>Pronađite savršen smještaj za vašu rezervaciju</h1>
        <p>Isplative cijene, kvalitetni smještaji, brza potvrda</p>
        <Link to="/properties" className="cta-button">Pretraži smještaje</Link>
      </section>
    </div>
  )
}

function LoginPage() {
  return (
    <div className="login-page">
      <div className="login-container">
        <h1>Prijava</h1>
        <LoginForm />
        <p>Nemate račun? <Link to="/register">Registrujte se</Link></p>
      </div>
    </div>
  )
}

function RegisterPage() {
  return (
    <div className="login-page">
      <div className="login-container" style={{ maxWidth: '550px' }}>
        <h1>Registracija</h1>
        <RegisterForm />
        <p>Već imate račun? <Link to="/login">Prijavite se</Link></p>
      </div>
    </div>
  )
}

function PropertiesPage() {
  const [filters, setFilters] = useState({})

  return (
    <div className="properties-page">
      <PropertyList filters={filters} />
    </div>
  )
}

function PropertyDetailPage() {
  return <PropertyDetail />
}

function DashboardPage() {
  return <Dashboard />
}

function ReservationPage() {
  const { id } = useParams()
  return (
    <div className="reservation-page">
      <h1>Rezerviši smještaj</h1>
      <ReservationForm propertyId={id} />
    </div>
  )
}

function ProfilePage() {
  return <UserProfile />
}

function WishlistPage() {
  return <Wishlist />
}

function MessagesPage() {
  return <Messages />
}

function AdminPage() {
  return <AdminDashboard />
}

function HostPage() {
  return <HostDashboard />
}

function NotFoundPage() {
  return (
    <div className="not-found">
      <h1>404 - Stranica nije pronađena</h1>
      <Link to="/">Nazad na početnu</Link>
    </div>
  )
}

export default App
