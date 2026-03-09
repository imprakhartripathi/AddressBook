import Card from '../components/ui/Card'

export default function HomePage() {
  return (
    <section className="space-y-6">
      <div>
        <p className="text-sm uppercase tracking-wide text-slate-500">AddressBook System</p>
        <h1 className="text-3xl font-semibold text-slate-900">Welcome to Address Book Program</h1>
        <p className="mt-2 text-slate-600">
          Manage contacts, organize address books, and sync data to file, CSV, or JSON.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card className="space-y-2">
          <h2 className="text-lg font-semibold text-slate-900">Contacts</h2>
          <p className="text-sm text-slate-600">
            Create, edit, delete, and search contacts across multiple address books.
          </p>
        </Card>
        <Card className="space-y-2">
          <h2 className="text-lg font-semibold text-slate-900">Organization</h2>
          <p className="text-sm text-slate-600">
            Group and count contacts by city or state and sort the address book with one click.
          </p>
        </Card>
        <Card className="space-y-2">
          <h2 className="text-lg font-semibold text-slate-900">Storage</h2>
          <p className="text-sm text-slate-600">
            Export or import contacts using file IO, CSV, or JSON formats.
          </p>
        </Card>
      </div>
    </section>
  )
}
