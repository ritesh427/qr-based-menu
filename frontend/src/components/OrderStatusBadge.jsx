const styles = {
  CREATED: "bg-amber-100 text-amber-800",
  CONFIRMED: "bg-blue-100 text-blue-800",
  PREPARING: "bg-orange-100 text-orange-800",
  READY: "bg-emerald-100 text-emerald-800",
  SERVED: "bg-slate-200 text-slate-800"
};

export default function OrderStatusBadge({ status }) {
  return (
    <span className={`rounded-full px-3 py-1 text-xs font-semibold ${styles[status] || styles.CREATED}`}>
      {status}
    </span>
  );
}
