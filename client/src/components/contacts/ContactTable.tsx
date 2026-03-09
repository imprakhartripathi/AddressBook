import type { Contact } from '../../types/contact'
import Button from '../ui/Button'
import ContactCard from './ContactCard'

type ContactTableProps = {
  contacts: Contact[]
  onEdit: (contact: Contact) => void
  onDelete: (contact: Contact) => void
}

export default function ContactTable({ contacts, onEdit, onDelete }: ContactTableProps) {
  if (contacts.length === 0) {
    return (
      <div className="rounded-lg border border-dashed border-slate-200 bg-white p-10 text-center text-slate-500">
        No contacts yet. Add your first contact to get started.
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:hidden">
        {contacts.map((contact) => (
          <ContactCard
            key={contact.id}
            contact={contact}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))}
      </div>

      <div className="hidden overflow-hidden rounded-lg border border-slate-200 bg-white md:block">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Phone</th>
              <th className="px-4 py-3">Location</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {contacts.map((contact) => (
              <tr key={contact.id} className="hover:bg-slate-50">
                <td className="px-4 py-3 font-medium text-slate-900">
                  {contact.firstName} {contact.lastName}
                </td>
                <td className="px-4 py-3 text-slate-600">{contact.email}</td>
                <td className="px-4 py-3 text-slate-600">{contact.phone}</td>
                <td className="px-4 py-3 text-slate-600">
                  {contact.city}, {contact.state}
                </td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      onClick={() => onEdit(contact)}
                      className="bg-slate-700 hover:bg-slate-600"
                    >
                      Edit
                    </Button>
                    <Button
                      onClick={() => onDelete(contact)}
                      className="bg-rose-600 hover:bg-rose-500"
                    >
                      Delete
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
