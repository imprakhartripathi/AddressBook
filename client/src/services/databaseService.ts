import api from '../api/axios'
import type { Contact } from '../types/contact'

type ContactPayload = Omit<Contact, 'id'>

export function getDbContacts(book?: string) {
  return api.get('/db/contacts', { params: book ? { book } : {} })
}

export function createDbContact(contact: ContactPayload, book?: string) {
  return api.post('/db/contacts', contact, { params: book ? { book } : {} })
}

export function updateDbContact(id: number, contact: ContactPayload) {
  return api.put(`/db/contacts/${id}`, contact)
}

export function deleteDbContact(id: number) {
  return api.delete(`/db/contacts/${id}`)
}

export function bulkDbContacts(contacts: ContactPayload[], book?: string, async = false) {
  const params: Record<string, string | boolean> = { async }
  if (book) {
    params.book = book
  }
  return api.post('/db/contacts/bulk', contacts, { params })
}

export function dbSyncFromMemory(book?: string) {
  return api.post('/db/contacts/sync/from-memory', null, { params: book ? { book } : {} })
}

export function dbSyncToMemory(book?: string) {
  return api.post('/db/contacts/sync/to-memory', null, { params: book ? { book } : {} })
}

export function dbContactsByRange(from: string, to: string) {
  return api.get('/db/contacts/range', { params: { from, to } })
}

export function dbCountByCity(city: string) {
  return api.get('/db/contacts/count', { params: { city } })
}

export function dbCountByState(state: string) {
  return api.get('/db/contacts/count', { params: { state } })
}
