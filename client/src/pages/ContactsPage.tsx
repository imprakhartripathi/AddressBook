import { useEffect, useMemo, useState } from 'react'
import type { ApiResponse, Contact } from '../types'
import {
  createContact,
  deleteContact,
  getAddressBooks,
  getContacts,
  searchContacts,
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

export default function ContactsPage() {
  const [contacts, setContacts] = useState<Contact[]>([])
  const [addressBooks, setAddressBooks] = useState<string[]>([])
  const [selectedBook, setSelectedBook] = useState(defaultBook)
  const [sortBy, setSortBy] = useState<SortOption>('name')
  const [searchCity, setSearchCity] = useState('')
  const [searchState, setSearchState] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<FormMode>('create')
  const [activeContact, setActiveContact] = useState<Contact | null>(null)

  const modalTitle = useMemo(() => (formMode === 'edit' ? 'Edit Contact' : 'Add Contact'), [formMode])

  const loadBooks = async () => {
    try {
      const response = await getAddressBooks()
      const payload = response.data as ApiResponse<string[]>
      const books = payload.data || []
      setAddressBooks(books)
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
      const response = await getContacts({ book: selectedBook, sortBy })
      const payload = response.data as ApiResponse<Contact[]>
      setContacts(payload.data || [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load contacts')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadBooks()
  }, [])

  useEffect(() => {
    loadContacts()
  }, [selectedBook, sortBy])

  const handleSearch = async () => {
    if (!searchCity.trim() && !searchState.trim()) {
      await loadContacts()
      return
    }
    setIsLoading(true)
    setError(null)
    try {
      const response = await searchContacts({
        city: searchCity.trim() || undefined,
        state: searchState.trim() || undefined,
      })
      const payload = response.data as ApiResponse<Array<{ contact: Contact; addressBook: string }>>
      const filtered = (payload.data || [])
        .filter((entry) => entry.addressBook === selectedBook)
        .map((entry) => entry.contact)
      setContacts(filtered)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Search failed')
    } finally {
      setIsLoading(false)
    }
  }

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
    try {
      await deleteContact(contact.id, selectedBook)
      await loadContacts()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to delete contact')
    }
  }

  const handleSubmit = async (payload: Omit<Contact, 'id'>) => {
    setIsLoading(true)
    setError(null)
    try {
      if (formMode === 'edit' && activeContact) {
        await updateContact(activeContact.id, payload, selectedBook)
      } else {
        await createContact(payload, selectedBook)
      }
      setIsModalOpen(false)
      setActiveContact(null)
      await loadContacts()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save contact')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <section className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Contacts</h1>
          <p className="text-sm text-slate-500">Book-aware CRUD with search and sort.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button onClick={handleAddClick}>Add Contact</Button>
          <Button
            onClick={() => exportContacts(contacts, selectedBook, 'json')}
            className="bg-slate-700 hover:bg-slate-600"
          >
            Export JSON
          </Button>
          <Button
            onClick={() => exportContacts(contacts, selectedBook, 'txt')}
            className="bg-slate-700 hover:bg-slate-600"
          >
            Export TXT
          </Button>
          <Button
            onClick={() => exportContacts(contacts, selectedBook, 'pdf')}
            className="bg-slate-700 hover:bg-slate-600"
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
          >
            <option value="name">Name</option>
            <option value="city">City</option>
            <option value="state">State</option>
            <option value="zip">Zip</option>
          </select>
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium uppercase text-slate-500">Search City</label>
          <Input value={searchCity} onChange={(event) => setSearchCity(event.target.value)} placeholder="City" />
        </div>
        <div className="space-y-1">
          <label className="text-xs font-medium uppercase text-slate-500">Search State</label>
          <Input value={searchState} onChange={(event) => setSearchState(event.target.value)} placeholder="State" />
        </div>
        <div className="flex items-end gap-2">
          <Button onClick={handleSearch} className="w-full">Search</Button>
          <Button
            onClick={() => {
              setSearchCity('')
              setSearchState('')
              loadContacts()
            }}
            className="w-full bg-slate-200 text-slate-700 hover:bg-slate-300"
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
        <ContactTable contacts={contacts} onEdit={handleEdit} onDelete={handleDelete} />
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
              >
                Close
              </button>
            </div>
            <div className="pt-4">
              <ContactForm
                initialContact={activeContact}
                onSubmit={handleSubmit}
                onCancel={() => setIsModalOpen(false)}
                loading={isLoading}
              />
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
