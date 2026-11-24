import { Plus } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import type { InventorySummaryDTO, StockUpdateRequestDTO } from '../types/scms';
import * as React from "react";

export function InventoryPage() {
  const [items, setItems] = useState<InventorySummaryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [productId, setProductId] = useState('');
  const [quantity, setQuantity] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadInventory = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiRequest<InventorySummaryDTO[]>('/inventory/summaries');
      setItems(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load inventory');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadInventory();
  }, [loadInventory]);

  async function handleAddStock(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const payload: StockUpdateRequestDTO = {
        productId: Number(productId),
        quantity: Number(quantity),
      };
      await apiRequest('/inventory/add-stock', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      setProductId('');
      setQuantity('');
      await loadInventory();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add stock');
    } finally {
      setSubmitting(false);
    }
  }

  function renderTableBody() {
    if (loading) {
      return (
        <tr>
          <td colSpan={3} className="px-4 py-8 text-center text-slate-500">
            Loading inventory...
          </td>
        </tr>
      );
    }
    if (items.length === 0) {
      return (
        <tr>
          <td colSpan={3} className="px-4 py-8 text-center text-slate-500">
            No inventory records found.
          </td>
        </tr>
      );
    }
    return items.map((item) => (
      <tr key={item.id} className="hover:bg-slate-50">
        <td className="px-4 py-3 font-mono text-xs text-slate-500">#{item.id}</td>
        <td className="px-4 py-3 font-medium">Product #{item.productId}</td>
        <td className="px-4 py-3 text-right font-mono font-semibold">{item.quantity}</td>
      </tr>
    ));
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">Inventory</h2>
        <p className="mt-1 text-sm text-slate-500">
          Physical stock levels by product. Replenish stock or inspect current counts.
        </p>
      </div>

      {error && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {error}
        </div>
      )}

      <form
        onSubmit={handleAddStock}
        className="flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 bg-white p-5 shadow-xs"
      >
        <input
          required
          type="number"
          min="1"
          placeholder="Product ID"
          value={productId}
          onChange={(e) => setProductId(e.target.value)}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
        />
        <input
          required
          type="number"
          min="1"
          placeholder="Quantity to add"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={submitting}
          className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
        >
          <Plus className="h-4 w-4" />
          Add Stock
        </button>
      </form>

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xs">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
            <tr>
              <th className="px-4 py-3">Inventory ID</th>
              <th className="px-4 py-3">Product ID</th>
              <th className="px-4 py-3 text-right">Physical Quantity</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">{renderTableBody()}</tbody>
        </table>
      </div>
    </div>
  );
}
