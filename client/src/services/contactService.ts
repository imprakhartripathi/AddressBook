import api from '../api/axios'
import type { Contact } from '../types/contact'

type ContactPayload = Omit<Contact, 'id'>

export function getContacts() {
  return api.get('/contacts')
}

export function getContact(id: number) {
  return api.get(`/contacts/${id}`)
}

export function createContact(contact: ContactPayload) {
  return api.post('/contacts', contact)
}

export function updateContact(id: number, contact: ContactPayload) {
  return api.put(`/contacts/${id}`, contact)
}

export function deleteContact(id: number) {
  return api.delete(`/contacts/${id}`)
}
