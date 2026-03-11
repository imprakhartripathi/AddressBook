import type { HTMLAttributes, PropsWithChildren } from 'react'

type CardProps = PropsWithChildren<HTMLAttributes<HTMLDivElement>>

export default function Card({ className = '', children, ...props }: CardProps) {
  return (
    <div
      className={`rounded-2xl border border-white/60 bg-white/85 p-6 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur ${className}`}
      {...props}
    >
      {children}
    </div>
  )
}
