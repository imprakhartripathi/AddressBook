import type { ButtonHTMLAttributes } from 'react'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement>

export default function Button({ className = '', ...props }: ButtonProps) {
  return (
    <button
      className={`inline-flex items-center justify-center rounded-xl bg-gradient-to-br from-slate-900 to-slate-800 px-4 py-2 text-sm font-semibold text-white shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:from-slate-800 hover:to-slate-700 hover:shadow-md disabled:cursor-not-allowed disabled:opacity-60 ${className}`}
      {...props}
    />
  )
}
