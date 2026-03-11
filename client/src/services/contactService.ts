import api from '../api/axios'
import type { Contact } from '../types/contact'

type ContactPayload = Omit<Contact, 'id'>
type ContactListParams = {
  book?: string
  sortBy?: 'name' | 'city' | 'state' | 'zip'
}
type SearchParams = { city?: string; state?: string }

export function getContacts(params?: ContactListParams) {
  return api.get('/contacts', { params })
}

export function getContact(id: number, book?: string) {
  return api.get(`/contacts/${id}`, { params: book ? { book } : {} })
}

export function createContact(contact: ContactPayload, book?: string) {
  return api.post('/contacts', contact, { params: book ? { book } : {} })
}

export function updateContact(id: number, contact: ContactPayload, book?: string) {
  return api.put(`/contacts/${id}`, contact, { params: book ? { book } : {} })
}

export function deleteContact(id: number, book?: string) {
  return api.delete(`/contacts/${id}`, { params: book ? { book } : {} })
}

export function searchContacts(params: SearchParams) {
  return api.get('/contacts/search', { params })
}

export function getCountsByCity() {
  return api.get('/contacts/count-by-city')
}

export function getCountsByState() {
  return api.get('/contacts/count-by-state')
}

export function createBulkContacts(contacts: ContactPayload[], book?: string, async = false) {
  const params: Record<string, string | boolean> = { async }
  if (book) {
    params.book = book
  }
  return api.post('/contacts/bulk', contacts, { params })
}
