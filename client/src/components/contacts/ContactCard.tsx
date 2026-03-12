import type { Contact } from '../../types/contact'
import Button from '../ui/Button'
import Card from '../ui/Card'

type ContactCardProps = {
  contact: Contact
  onView: (contact: Contact) => void
  onEdit: (contact: Contact) => void
  onDelete: (contact: Contact) => void
}

export default function ContactCard({ contact, onView, onEdit, onDelete }: ContactCardProps) {
  return (
    <Card className="space-y-4">
      <div>
        <button
          type="button"
          onClick={() => onView(contact)}
          className="text-left text-lg font-bold text-slate-900 transition hover:text-sky-700"
        >
          {contact.firstName} {contact.lastName}
        </button>
        <p className="text-sm text-slate-500">{contact.email}</p>
        <p className="text-sm text-slate-500">{contact.phone}</p>
      </div>
      <div className="rounded-xl border border-slate-100 bg-white/70 p-3 text-sm text-slate-600">
        <p>{contact.address}</p>
        <p>
          {contact.city}, {contact.state} {contact.zip}
        </p>
      </div>
      <div className="flex gap-2">
        <Button onClick={() => onEdit(contact)} className="bg-slate-700 hover:bg-slate-600">
          Edit
        </Button>
        <Button onClick={() => onDelete(contact)} className="bg-rose-600 hover:bg-rose-500">
          Delete
        </Button>
      </div>
    </Card>
  )
}
