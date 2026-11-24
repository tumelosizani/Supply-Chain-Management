interface StatusBadgeProps {
  status: string;
}

const STATUS_STYLES: Record<string, string> = {
  PENDING: 'bg-amber-100 text-amber-800 border-amber-200',
  NEW: 'bg-sky-100 text-sky-800 border-sky-200',
  PROCESSING: 'bg-indigo-100 text-indigo-800 border-indigo-200',
  APPROVED: 'bg-blue-100 text-blue-800 border-blue-200',
  CONFIRMED: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  SHIPPED: 'bg-purple-100 text-purple-800 border-purple-200',
  DELIVERED: 'bg-teal-100 text-teal-800 border-teal-200',
  PARTIALLY_RECEIVED: 'bg-orange-100 text-orange-800 border-orange-200',
  FULLY_RECEIVED: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  CANCELLED: 'bg-rose-100 text-rose-800 border-rose-200',
  RETURNED: 'bg-slate-200 text-slate-800 border-slate-300',
};

export function StatusBadge({ status }: StatusBadgeProps) {
  const style = STATUS_STYLES[status] ?? 'bg-slate-100 text-slate-700 border-slate-200';
  return (
    <span className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${style}`}>
      {status.replace(/_/g, ' ')}
    </span>
  );
}
