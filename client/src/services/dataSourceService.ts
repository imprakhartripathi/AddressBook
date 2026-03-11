import api from '../api/axios'

export function getDataSources() {
  return api.get('/data-sources')
}

export function transferDataSource(from: string, to: string, book?: string) {
  const params: Record<string, string> = { from, to }
  if (book) {
    params.book = book
  }
  return api.post('/data-sources/transfer', null, { params })
}
