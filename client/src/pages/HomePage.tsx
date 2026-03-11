import { useEffect, useMemo, useState } from 'react'
import { motion } from 'framer-motion'
import type { ApiResponse, Contact } from '../types'
import { createAddressBook, getAddressBooks, getContacts } from '../services'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import Input from '../components/ui/Input'

export default function HomePage() {
  const [contacts, setContacts] = useState<Contact[]>([])
  const [addressBooks, setAddressBooks] = useState<string[]>([])
  const [bookName, setBookName] = useState('')
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [creatingBook, setCreatingBook] = useState(false)

  const totalContacts = contacts.length
  const recentContacts = useMemo(() => contacts.slice(-5).reverse(), [contacts])

  const loadDashboard = async () => {
    setLoading(true)
    setError(null)
    try {
      const [contactsResponse, booksResponse] = await Promise.all([getContacts(), getAddressBooks()])
      const contactsPayload = contactsResponse.data as ApiResponse<Contact[]>
      const booksPayload = booksResponse.data as ApiResponse<string[]>
      setContacts(contactsPayload.data || [])
      setAddressBooks(booksPayload.data || [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load dashboard data')
      setContacts([])
      setAddressBooks([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDashboard()
  }, [])

  const handleCreateAddressBook = async () => {
    if (!bookName.trim()) {
      setError('Address book name is required')
      return
    }
    setCreatingBook(true)
    setError(null)
    try {
      await createAddressBook(bookName.trim())
      setBookName('')
      setStatus('Address book created')
      await loadDashboard()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create address book')
    } finally {
      setCreatingBook(false)
    }
  }

  return (
    <motion.section
      className="space-y-6"
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, ease: 'easeOut' }}
    >
      <div className="rounded-2xl border border-white/60 bg-white/80 p-6 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur">
        <p className="text-sm font-semibold uppercase tracking-[0.18em] text-sky-600">AddressBook System</p>
        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900">Welcome to Address Book Program</h1>
        <p className="mt-2 text-slate-600">
          Home Page - Overview
        </p>
      </div>

      {error && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {error}
        </div>
      )}

      {status && !error && (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          {status}
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <div className="space-y-6">
          <div className="grid gap-4 sm:grid-cols-2">
            <Card className="space-y-2">
              <p className="text-sm text-slate-500">Total Contacts</p>
              <p className="text-3xl font-extrabold text-slate-900">{totalContacts}</p>
            </Card>
            <Card className="space-y-2">
              <p className="text-sm text-slate-500">Address Books</p>
              <p className="text-3xl font-extrabold text-slate-900">{addressBooks.length}</p>
            </Card>
          </div>

          <Card className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-900">Recent Contacts</h2>
              <span className="text-xs text-slate-500">Last 5 added</span>
            </div>
            {loading ? (
              <p className="text-sm text-slate-500">Loading...</p>
            ) : recentContacts.length === 0 ? (
              <p className="text-sm text-slate-500">No contacts yet.</p>
            ) : (
              <div className="space-y-3">
                {recentContacts.map((contact) => (
                  <div key={contact.id} className="flex items-center justify-between rounded-xl border border-slate-100 bg-white/70 px-3 py-2 text-sm">
                    <div>
                      <p className="font-medium text-slate-900">
                        {contact.firstName} {contact.lastName}
                      </p>
                      <p className="text-slate-500">{contact.email}</p>
                    </div>
                    <div className="text-slate-500">{contact.city ?? '-'}</div>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>

        <div className="space-y-6">
          <Card className="space-y-4">
            <h2 className="text-lg font-semibold text-slate-900">Create Address Book</h2>
            <div className="space-y-2">
              <Input
                value={bookName}
                onChange={(event) => setBookName(event.target.value)}
                placeholder="Address book name"
                disabled={loading || creatingBook}
              />
              <Button onClick={handleCreateAddressBook} disabled={loading || creatingBook}>
                {creatingBook ? 'Creating...' : 'Add Address Book'}
              </Button>
            </div>
          </Card>

          <Card className="space-y-4">
            <h2 className="text-lg font-semibold text-slate-900">Your Address Books</h2>
            {loading ? (
              <p className="text-sm text-slate-500">Loading...</p>
            ) : addressBooks.length === 0 ? (
              <p className="text-sm text-slate-500">No address books yet.</p>
            ) : (
              <ul className="space-y-2 text-sm text-slate-700">
                {addressBooks.map((book) => (
                  <li key={book} className="rounded-xl border border-slate-200 bg-white/70 px-3 py-2">
                    {book}
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
      </div>
    </motion.section>
  )
}
