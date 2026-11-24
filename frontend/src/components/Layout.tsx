import {
  Boxes,
  ClipboardList,
  LayoutDashboard,
  Package,
  PackageCheck,
  ShoppingCart,
  Truck,
} from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/products', label: 'Products', icon: Package },
  { to: '/inventory', label: 'Inventory', icon: Boxes },
  { to: '/orders', label: 'Customer Orders', icon: ShoppingCart },
  { to: '/shipments', label: 'Shipments', icon: PackageCheck },
  { to: '/procurement', label: 'Procurement', icon: ClipboardList },
  { to: '/suppliers', label: 'Suppliers', icon: Truck },
];

export function Layout() {
  return (
    <div className="flex min-h-screen bg-slate-50 text-slate-900">
      <aside className="w-64 shrink-0 border-r border-slate-200 bg-white">
        <div className="flex h-16 items-center gap-2 border-b border-slate-200 px-6">
          <Boxes className="h-6 w-6 text-indigo-600" />
          <span className="text-lg font-semibold tracking-tight">SCMS</span>
        </div>
        <nav className="space-y-1 p-3">
          {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-indigo-50 text-indigo-700'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                }`
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-8">
          <h1 className="text-sm font-medium text-slate-500">
            Supply Chain Management System
          </h1>
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="text-xs font-medium text-indigo-600 hover:text-indigo-500"
          >
            Swagger API Docs ↗
          </a>
        </header>

        <main className="flex-1 p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
