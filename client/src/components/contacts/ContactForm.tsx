import { useMemo, useState, type FormEvent } from 'react'
import type { Contact } from '../../types/contact'
import Button from '../ui/Button'
import Input from '../ui/Input'

type ContactFormProps = {
  initialContact?: Contact | null
  onSubmit: (payload: Omit<Contact, 'id'>) => void
  onCancel: () => void
  loading?: boolean
}

type FormState = Omit<Contact, 'id'>

const emptyForm: FormState = {
  firstName: '',
  lastName: '',
  address: '',
  city: '',
  state: '',
  zip: '',
  phone: '',
  email: '',
}

export default function ContactForm({ initialContact, onSubmit, onCancel, loading }: ContactFormProps) {
  const [formData, setFormData] = useState<FormState>(() => {
    if (!initialContact) {
      return emptyForm
    }
    const { id: _id, ...rest } = initialContact
    return { ...emptyForm, ...rest }
  })
  const [errors, setErrors] = useState<{ email?: string; phone?: string }>({})

  const isEdit = useMemo(() => Boolean(initialContact?.id), [initialContact])

  const handleChange = (field: keyof FormState, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const validate = () => {
    const nextErrors: { email?: string; phone?: string } = {}
    if (!formData.email.trim()) {
      nextErrors.email = 'Email is required'
    }
    if (!formData.phone.trim()) {
      nextErrors.phone = 'Phone is required'
    }
    setErrors(nextErrors)
    return Object.keys(nextErrors).length === 0
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    if (!validate()) {
      return
    }
    onSubmit({ ...formData })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">First name</label>
          <Input
            value={formData.firstName}
            onChange={(event) => handleChange('firstName', event.target.value)}
            placeholder="First name"
          />
        </div>
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Last name</label>
          <Input
            value={formData.lastName}
            onChange={(event) => handleChange('lastName', event.target.value)}
            placeholder="Last name"
          />
        </div>
      </div>
      <div className="space-y-1">
        <label className="text-sm font-medium text-slate-700">Address</label>
        <Input
          value={formData.address}
          onChange={(event) => handleChange('address', event.target.value)}
          placeholder="Street address"
        />
      </div>
      <div className="grid gap-4 md:grid-cols-3">
        <div className="space-y-1 md:col-span-1">
          <label className="text-sm font-medium text-slate-700">City</label>
          <Input
            value={formData.city}
            onChange={(event) => handleChange('city', event.target.value)}
            placeholder="City"
          />
        </div>
        <div className="space-y-1 md:col-span-1">
          <label className="text-sm font-medium text-slate-700">State</label>
          <Input
            value={formData.state}
            onChange={(event) => handleChange('state', event.target.value)}
            placeholder="State"
          />
        </div>
        <div className="space-y-1 md:col-span-1">
          <label className="text-sm font-medium text-slate-700">Zip</label>
          <Input
            value={formData.zip}
            onChange={(event) => handleChange('zip', event.target.value)}
            placeholder="Zip code"
          />
        </div>
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Phone</label>
          <Input
            value={formData.phone}
            onChange={(event) => handleChange('phone', event.target.value)}
            placeholder="Phone"
          />
          {errors.phone && <p className="text-xs text-rose-600">{errors.phone}</p>}
        </div>
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Email</label>
          <Input
            value={formData.email}
            onChange={(event) => handleChange('email', event.target.value)}
            placeholder="Email"
            type="email"
          />
          {errors.email && <p className="text-xs text-rose-600">{errors.email}</p>}
        </div>
      </div>
      <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
        <Button type="button" onClick={onCancel} className="bg-slate-200 text-slate-700 hover:bg-slate-300">
          Cancel
        </Button>
        <Button type="submit" disabled={loading}>
          {isEdit ? 'Update Contact' : 'Add Contact'}
        </Button>
      </div>
    </form>
  )
}
