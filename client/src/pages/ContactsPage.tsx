import { useEffect, useMemo, useState } from 'react'
import type { ApiResponse, Contact } from '../types'
import {
  createContact,
  deleteContact,
  getContacts,
  updateContact,
} from '../services'
import ContactTable from '../components/contacts/ContactTable'
import ContactForm from '../components/contacts/ContactForm'
import Button from '../components/ui/Button'

const emptyContacts: Contact[] = []

type FormMode = 'create' | 'edit'

export default function ContactsPage() {
  const [contacts, setContacts] = useState<Contact[]>(emptyContacts)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [formMode, setFormMode] = useState<FormMode>('create')
  const [activeContact, setActiveContact] = useState<Contact | null>(null)

  const modalTitle = useMemo(() => (formMode === 'edit' ? 'Edit Contact' : 'Add Contact'), [formMode])

  const loadContacts = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const response = await getContacts()
      const payload = response.data as ApiResponse<Contact[]>
      setContacts(payload.data || [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load contacts')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadContacts()
  }, [])

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
      await deleteContact(contact.id)
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
        await updateContact(activeContact.id, payload)
      } else {
        await createContact(payload)
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
          <p className="text-sm text-slate-500">Manage your address book in one place.</p>
        </div>
        <Button onClick={handleAddClick}>Add Contact</Button>
      </header>

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
