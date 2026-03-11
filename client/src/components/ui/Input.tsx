import type { InputHTMLAttributes } from 'react'

type InputProps = InputHTMLAttributes<HTMLInputElement>

export default function Input({ className = '', ...props }: InputProps) {
  return (
    <input
      className={`w-full rounded-xl border border-slate-200 bg-white/95 px-3 py-2 text-sm text-slate-900 shadow-sm placeholder:text-slate-400 transition focus:border-sky-300 focus:outline-none focus:ring-2 focus:ring-sky-100 ${className}`}
      {...props}
    />
  )
}
