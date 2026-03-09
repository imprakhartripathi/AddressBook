import type { Contact } from '../../types/contact'
import Button from '../ui/Button'
import Card from '../ui/Card'

type ContactCardProps = {
  contact: Contact
  onEdit: (contact: Contact) => void
  onDelete: (contact: Contact) => void
}

export default function ContactCard({ contact, onEdit, onDelete }: ContactCardProps) {
  return (
    <Card className="space-y-4">
      <div>
        <p className="text-lg font-semibold text-slate-900">
          {contact.firstName} {contact.lastName}
        </p>
        <p className="text-sm text-slate-500">{contact.email}</p>
        <p className="text-sm text-slate-500">{contact.phone}</p>
      </div>
      <div className="text-sm text-slate-600">
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
