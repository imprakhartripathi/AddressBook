import { useEffect, useState } from 'react'
import type { ApiResponse } from '../types'
import {
  dbContactsByRange,
  dbCountByCity,
  dbCountByState,
  dbSyncFromMemory,
  dbSyncToMemory,
  getDataSources,
  jsonServerPull,
  jsonServerPush,
  jsonServerPushAsync,
  transferDataSource,
} from '../services'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import Input from '../components/ui/Input'

export default function IntegrationsPage() {
  const [book, setBook] = useState('default')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [city, setCity] = useState('')
  const [state, setState] = useState('')
  const [sources, setSources] = useState<string[]>([])
  const [sourceFrom, setSourceFrom] = useState('MEMORY')
  const [sourceTo, setSourceTo] = useState('DB')
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const loadSources = async () => {
      try {
        const response = await getDataSources()
        const payload = response.data as ApiResponse<string[]>
        const available = payload.data || []
        setSources(available)
        if (available.length > 1) {
          setSourceFrom(available[0])
          setSourceTo(available[1])
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unable to load data sources')
      }
    }
    loadSources()
  }, [])

  const run = async (action: () => Promise<unknown>, message: string) => {
    setError(null)
    setStatus(null)
    try {
      await action()
      setStatus(message)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Operation failed')
    }
  }

  return (
    <section className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold text-slate-900">Integrations</h1>
        <p className="text-sm text-slate-500">Run DB, file IO, and JSON server sync flows.</p>
      </header>

      {error && <div className="rounded border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</div>}
      {status && <div className="rounded border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{status}</div>}

      <div className="grid gap-4 md:grid-cols-2">
        <Card className="space-y-3">
          <h2 className="text-lg font-semibold">Database Sync</h2>
          <Input value={book} onChange={(event) => setBook(event.target.value)} placeholder="Address book" />
          <div className="flex flex-wrap gap-2">
            <Button onClick={() => run(() => dbSyncFromMemory(book), 'Memory -> DB sync complete')}>Memory to DB</Button>
            <Button onClick={() => run(() => dbSyncToMemory(book), 'DB -> Memory sync complete')}>DB to Memory</Button>
          </div>
        </Card>

        <Card className="space-y-3">
          <h2 className="text-lg font-semibold">JSON Server</h2>
          <div className="flex flex-wrap gap-2">
            <Button onClick={() => run(() => jsonServerPull(book), 'JSON server pull complete')}>Pull</Button>
            <Button onClick={() => run(() => jsonServerPush(), 'JSON server push complete')}>Push</Button>
            <Button onClick={() => run(() => jsonServerPushAsync(), 'JSON server async push started')}>Push Async</Button>
          </div>
        </Card>

        <Card className="space-y-3">
          <h2 className="text-lg font-semibold">DB Analytics</h2>
          <div className="grid gap-2 sm:grid-cols-2">
            <Input type="datetime-local" value={fromDate} onChange={(event) => setFromDate(event.target.value)} />
            <Input type="datetime-local" value={toDate} onChange={(event) => setToDate(event.target.value)} />
          </div>
          <div className="flex flex-wrap gap-2">
            <Button
              onClick={() =>
                run(
                  () => dbContactsByRange(new Date(fromDate).toISOString(), new Date(toDate).toISOString()),
                  'Date-range query completed',
                )
              }
            >
              Query by Date
            </Button>
          </div>
          <div className="grid gap-2 sm:grid-cols-2">
            <Input value={city} onChange={(event) => setCity(event.target.value)} placeholder="City" />
            <Input value={state} onChange={(event) => setState(event.target.value)} placeholder="State" />
          </div>
          <div className="flex flex-wrap gap-2">
            <Button onClick={() => run(() => dbCountByCity(city), 'DB count by city completed')}>Count by City</Button>
            <Button onClick={() => run(() => dbCountByState(state), 'DB count by state completed')}>Count by State</Button>
          </div>
        </Card>

        <Card className="space-y-3">
          <h2 className="text-lg font-semibold">Data Source Transfer</h2>
          <div className="grid gap-2 sm:grid-cols-2">
            <select
              value={sourceFrom}
              onChange={(event) => setSourceFrom(event.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            >
              {sources.map((source) => (
                <option key={source} value={source}>{source}</option>
              ))}
            </select>
            <select
              value={sourceTo}
              onChange={(event) => setSourceTo(event.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            >
              {sources.map((source) => (
                <option key={source} value={source}>{source}</option>
              ))}
            </select>
          </div>
          <Button onClick={() => run(() => transferDataSource(sourceFrom, sourceTo, book), 'Source transfer complete')}>
            Transfer Data
          </Button>
        </Card>
      </div>
    </section>
  )
}
