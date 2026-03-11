import { useEffect, useMemo, useState } from 'react'
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
  const [formMode, setFormMode] = useState<FormMode>('create')
  const [activeContact, setActiveContact] = useState<Contact | null>(null)

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

  return (
    <section className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Contacts</h1>
          <p className="text-sm text-slate-500">DB-first CRUD for multiple address books.</p>
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

      <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 md:grid-cols-5">
        <div className="space-y-1">
          <label className="text-xs font-medium uppercase text-slate-500">Address Book</label>
          <select
            value={selectedBook}
            onChange={(event) => setSelectedBook(event.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
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
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
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

      {error && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {error}
        </div>
      )}

      {isLoading ? (
        <div className="rounded-lg border border-slate-200 bg-white p-10 text-center text-slate-500">
          Loading contacts...
        </div>
      ) : (
        <ContactTable contacts={filteredContacts} onEdit={handleEdit} onDelete={handleDelete} />
      )}

      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4">
          <div className="w-full max-w-2xl rounded-xl bg-white p-6 shadow-xl">
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
          </div>
        </div>
      )}
    </section>
  )
}
