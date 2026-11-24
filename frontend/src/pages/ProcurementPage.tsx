import {PackageCheck} from 'lucide-react';
import * as React from 'react';
import {useCallback, useEffect, useState} from 'react';
import {apiRequest} from '../api/client';
import {StatusBadge} from '../components/StatusBadge';
import type {GoodsReceiptRequestDTO, PurchaseOrderResponseDTO} from '../types/scms';

export function ProcurementPage() {
  const [orders, setOrders] = useState<PurchaseOrderResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [receivingPo, setReceivingPo] = useState<PurchaseOrderResponseDTO | null>(null);
  const [quantities, setQuantities] = useState<Record<number, number>>({});
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadPurchaseOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiRequest<PurchaseOrderResponseDTO[]>('/purchase-orders');
      setOrders(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load purchase orders');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadPurchaseOrders();
  }, [loadPurchaseOrders]);

  function openReceiveModal(po: PurchaseOrderResponseDTO) {
    const initial: Record<number, number> = {};
    for (const item of po.items ?? []) {
      initial[item.id] = Math.max(0, item.quantity - (item.quantityReceived ?? 0));
    }
    setQuantities(initial);
    setNotes('');
    setReceivingPo(po);
  }

  async function handleReceive(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!receivingPo) return;
    setSubmitting(true);
    setError(null);
    try {
      const items = (receivingPo.items ?? [])
        .map((item) => ({
          purchaseOrderItemId: item.id,
          quantityReceived: Number(quantities[item.id] ?? 0),
        }))
        .filter((i) => i.quantityReceived > 0);

      const payload: GoodsReceiptRequestDTO = { items, notes: notes || undefined };
      await apiRequest(`/purchase-orders/${receivingPo.id}/receive`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      setReceivingPo(null);
      await loadPurchaseOrders();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to receive goods');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">Procurement</h2>
        <p className="mt-1 text-sm text-slate-500">
          Purchase orders and goods receiving (automatically replenishes inventory).
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
              <th className="px-4 py-3">PO ID</th>
              <th className="px-4 py-3">Supplier</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Line Items</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {loading ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
                  Loading purchase orders...
                </td>
              </tr>
            ) : orders.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
                  No purchase orders found.
                </td>
              </tr>
            ) : (
              orders.map((po) => {
                const canReceive =
                  po.status !== 'CANCELLED' &&
                  po.status !== 'RETURNED' &&
                  po.status !== 'FULLY_RECEIVED' &&
                  (po.items?.length ?? 0) > 0;
                return (
                  <tr key={po.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-mono text-xs text-slate-600">#{po.id}</td>
                    <td className="px-4 py-3">Supplier #{po.supplierId}</td>
                    <td className="px-4 py-3">
                      <StatusBadge status={po.status} />
                    </td>
                    <td className="px-4 py-3 text-slate-600">{po.items?.length ?? 0} item(s)</td>
                    <td className="px-4 py-3 text-right">
                      {canReceive && (
                        <button
                          onClick={() => openReceiveModal(po)}
                          className="inline-flex items-center gap-1.5 rounded-md bg-indigo-50 px-2.5 py-1 text-xs font-medium text-indigo-700 hover:bg-indigo-100"
                        >
                          <PackageCheck className="h-3.5 w-3.5" />
                          Receive Goods
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {receivingPo && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <form
            onSubmit={handleReceive}
            className="w-full max-w-lg space-y-4 rounded-xl bg-white p-6 shadow-xl"
          >
            <h3 className="text-lg font-semibold">Receive Goods — PO #{receivingPo.id}</h3>
            <div className="space-y-3">
              {receivingPo.items.map((item) => {
                const received = item.quantityReceived ?? 0;
                const remaining = Math.max(0, item.quantity - received);
                return (
                  <div
                    key={item.id}
                    className="flex items-center justify-between rounded-lg border border-slate-200 p-3 text-sm"
                  >
                    <div>
                      <p className="font-medium">Product #{item.productId}</p>
                      <p className="text-xs text-slate-500">
                        Ordered: {item.quantity} · Received: {received} · Remaining: {remaining}
                      </p>
                    </div>
                    <input
                      type="number"
                      min={0}
                      max={remaining}
                      value={quantities[item.id] ?? 0}
                      onChange={(e) =>
                        setQuantities((prev) => ({ ...prev, [item.id]: Number(e.target.value) }))
                      }
                      className="w-24 rounded-lg border border-slate-300 px-2.5 py-1.5 text-right text-sm"
                    />
                  </div>
                );
              })}
            </div>
            <input
              placeholder="Receipt notes (optional)"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setReceivingPo(null)}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
              >
                Confirm Receipt
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
