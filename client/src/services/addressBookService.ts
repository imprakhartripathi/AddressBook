import api from '../api/axios'

export function getAddressBooks() {
  return api.get('/address-books')
}

export function createAddressBook(name: string) {
  return api.post('/address-books', null, { params: { name } })
}
