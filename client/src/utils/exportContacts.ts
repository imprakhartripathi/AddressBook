import jsPDF from 'jspdf'
import type { Contact } from '../types'

type ExportFormat = 'json' | 'txt' | 'pdf'

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

function toText(contacts: Contact[]) {
  return contacts
    .map(
      (contact, index) =>
        `${index + 1}. ${contact.firstName} ${contact.lastName}\n` +
        `   Email: ${contact.email}\n` +
        `   Phone: ${contact.phone}\n` +
        `   Address: ${contact.address}, ${contact.city}, ${contact.state} ${contact.zip}`,
    )
    .join('\n\n')
}

function toPdf(contacts: Contact[], bookName: string) {
  const pdf = new jsPDF()
  pdf.setFontSize(14)
  pdf.text(`Address Book: ${bookName}`, 14, 16)
  pdf.setFontSize(10)

  let y = 26
  contacts.forEach((contact, index) => {
    const lines = [
      `${index + 1}. ${contact.firstName} ${contact.lastName}`,
      `Email: ${contact.email} | Phone: ${contact.phone}`,
      `Address: ${contact.address}, ${contact.city}, ${contact.state} ${contact.zip}`,
    ]
    lines.forEach((line) => {
      if (y > 280) {
        pdf.addPage()
        y = 16
      }
      pdf.text(line, 14, y)
      y += 6
    })
    y += 2
  })

  return pdf.output('blob')
}

export function exportContacts(contacts: Contact[], bookName: string, format: ExportFormat) {
  const safeBookName = bookName.replace(/\s+/g, '-').toLowerCase()
  if (format === 'json') {
    const payload = JSON.stringify(contacts, null, 2)
    downloadBlob(new Blob([payload], { type: 'application/json;charset=utf-8' }), `${safeBookName}.json`)
    return
  }

  if (format === 'txt') {
    const payload = toText(contacts)
    downloadBlob(new Blob([payload], { type: 'text/plain;charset=utf-8' }), `${safeBookName}.txt`)
    return
  }

  downloadBlob(toPdf(contacts, bookName), `${safeBookName}.pdf`)
}
