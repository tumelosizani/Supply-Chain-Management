import { Check, X } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import type { CustomerOrderResponseDTO } from '../types/scms';

export function OrdersPage() {
  const [orders, setOrders] = useState<CustomerOrderResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  async function handleAction(orderId: number, action: 'confirm' | 'cancel') {
    setError(null);
    try {
      await apiRequest(`/customer-orders/${orderId}/${action}`, { method: 'POST' });
      await loadOrders();
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to ${action} order`);
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
      const canConfirm = order.status === 'PROCESSING';
      const canCancel = order.status === 'PENDING' || order.status === 'PROCESSING';
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
            {(canConfirm || canCancel) && (
              <div className="inline-flex gap-2">
                {canConfirm && (
                  <button
                    onClick={() => handleAction(order.id, 'confirm')}
                    className="inline-flex items-center gap-1 rounded-md bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-100"
                  >
                    <Check className="h-3.5 w-3.5" />
                    Confirm
                  </button>
                )}
                {canCancel && (
                  <button
                    onClick={() => handleAction(order.id, 'cancel')}
                    className="inline-flex items-center gap-1 rounded-md bg-rose-50 px-2.5 py-1 text-xs font-medium text-rose-700 hover:bg-rose-100"
                  >
                    <X className="h-3.5 w-3.5" />
                    Cancel
                  </button>
                )}
              </div>
            )}
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
