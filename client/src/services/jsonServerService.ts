import api from '../api/axios'
import type { Contact } from '../types/contact'

export function jsonServerPull(book?: string) {
  return api.post('/json-server/pull', null, { params: book ? { book } : {} })
}

export function jsonServerPush() {
  return api.post('/json-server/push')
}

export function jsonServerPushAsync() {
  return api.post('/json-server/push/async')
}

export function jsonServerBulk(contacts: Omit<Contact, 'id'>[], async = false) {
  return api.post('/json-server/bulk', contacts, { params: { async } })
}

export function jsonServerUpdate(id: number, contact: Partial<Contact>, book?: string) {
  return api.put(`/json-server/${id}`, contact, { params: book ? { book } : {} })
}

export function jsonServerDelete(id: number, book?: string) {
  return api.delete(`/json-server/${id}`, { params: book ? { book } : {} })
}
