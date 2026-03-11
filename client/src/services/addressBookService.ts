import api from '../api/axios'

export function getAddressBooks() {
  return api.get('/db/address-books')
}

export function createAddressBook(name: string) {
  return api.post('/db/address-books', null, { params: { name } })
}
