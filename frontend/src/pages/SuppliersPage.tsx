import { Plus, Trash2 } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import type { SupplierRequestDTO, SupplierResponseDTO } from '../types/scms';

export function SuppliersPage() {
  const [suppliers, setSuppliers] = useState<SupplierResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadSuppliers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiRequest<SupplierResponseDTO[]>('/suppliers');
      setSuppliers(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load suppliers');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadSuppliers();
  }, [loadSuppliers]);

  async function handleCreate(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const payload: SupplierRequestDTO = {
        name,
        email: email || undefined,
        phoneNumber: phoneNumber || undefined,
      };
      await apiRequest('/suppliers', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      setName('');
      setEmail('');
      setPhoneNumber('');
      await loadSuppliers();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create supplier');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(id: number) {
    try {
      await apiRequest(`/suppliers/${id}`, { method: 'DELETE' });
      await loadSuppliers();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete supplier');
    }
  }

  function renderTableBody() {
    if (loading) {
      return (
        <tr>
          <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
            Loading suppliers...
          </td>
        </tr>
      );
    }
    if (suppliers.length === 0) {
      return (
        <tr>
          <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
            No suppliers found.
          </td>
        </tr>
      );
    }
    return suppliers.map((s) => (
      <tr key={s.id} className="hover:bg-slate-50">
        <td className="px-4 py-3 font-mono text-xs text-slate-500">#{s.id}</td>
        <td className="px-4 py-3 font-medium">{s.name}</td>
        <td className="px-4 py-3 text-slate-600">{s.email ?? '—'}</td>
        <td className="px-4 py-3 text-slate-600">{s.phoneNumber ?? '—'}</td>
        <td className="px-4 py-3 text-right">
          <button
            onClick={() => handleDelete(s.id)}
            className="rounded p-1 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
            title="Delete supplier"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </td>
      </tr>
    ));
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">Suppliers</h2>
        <p className="mt-1 text-sm text-slate-500">Vendor directory and contact details.</p>
      </div>

      {error && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {error}
        </div>
      )}

      <form
        onSubmit={handleCreate}
        className="grid gap-4 rounded-xl border border-slate-200 bg-white p-5 shadow-xs sm:grid-cols-4"
      >
        <input
          required
          placeholder="Supplier name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
        />
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
        />
        <input
          placeholder="Phone number"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={submitting}
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
        >
          <Plus className="h-4 w-4" />
          Add Supplier
        </button>
      </form>

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xs">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
            <tr>
              <th className="px-4 py-3">ID</th>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Phone</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">{renderTableBody()}</tbody>
        </table>
      </div>
    </div>
  );
}
