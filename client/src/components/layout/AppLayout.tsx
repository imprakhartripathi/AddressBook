import { Outlet, NavLink } from 'react-router-dom'

export default function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col text-slate-900">
      <header className="sticky top-0 z-20 border-b border-white/20 bg-transparent backdrop-blur-sm">
        <div className="flex w-full items-center justify-between px-6 py-4">
          <div className="flex items-center gap-3">
            <span className="inline-block h-2.5 w-2.5 rounded-full bg-sky-500 shadow-[0_0_0_6px_rgba(14,165,233,0.15)]" />
            <div className="text-lg font-extrabold tracking-tight">
              AddressBook App
            </div>
          </div>
          <nav className="flex gap-2 rounded-xl border border-white/40 bg-white/35 p-1 text-sm shadow-[0_8px_20px_rgba(15,23,42,0.05)] backdrop-blur">
            <NavLink
              to="/"
              className={({ isActive }) =>
                `rounded-lg px-3 py-1.5 transition ${
                  isActive
                    ? "bg-slate-900 text-white shadow-sm"
                    : "text-slate-600 hover:text-slate-900"
                }`
              }
              end
            >
              Home
            </NavLink>
            <NavLink
              to="/contacts"
              className={({ isActive }) =>
                `rounded-lg px-3 py-1.5 transition ${
                  isActive
                    ? "bg-slate-900 text-white shadow-sm"
                    : "text-slate-600 hover:text-slate-900"
                }`
              }
            >
              Contacts
            </NavLink>
          </nav>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-10">
        <Outlet />
      </main>
      <footer className="border-t border-white/30 bg-white/35">
        <div className="flex w-full flex-col gap-3 px-6 py-6 text-sm text-slate-600 md:flex-row md:items-center md:justify-between">
          <p className="font-medium text-slate-700">AddressBook App</p>
          <p>Designed and developed by Prakhar Tripathi</p>
          <p className="text-slate-500">Built with React + Spring Boot</p>
        </div>
      </footer>
    </div>
  );
}
