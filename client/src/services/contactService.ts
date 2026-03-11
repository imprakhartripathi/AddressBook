import api from '../api/axios'
import type { Contact } from '../types/contact'

type ContactPayload = Omit<Contact, 'id'>
type ContactListParams = {
  book?: string
}

export function getContacts(params?: ContactListParams) {
  return api.get('/db/contacts', { params })
}

export function getContact(id: number) {
  return api.get(`/db/contacts/${id}`)
}

export function createContact(contact: ContactPayload, book?: string) {
  return api.post('/db/contacts', contact, { params: book ? { book } : {} })
}

export function updateContact(id: number, contact: ContactPayload) {
  return api.put(`/db/contacts/${id}`, contact)
}

export function deleteContact(id: number) {
  return api.delete(`/db/contacts/${id}`)
}

export function createBulkContacts(contacts: ContactPayload[], book?: string, async = false) {
  const params: Record<string, string | boolean> = { async }
  if (book) {
    params.book = book
  }
  return api.post('/db/contacts/bulk', contacts, { params })
}
