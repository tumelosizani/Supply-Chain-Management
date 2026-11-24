import { AlertCircle, Boxes, ClipboardList, Package, PackageCheck, ShoppingCart, Truck } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../api/client';
import type {
  CustomerOrderResponseDTO,
  InventorySummaryDTO,
  PageResponse,
  ProductSummaryDTO,
  PurchaseOrderResponseDTO,
  ShipmentResponseDTO,
  SupplierResponseDTO,
} from '../types/scms';

interface Stats {
  products: number;
  inventoryItems: number;
  customerOrders: number;
  shipments: number;
  purchaseOrders: number;
  suppliers: number;
}

export function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const [products, inventory, orders, shipments, pos, suppliers] = await Promise.all([
          apiRequest<PageResponse<ProductSummaryDTO>>('/products/summaries?page=0&size=1'),
          apiRequest<InventorySummaryDTO[]>('/inventory/summaries'),
          apiRequest<CustomerOrderResponseDTO[]>('/customer-orders'),
          apiRequest<ShipmentResponseDTO[]>('/shipments'),
          apiRequest<PurchaseOrderResponseDTO[]>('/purchase-orders'),
          apiRequest<SupplierResponseDTO[]>('/suppliers'),
        ]);
        if (!active) return;
        setStats({
          products: products.totalElements,
          inventoryItems: inventory.length,
          customerOrders: orders.length,
          shipments: shipments.length,
          purchaseOrders: pos.length,
          suppliers: suppliers.length,
        });
      } catch (err) {
        if (!active) return;
        setError(err instanceof Error ? err.message : 'Unable to reach backend API');
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => {
      active = false;
    };
  }, []);

  const cards = [
    { label: 'Products', value: stats?.products ?? '—', to: '/products', icon: Package },
    { label: 'Inventory Records', value: stats?.inventoryItems ?? '—', to: '/inventory', icon: Boxes },
    { label: 'Customer Orders', value: stats?.customerOrders ?? '—', to: '/orders', icon: ShoppingCart },
    { label: 'Shipments', value: stats?.shipments ?? '—', to: '/shipments', icon: PackageCheck },
    { label: 'Purchase Orders', value: stats?.purchaseOrders ?? '—', to: '/procurement', icon: ClipboardList },
    { label: 'Suppliers', value: stats?.suppliers ?? '—', to: '/suppliers', icon: Truck },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">Dashboard</h2>
        <p className="mt-1 text-sm text-slate-500">
          Live overview of your Spring Boot SCMS backend.
        </p>
      </div>

      {error && (
        <div className="flex items-start gap-3 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          <AlertCircle className="mt-0.5 h-5 w-5 shrink-0 text-amber-600" />
          <div>
            <p className="font-medium">Backend API not reachable</p>
            <p className="mt-0.5 text-amber-800">
              Start the Spring Boot app with <code className="rounded bg-amber-100 px-1.5 py-0.5 font-mono text-xs">./gradlew bootRun</code> (expects port 8080). Details: {error}
            </p>
          </div>
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {cards.map(({ label, value, to, icon: Icon }) => (
          <Link
            key={to}
            to={to}
            className="flex items-center justify-between rounded-xl border border-slate-200 bg-white p-5 shadow-xs transition hover:border-indigo-300 hover:shadow-sm"
          >
            <div>
              <p className="text-sm font-medium text-slate-500">{label}</p>
              <p className="mt-2 text-3xl font-semibold tracking-tight">
                {loading ? '…' : value}
              </p>
            </div>
            <div className="rounded-lg bg-indigo-50 p-3 text-indigo-600">
              <Icon className="h-6 w-6" />
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
