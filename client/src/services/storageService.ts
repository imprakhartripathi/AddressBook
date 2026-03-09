import api from '../api/axios'

export function exportFile(book?: string) {
  return api.post('/storage/file/export', null, { params: book ? { book } : {} })
}

export function importFile(book?: string) {
  return api.post('/storage/file/import', null, { params: book ? { book } : {} })
}

export function exportCsv(book?: string) {
  return api.post('/storage/csv/export', null, { params: book ? { book } : {} })
}

export function importCsv(book?: string) {
  return api.post('/storage/csv/import', null, { params: book ? { book } : {} })
}

export function exportJson(book?: string) {
  return api.post('/storage/json/export', null, { params: book ? { book } : {} })
}

export function importJson(book?: string) {
  return api.post('/storage/json/import', null, { params: book ? { book } : {} })
}
