import { useState, useEffect, useCallback, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { messagesApi } from '../../api/messagesApi'
import { userApi } from '../../api/userApi'
import { propertyApi } from '../../api/propertyApi'
import { useAuthStore } from '../../store/authStore'
import { useToast } from '../common/ToastProvider'
import Spinner from '../common/Spinner'
import ErrorState from '../common/ErrorState'
import '../../styles/Messages.css'

/**
 * F8 — Poruke. Levo lista konverzacija, desno chat aktivne konverzacije.
 * GUEST vidi konverzacije gdje je on guest, HOST gdje je host.
 */
export default function Messages() {
  const { user } = useAuthStore()
  const { showToast } = useToast()
  const isHost = (user?.role || '').toUpperCase() === 'HOST'
  // BUG F — kad gost klikne "Pošalji poruku domaćinu", redirect dolazi sa ?conv=ID.
  // Bez ovog activeId bi uvijek bio prva konverzacija iz liste.
  const [searchParams, setSearchParams] = useSearchParams()
  const preferredConvId = searchParams.get('conv')

  const [conversations, setConversations] = useState([])
  const [activeId, setActiveId] = useState(null)
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadingMsgs, setLoadingMsgs] = useState(false)
  const [error, setError] = useState(null)
  const [draft, setDraft] = useState('')
  const [sending, setSending] = useState(false)
  const messagesEndRef = useRef(null)

  const loadConversations = useCallback(async () => {
    if (!user?.id) return
    setLoading(true)
    setError(null)
    try {
      const data = isHost
        ? await messagesApi.getByHost(user.id)
        : await messagesApi.getByGuest(user.id)

      // BUG 6 — dohvati imena gostiju/hostova + property nazive za pretty display
      const otherUserIds = [...new Set(data.map(c => isHost ? c.guestId : c.hostId).filter(Boolean))]
      const propertyIds = [...new Set(data.map(c => c.propertyId).filter(Boolean))]

      const userNames = {}
      const propertyNames = {}
      await Promise.all([
        ...otherUserIds.map(async (uid) => {
          try {
            const u = await userApi.getById(uid)
            userNames[uid] = `${u.firstName || ''} ${u.lastName || ''}`.trim() || u.email || `User #${uid}`
          } catch {
            userNames[uid] = `User #${uid}`
          }
        }),
        ...propertyIds.map(async (pid) => {
          try {
            const p = await propertyApi.getById(pid)
            propertyNames[pid] = p.name || `Smještaj #${pid}`
          } catch {
            propertyNames[pid] = `Smještaj #${pid}`
          }
        })
      ])

      const enriched = data.map(c => ({
        ...c,
        otherName: isHost ? userNames[c.guestId] : userNames[c.hostId],
        propertyName: propertyNames[c.propertyId]
      }))
      setConversations(enriched)
      if (enriched.length > 0 && !activeId) {
        const preferred = preferredConvId
          ? enriched.find(c => String(c.id) === String(preferredConvId))
          : null
        setActiveId(preferred ? preferred.id : enriched[0].id)
        // Ocisti ?conv= iz URL-a nakon prve upotrebe — refresh stranice ne treba
        // ponovo skakati na tu istu konverzaciju ako je korisnik kliknuo drugu.
        if (preferredConvId) {
          searchParams.delete('conv')
          setSearchParams(searchParams, { replace: true })
        }
      }
    } catch {
      setError('Greška pri učitavanju konverzacija')
    } finally {
      setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id, isHost])

  useEffect(() => { loadConversations() }, [loadConversations])

  const loadMessages = useCallback(async () => {
    if (!activeId) {
      setMessages([])
      return
    }
    setLoadingMsgs(true)
    try {
      const data = await messagesApi.getMessages(activeId)
      setMessages(data)
    } catch {
      // tiho
    } finally {
      setLoadingMsgs(false)
    }
  }, [activeId])

  useEffect(() => { loadMessages() }, [loadMessages])

  // Scroll na dno kad stignu nove poruke
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = async (e) => {
    e.preventDefault()
    if (!draft.trim() || !activeId) return
    setSending(true)
    try {
      const msg = await messagesApi.sendMessage(activeId, user.id, draft.trim())
      setMessages((prev) => [...prev, msg])
      setDraft('')
    } catch {
      showToast({ type: 'error', title: 'Greška', message: 'Poruka nije poslana.' })
    } finally {
      setSending(false)
    }
  }

  if (loading) return <Spinner label="Učitavanje poruka..." size="lg" />
  if (error) return <ErrorState message={error} onRetry={loadConversations} />

  const activeConv = conversations.find((c) => c.id === activeId)

  return (
    <div className="messages-page">
      <h1>💬 Poruke</h1>

      {conversations.length === 0 ? (
        <div className="no-data">
          Nemate aktivnih konverzacija. {isHost
            ? 'Gost otvori konverzaciju iz pregleda smještaja.'
            : 'Konverzaciju otvarate sa stranice smještaja.'}
        </div>
      ) : (
        <div className="messages-layout">
          <aside className="conversations-list">
            <h3>Konverzacije</h3>
            {conversations.map((c) => (
              <button
                key={c.id}
                className={`conv-item ${c.id === activeId ? 'active' : ''}`}
                onClick={() => setActiveId(c.id)}
              >
                <div className="conv-title">
                  {c.otherName || (isHost ? `Gost #${c.guestId}` : `Domaćin #${c.hostId}`)}
                </div>
                <div className="conv-meta">
                  {c.propertyName || `Smještaj #${c.propertyId}`}
                  {c.reservationId ? ` · Rez #${c.reservationId}` : ''}
                </div>
              </button>
            ))}
          </aside>

          <section className="chat-panel">
            {activeConv ? (
              <>
                <header className="chat-header">
                  <strong>{activeConv.otherName || (isHost ? `Gost #${activeConv.guestId}` : `Domaćin #${activeConv.hostId}`)}</strong>
                  <span>{activeConv.propertyName || `Smještaj #${activeConv.propertyId}`}</span>
                </header>

                <div className="chat-messages">
                  {loadingMsgs && <Spinner size="sm" inline label="Učitavanje..." />}
                  {!loadingMsgs && messages.length === 0 && (
                    <p className="chat-empty">Nema poruka. Pošaljite prvu.</p>
                  )}
                  {messages.map((m) => (
                    <div key={m.id} className={`chat-msg ${m.senderId === user.id ? 'mine' : 'theirs'}`}>
                      <div className="chat-msg-content">{m.content}</div>
                      <div className="chat-msg-time">
                        {new Date(m.sentAt).toLocaleString('bs-BA', { dateStyle: 'short', timeStyle: 'short' })}
                      </div>
                    </div>
                  ))}
                  <div ref={messagesEndRef} />
                </div>

                <form className="chat-input-row" onSubmit={handleSend}>
                  <input
                    type="text"
                    placeholder="Napišite poruku..."
                    value={draft}
                    onChange={(e) => setDraft(e.target.value)}
                    disabled={sending}
                  />
                  <button type="submit" className="btn-primary" disabled={sending || !draft.trim()}>
                    {sending ? '...' : 'Pošalji'}
                  </button>
                </form>
              </>
            ) : (
              <p className="no-data">Izaberi konverzaciju iz liste.</p>
            )}
          </section>
        </div>
      )}
    </div>
  )
}
