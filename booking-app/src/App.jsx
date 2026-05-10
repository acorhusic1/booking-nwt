import React, { useState } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate, useParams, Link } from 'react-router-dom'
import Header from './components/common/Header'
import LoginForm from './components/auth/LoginForm'
import PropertyList from './components/properties/PropertyList'
import PropertyDetail from './components/properties/PropertyDetail'
import Dashboard from './components/Dashboard'
import ReservationForm from './components/reservations/ReservationForm'
import { useAuthStore } from './store/authStore'

function App() {
  const { isAuthenticated } = useAuthStore()

  return (
    <Router>
      <Header />
      <main className="app-main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
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
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
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

function NotFoundPage() {
  return (
    <div className="not-found">
      <h1>404 - Stranica nije pronađena</h1>
      <Link to="/">Nazad na početnu</Link>
    </div>
  )
}

export default App

