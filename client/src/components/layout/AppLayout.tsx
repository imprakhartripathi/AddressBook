import { Outlet, NavLink } from 'react-router-dom'

export default function AppLayout() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div className="text-lg font-semibold">AddressBook App</div>
          <nav className="flex gap-4 text-sm">
            <NavLink
              to="/"
              className={({ isActive }) =>
                `rounded px-2 py-1 ${
                  isActive ? 'bg-slate-900 text-white' : 'text-slate-700 hover:text-slate-900'
                }`
              }
              end
            >
              Home
            </NavLink>
            <NavLink
              to="/contacts"
              className={({ isActive }) =>
                `rounded px-2 py-1 ${
                  isActive ? 'bg-slate-900 text-white' : 'text-slate-700 hover:text-slate-900'
                }`
              }
            >
              Contacts
            </NavLink>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-6 py-10">
        <Outlet />
      </main>
    </div>
  )
}
