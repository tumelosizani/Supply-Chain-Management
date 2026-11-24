import { Check, PackageCheck, Play, X } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import type { CustomerOrderResponseDTO } from '../types/scms';

export function OrdersPage() {
  const [orders, setOrders] = useState<CustomerOrderResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyIds, setBusyIds] = useState<Set<number>>(() => new Set());

  const loadOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiRequest<CustomerOrderResponseDTO[]>('/customer-orders');
      setOrders(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load customer orders');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  async function handleAction(orderId: number, action: 'process' | 'confirm' | 'cancel') {
    if (busyIds.has(orderId)) return;
    setError(null);
    setBusyIds((prev) => new Set(prev).add(orderId));
    try {
      await apiRequest(`/customer-orders/${orderId}/${action}`, { method: 'POST' });
      await loadOrders();
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to ${action} order`);
    } finally {
      setBusyIds((prev) => {
        const next = new Set(prev);
        next.delete(orderId);
        return next;
      });
    }
  }

  function renderTableBody() {
    if (loading) {
      return (
        <tr>
          <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
            Loading orders...
          </td>
        </tr>
      );
    }
    if (orders.length === 0) {
      return (
        <tr>
          <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
            No customer orders found.
          </td>
        </tr>
      );
    }
    return orders.map((order) => {
      const isBusy = busyIds.has(order.id);
      const canProcess = order.status === 'PENDING';
      const canConfirm = order.status === 'PROCESSING';
      const canCancel = order.status === 'PENDING' || order.status === 'PROCESSING';
      const hasFulfillment =
        order.status === 'CONFIRMED' ||
        order.status === 'SHIPPED' ||
        order.status === 'DELIVERED';
      return (
        <tr key={order.id} className="hover:bg-slate-50">
          <td className="px-4 py-3 font-mono text-xs text-slate-600">#{order.id}</td>
          <td className="px-4 py-3">
            <StatusBadge status={order.status} />
          </td>
          <td className="px-4 py-3 text-slate-600">{order.items?.length ?? 0} item(s)</td>
          <td className="px-4 py-3 text-right font-mono">
            ${Number(order.totalCost ?? 0).toFixed(2)}
          </td>
          <td className="px-4 py-3 text-right">
            <div className="inline-flex items-center gap-2">
              {canProcess && (
                <button
                  onClick={() => handleAction(order.id, 'process')}
                  disabled={isBusy}
                  className="inline-flex items-center gap-1 rounded-md bg-indigo-50 px-2.5 py-1 text-xs font-medium text-indigo-700 hover:bg-indigo-100 disabled:opacity-50"
                >
                  <Play className="h-3.5 w-3.5" />
                  Process
                </button>
              )}
              {canConfirm && (
                <button
                  onClick={() => handleAction(order.id, 'confirm')}
                  disabled={isBusy}
                  className="inline-flex items-center gap-1 rounded-md bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-100 disabled:opacity-50"
                >
                  <Check className="h-3.5 w-3.5" />
                  Confirm
                </button>
              )}
              {canCancel && (
                <button
                  onClick={() => handleAction(order.id, 'cancel')}
                  disabled={isBusy}
                  className="inline-flex items-center gap-1 rounded-md bg-rose-50 px-2.5 py-1 text-xs font-medium text-rose-700 hover:bg-rose-100 disabled:opacity-50"
                >
                  <X className="h-3.5 w-3.5" />
                  Cancel
                </button>
              )}
              {hasFulfillment && (
                <Link
                  to="/shipments"
                  className="inline-flex items-center gap-1 rounded-md bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-700 hover:bg-slate-200"
                >
                  <PackageCheck className="h-3.5 w-3.5" />
                  Shipments
                </Link>
              )}
            </div>
          </td>
        </tr>
      );
    });
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">Customer Orders</h2>
        <p className="mt-1 text-sm text-slate-500">
          Confirm orders to deduct reserved stock, or cancel to release reservations.
        </p>
      </div>

      {error && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {error}
        </div>
      )}

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xs">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
            <tr>
              <th className="px-4 py-3">Order ID</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Items</th>
              <th className="px-4 py-3 text-right">Total Cost</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">{renderTableBody()}</tbody>
        </table>
      </div>
    </div>
  );
}
