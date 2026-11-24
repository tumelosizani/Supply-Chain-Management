import { CheckCircle2, PackageCheck, Send, X } from 'lucide-react';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { apiRequest } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import type {
  ShipmentDispatchRequestDTO,
  ShipmentResponseDTO,
  ShipmentStatus,
} from '../types/scms';

const STATUS_FILTERS: Array<'ALL' | ShipmentStatus> = [
  'ALL',
  'PENDING',
  'SHIPPED',
  'DELIVERED',
  'RETURNED',
];

function formatDateTime(value?: string) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function formatTimeline(shipment: ShipmentResponseDTO): string {
  switch (shipment.status) {
    case 'PENDING':
      return 'Awaiting dispatch';
    case 'SHIPPED':
      return shipment.shippedAt ? `Shipped ${formatDateTime(shipment.shippedAt)}` : 'Shipped';
    case 'DELIVERED':
      return shipment.deliveredAt ? `Delivered ${formatDateTime(shipment.deliveredAt)}` : 'Delivered';
    case 'RETURNED':
      return shipment.shippedAt ? `Returned (shipped ${formatDateTime(shipment.shippedAt)})` : 'Returned';
  }
}

export function ShipmentsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const orderIdParam = searchParams.get('orderId');

  const [shipments, setShipments] = useState<ShipmentResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<'ALL' | ShipmentStatus>('ALL');

  const [dispatchingId, setDispatchingId] = useState<number | null>(null);
  const [carrier, setCarrier] = useState('');
  const [trackingNumber, setTrackingNumber] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState<string | null>(null);
  const [busyIds, setBusyIds] = useState<Set<number>>(() => new Set());

  const loadShipments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const endpoint = orderIdParam ? `/shipments/order/${orderIdParam}` : '/shipments';
      const res = await apiRequest<ShipmentResponseDTO[]>(endpoint);
      setShipments(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load shipments');
    } finally {
      setLoading(false);
    }
  }, [orderIdParam]);

  useEffect(() => {
    void loadShipments();
  }, [loadShipments]);

  const filteredShipments = useMemo(() => {
    if (statusFilter === 'ALL') return shipments;
    return shipments.filter((s) => s.status === statusFilter);
  }, [shipments, statusFilter]);

  function openDispatchModal(shipment: ShipmentResponseDTO) {
    setDispatchingId(shipment.id);
    setCarrier(shipment.carrier ?? '');
    setTrackingNumber(shipment.trackingNumber ?? '');
    setModalError(null);
  }

  function closeDispatchModal() {
    setDispatchingId(null);
    setCarrier('');
    setTrackingNumber('');
    setModalError(null);
  }

  async function handleDispatch(e: React.FormEvent) {
    e.preventDefault();
    if (dispatchingId === null) return;

    const trimmedCarrier = carrier.trim();
    const trimmedTracking = trackingNumber.trim();
    if (!trimmedCarrier || !trimmedTracking) {
      setModalError('Carrier and tracking number are required.');
      return;
    }

    setSubmitting(true);
    setModalError(null);
    try {
      const payload: ShipmentDispatchRequestDTO = {
        carrier: trimmedCarrier,
        trackingNumber: trimmedTracking,
      };
      await apiRequest<ShipmentResponseDTO>(`/shipments/${dispatchingId}/ship`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      closeDispatchModal();
      await loadShipments();
    } catch (err) {
      setModalError(err instanceof Error ? err.message : 'Failed to dispatch shipment');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDeliver(shipmentId: number) {
    if (busyIds.has(shipmentId)) return;
    setError(null);
    setBusyIds((prev) => new Set(prev).add(shipmentId));
    try {
      await apiRequest<ShipmentResponseDTO>(`/shipments/${shipmentId}/deliver`, {
        method: 'POST',
      });
      await loadShipments();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to mark shipment as delivered');
    } finally {
      setBusyIds((prev) => {
        const next = new Set(prev);
        next.delete(shipmentId);
        return next;
      });
    }
  }

  function renderTableBody() {
    if (loading) {
      return (
        <tr>
          <td colSpan={7} className="px-4 py-8 text-center text-slate-500">
            Loading shipments...
          </td>
        </tr>
      );
    }
    if (filteredShipments.length === 0) {
      return (
        <tr>
          <td colSpan={7} className="px-4 py-8 text-center text-slate-500">
            No shipments found. Confirm a customer order to auto-create one.
          </td>
        </tr>
      );
    }
    return filteredShipments.map((shipment) => (
      <tr key={shipment.id} className="hover:bg-slate-50">
        <td className="px-4 py-3 font-mono text-xs text-slate-600">#{shipment.id}</td>
        <td className="px-4 py-3 font-mono text-xs">
          <Link to="/orders" className="text-indigo-600 hover:underline">
            Order #{shipment.orderId}
          </Link>
        </td>
        <td className="px-4 py-3">
          <StatusBadge status={shipment.status} />
        </td>
        <td className="px-4 py-3 text-slate-700">{shipment.carrier || '—'}</td>
        <td className="px-4 py-3 font-mono text-xs text-slate-600">
          {shipment.trackingNumber || '—'}
        </td>
        <td className="px-4 py-3 text-xs text-slate-500">
          {formatTimeline(shipment)}
        </td>
        <td className="px-4 py-3 text-right">
          {shipment.status === 'PENDING' && (
            <button
              onClick={() => openDispatchModal(shipment)}
              className="inline-flex items-center gap-1 rounded-md bg-indigo-50 px-2.5 py-1 text-xs font-medium text-indigo-700 hover:bg-indigo-100"
            >
              <Send className="h-3.5 w-3.5" />
              Ship
            </button>
          )}
          {shipment.status === 'SHIPPED' && (
            <button
              onClick={() => handleDeliver(shipment.id)}
              disabled={busyIds.has(shipment.id)}
              className="inline-flex items-center gap-1 rounded-md bg-teal-50 px-2.5 py-1 text-xs font-medium text-teal-700 hover:bg-teal-100 disabled:opacity-50"
            >
              <CheckCircle2 className="h-3.5 w-3.5" />
              Mark Delivered
            </button>
          )}
        </td>
      </tr>
    ));
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight">Shipments</h2>
          <p className="mt-1 text-sm text-slate-500">
            Track order fulfillment, dispatch pending shipments, and confirm delivery.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-1.5">
          {orderIdParam && (
            <button
              onClick={() => setSearchParams({})}
              className="mr-2 inline-flex items-center gap-1 rounded-lg border border-indigo-200 bg-indigo-50 px-3 py-1.5 text-xs font-medium text-indigo-700 hover:bg-indigo-100"
            >
              Order #{orderIdParam}
              <X className="h-3.5 w-3.5" />
            </button>
          )}
          {STATUS_FILTERS.map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`rounded-lg px-3 py-1.5 text-xs font-medium transition ${
                statusFilter === status
                  ? 'bg-indigo-600 text-white'
                  : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              }`}
            >
              {status}
            </button>
          ))}
        </div>
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
              <th className="px-4 py-3">Shipment</th>
              <th className="px-4 py-3">Order</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Carrier</th>
              <th className="px-4 py-3">Tracking #</th>
              <th className="px-4 py-3">Timeline</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">{renderTableBody()}</tbody>
        </table>
      </div>

      {dispatchingId !== null && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <div className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-lg">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <PackageCheck className="h-5 w-5 text-indigo-600" />
                <h3 className="text-lg font-semibold">Dispatch Shipment #{dispatchingId}</h3>
              </div>
              <button
                onClick={closeDispatchModal}
                className="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <form onSubmit={handleDispatch} className="mt-4 space-y-4">
              {modalError && (
                <div className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-800">
                  {modalError}
                </div>
              )}

              <div>
                <label className="block text-xs font-medium uppercase tracking-wider text-slate-500">
                  Carrier
                </label>
                <input
                  type="text"
                  required
                  value={carrier}
                  onChange={(e) => setCarrier(e.target.value)}
                  placeholder="e.g. DHL, FedEx, UPS"
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-medium uppercase tracking-wider text-slate-500">
                  Tracking Number
                </label>
                <input
                  type="text"
                  required
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                  placeholder="e.g. TRK-2026-0001"
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-mono text-sm focus:border-indigo-500 focus:outline-none"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={closeDispatchModal}
                  className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
                >
                  <Send className="h-4 w-4" />
                  {submitting ? 'Dispatching...' : 'Confirm Dispatch'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
