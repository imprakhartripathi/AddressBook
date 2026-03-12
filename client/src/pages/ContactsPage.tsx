import { useEffect, useMemo, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import type { ApiResponse, Contact } from '../types'
import {
  createContact,
  deleteContact,
  getAddressBooks,
  getContacts,
  updateContact,
} from '../services'
import { exportContacts } from '../utils/exportContacts'
import ContactTable from '../components/contacts/ContactTable'
import ContactForm from '../components/contacts/ContactForm'
import Button from '../components/ui/Button'
import Input from '../components/ui/Input'

type FormMode = 'create' | 'edit'
type SortOption = 'name' | 'city' | 'state' | 'zip'

const defaultBook = 'default'

function compareContacts(a: Contact, b: Contact, sortBy: SortOption) {
  const getName = (c: Contact) => `${c.firstName ?? ''} ${c.lastName ?? ''}`.trim().toLowerCase()
  const getValue = (c: Contact) => {
    switch (sortBy) {
      case 'city':
        return (c.city ?? '').toLowerCase()
      case 'state':
        return (c.state ?? '').toLowerCase()
      case 'zip':
        return (c.zip ?? '').toLowerCase()
      default:
        return getName(c)
    }
  }
  return getValue(a).localeCompare(getValue(b))
}

export default function ContactsPage() {
  const [contacts, setContacts] = useState<Contact[]>([])
  const [addressBooks, setAddressBooks] = useState<string[]>([])
  const [selectedBook, setSelectedBook] = useState(defaultBook)
  const [sortBy, setSortBy] = useState<SortOption>('name')
  const [searchCity, setSearchCity] = useState('')
  const [searchState, setSearchState] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<FormMode>('create')
  const [activeContact, setActiveContact] = useState<Contact | null>(null)
  const [detailsContact, setDetailsContact] = useState<Contact | null>(null)

  const modalTitle = useMemo(() => (formMode === 'edit' ? 'Edit Contact' : 'Add Contact'), [formMode])

  const filteredContacts = useMemo(() => {
    const city = searchCity.trim().toLowerCase()
    const state = searchState.trim().toLowerCase()
    return [...contacts]
      .filter((contact) => {
        const cityMatch = city ? (contact.city ?? '').toLowerCase().includes(city) : true
        const stateMatch = state ? (contact.state ?? '').toLowerCase().includes(state) : true
        return cityMatch && stateMatch
      })
      .sort((a, b) => compareContacts(a, b, sortBy))
  }, [contacts, searchCity, searchState, sortBy])

  const loadBooks = async () => {
    try {
      const response = await getAddressBooks()
      const payload = response.data as ApiResponse<string[]>
      const books = payload.data || []
      setAddressBooks(books.length ? books : [defaultBook])
      if (!books.includes(selectedBook)) {
        setSelectedBook(books[0] || defaultBook)
      }
    } catch {
      setAddressBooks([defaultBook])
    }
  }

  const loadContacts = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await getContacts({ book: selectedBook })
      const payload = response.data as ApiResponse<Contact[]>
      setContacts(payload.data || [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load contacts')
      setContacts([])
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadBooks()
  }, [])

  useEffect(() => {
    loadContacts()
  }, [selectedBook])

  const handleAddClick = () => {
    setFormMode('create')
    setActiveContact(null)
    setIsModalOpen(true)
  }

  const handleEdit = (contact: Contact) => {
    setFormMode('edit')
    setActiveContact(contact)
    setIsModalOpen(true)
  }

  const handleView = (contact: Contact) => {
    setDetailsContact(contact)
    setIsDetailsModalOpen(true)
  }

  const handleDelete = async (contact: Contact) => {
    const confirmed = window.confirm(`Delete ${contact.firstName} ${contact.lastName}?`)
    if (!confirmed) {
      return
    }
    setIsSubmitting(true)
    try {
      await deleteContact(contact.id)
      await loadContacts()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to delete contact')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleSubmit = async (payload: Omit<Contact, 'id'>) => {
    setIsSubmitting(true)
    setError(null)
    try {
      if (formMode === 'edit' && activeContact) {
        await updateContact(activeContact.id, payload)
      } else {
        await createContact(payload, selectedBook)
      }
      setIsModalOpen(false)
      setActiveContact(null)
      await loadContacts()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save contact')
    } finally {
      setIsSubmitting(false)
    }
  }

  const disableActions = isLoading || isSubmitting
  const activeFilterCount = [searchCity, searchState].filter((value) => value.trim().length > 0).length

  return (
    <motion.section
      className="space-y-6"
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, ease: 'easeOut' }}
    >
      <header className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-white/50 bg-white/75 p-5 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-sky-600">Directory</p>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Contacts</h1>
          <p className="text-sm text-slate-500">Manage your contacts across different address books.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button onClick={handleAddClick} disabled={disableActions}>Add Contact</Button>
          <Button
            onClick={() => exportContacts(filteredContacts, selectedBook, 'json')}
            className="bg-slate-700 hover:bg-slate-600"
            disabled={disableActions || filteredContacts.length === 0}
          >
            Export JSON
          </Button>
          <Button
            onClick={() => exportContacts(filteredContacts, selectedBook, 'txt')}
            className="bg-slate-700 hover:bg-slate-600"
            disabled={disableActions || filteredContacts.length === 0}
          >
            Export TXT
          </Button>
          <Button
            onClick={() => exportContacts(filteredContacts, selectedBook, 'pdf')}
            className="bg-slate-700 hover:bg-slate-600"
            disabled={disableActions || filteredContacts.length === 0}
          >
            Export PDF
          </Button>
        </div>
      </header>

      <div className="grid gap-3 md:grid-cols-4">
        <motion.div
          className="rounded-2xl border border-white/60 bg-white/85 p-4 shadow-[0_10px_30px_rgba(15,23,42,0.05)] backdrop-blur"
          whileHover={{ y: -2 }}
        >
          <p className="text-xs uppercase tracking-[0.14em] text-slate-500">Current Book</p>
          <p className="mt-1 truncate text-lg font-semibold text-slate-900">{selectedBook}</p>
        </motion.div>
        <motion.div
          className="rounded-2xl border border-white/60 bg-white/85 p-4 shadow-[0_10px_30px_rgba(15,23,42,0.05)] backdrop-blur"
          whileHover={{ y: -2 }}
        >
          <p className="text-xs uppercase tracking-[0.14em] text-slate-500">Visible Contacts</p>
          <p className="mt-1 text-lg font-semibold text-slate-900">{filteredContacts.length}</p>
        </motion.div>
        <motion.div
          className="rounded-2xl border border-white/60 bg-white/85 p-4 shadow-[0_10px_30px_rgba(15,23,42,0.05)] backdrop-blur"
          whileHover={{ y: -2 }}
        >
          <p className="text-xs uppercase tracking-[0.14em] text-slate-500">Active Filters</p>
          <p className="mt-1 text-lg font-semibold text-slate-900">{activeFilterCount}</p>
        </motion.div>
        <motion.div
          className="rounded-2xl border border-white/60 bg-white/85 p-4 shadow-[0_10px_30px_rgba(15,23,42,0.05)] backdrop-blur"
          whileHover={{ y: -2 }}
        >
          <p className="text-xs uppercase tracking-[0.14em] text-slate-500">Sort Mode</p>
          <p className="mt-1 text-lg font-semibold capitalize text-slate-900">{sortBy}</p>
        </motion.div>
      </div>

      <div className="grid gap-3 rounded-2xl border border-white/60 bg-white/85 p-4 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur md:grid-cols-5">
        <div className="space-y-1">
          <label className="text-xs font-medium uppercase text-slate-500">Address Book</label>
          <select
            value={selectedBook}
            onChange={(event) => setSelectedBook(event.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-sky-300 focus:outline-none focus:ring-2 focus:ring-sky-100 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500"
            disabled={disableActions}
          >
            {(addressBooks.length ? addressBooks : [defaultBook]).map((book) => (
              <option key={book} value={book}>
                {book}
              </option>
            ))}
          </select>
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium uppercase text-slate-500">Sort By</label>
          <select
            value={sortBy}
            onChange={(event) => setSortBy(event.target.value as SortOption)}
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-sky-300 focus:outline-none focus:ring-2 focus:ring-sky-100 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500"
            disabled={disableActions}
          >
            <option value="name">Name</option>
            <option value="city">City</option>
            <option value="state">State</option>
            <option value="zip">Zip</option>
          </select>
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium uppercase text-slate-500">Search City</label>
          <Input
            value={searchCity}
            onChange={(event) => setSearchCity(event.target.value)}
            placeholder="City"
            disabled={disableActions}
          />
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium uppercase text-slate-500">Search State</label>
          <Input
            value={searchState}
            onChange={(event) => setSearchState(event.target.value)}
            placeholder="State"
            disabled={disableActions}
          />
        </div>
        <div className="flex items-end gap-2">
          <Button
            onClick={() => {
              setSearchCity('')
              setSearchState('')
            }}
            className="w-full bg-slate-200 text-slate-700 hover:bg-slate-300"
            disabled={disableActions}
          >
            Reset
          </Button>
        </div>
      </div>

      <AnimatePresence>
        {error && (
          <motion.div
            className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
          >
            {error}
          </motion.div>
        )}
      </AnimatePresence>

      {isLoading ? (
        <div className="rounded-2xl border border-white/60 bg-white/85 p-10 text-center text-slate-500 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur">
          Loading contacts...
        </div>
      ) : (
        <ContactTable contacts={filteredContacts} onView={handleView} onEdit={handleEdit} onDelete={handleDelete} />
      )}

      <AnimatePresence>
        {isDetailsModalOpen && detailsContact && (
          <motion.div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div
              className="w-full max-w-xl rounded-2xl border border-white/70 bg-white p-6 shadow-2xl"
              initial={{ opacity: 0, y: 18, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 12, scale: 0.98 }}
            >
              <div className="flex items-center justify-between border-b border-slate-200 pb-4">
                <h2 className="text-lg font-semibold text-slate-900">Contact Details</h2>
                <button
                  type="button"
                  onClick={() => setIsDetailsModalOpen(false)}
                  className="text-sm text-slate-500 hover:text-slate-700"
                >
                  Close
                </button>
              </div>
              <div className="mt-4 grid gap-4 text-sm">
                <div>
                  <p className="text-xs uppercase tracking-[0.12em] text-slate-500">Name</p>
                  <p className="text-base font-semibold text-slate-900">
                    {detailsContact.firstName} {detailsContact.lastName}
                  </p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.12em] text-slate-500">Email</p>
                  <a
                    href={`mailto:${detailsContact.email}`}
                    className="text-sky-700 hover:underline"
                  >
                    {detailsContact.email}
                  </a>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.12em] text-slate-500">Phone</p>
                  <a
                    href={`tel:${detailsContact.phone}`}
                    className="text-sky-700 hover:underline"
                  >
                    {detailsContact.phone}
                  </a>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.12em] text-slate-500">Address</p>
                  <p className="text-slate-700">{detailsContact.address || '-'}</p>
                </div>
                <div className="grid gap-3 sm:grid-cols-3">
                  <div>
                    <p className="text-xs uppercase tracking-[0.12em] text-slate-500">City</p>
                    <p className="text-slate-700">{detailsContact.city || '-'}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-[0.12em] text-slate-500">State</p>
                    <p className="text-slate-700">{detailsContact.state || '-'}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-[0.12em] text-slate-500">Zip</p>
                    <p className="text-slate-700">{detailsContact.zip || '-'}</p>
                  </div>
                </div>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {isModalOpen && (
          <motion.div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div
              className="w-full max-w-2xl rounded-2xl border border-white/70 bg-white p-6 shadow-2xl"
              initial={{ opacity: 0, y: 18, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 12, scale: 0.98 }}
            >
              <div className="flex items-center justify-between border-b border-slate-200 pb-4">
                <h2 className="text-lg font-semibold text-slate-900">{modalTitle}</h2>
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="text-sm text-slate-500 hover:text-slate-700"
                  disabled={isSubmitting}
                >
                  Close
                </button>
              </div>
              <div className="pt-4">
                <ContactForm
                  initialContact={activeContact}
                  onSubmit={handleSubmit}
                  onCancel={() => setIsModalOpen(false)}
                  loading={isSubmitting}
                />
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.section>
  )
}
