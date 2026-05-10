import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { fetchFinalBill } from "../../api/menuApi";

export default function FinalBillPage() {
  const { qrToken } = useParams();
  const [bill, setBill] = useState(null);

  useEffect(() => {
    fetchFinalBill(qrToken).then(setBill);
  }, [qrToken]);

  if (!bill) {
    return <div className="p-6 text-center text-sand-700">Loading bill...</div>;
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-6 md:px-8">
      <div className="glass-card space-y-5 p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-paprika-500">Table {bill.tableNumber}</p>
            <h1 className="font-display text-3xl text-sand-900">Final Bill</h1>
          </div>
          <div className="flex items-center gap-4">
            <Link to={`/orders/${qrToken}`} className="text-sm font-semibold text-sand-900">
              My Orders
            </Link>
            <Link to={`/menu/${qrToken}`} className="text-sm font-semibold text-paprika-700">
              Order More
            </Link>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <div className="rounded-3xl bg-white p-4 text-sm text-sand-700">
            <div className="flex justify-between">
              <span>Subtotal</span>
              <span>Rs. {Number(bill.subtotalAmount).toFixed(2)}</span>
            </div>
            <div className="mt-2 flex justify-between">
              <span>Discount</span>
              <span>Rs. {Number(bill.discountAmount).toFixed(2)}</span>
            </div>
            <div className="mt-2 flex justify-between">
              <span>Tax</span>
              <span>Rs. {Number(bill.taxAmount).toFixed(2)}</span>
            </div>
            <div className="mt-2 flex justify-between">
              <span>Service Charge</span>
              <span>Rs. {Number(bill.serviceChargeAmount).toFixed(2)}</span>
            </div>
            <div className="mt-4 flex justify-between border-t border-sand-100 pt-4 font-bold text-sand-900">
              <span>Total</span>
              <span>Rs. {Number(bill.payableAmount).toFixed(2)}</span>
            </div>
            <div className="mt-2 flex justify-between text-emerald-700">
              <span>Paid</span>
              <span>Rs. {Number(bill.paidAmount).toFixed(2)}</span>
            </div>
            <div className="mt-2 flex justify-between font-semibold text-red-700">
              <span>Pending</span>
              <span>Rs. {Number(bill.pendingAmount).toFixed(2)}</span>
            </div>
          </div>

          <div className="rounded-3xl bg-white p-4">
            <p className="font-semibold text-sand-900">Payment Status</p>
            <p className="mt-2 text-sm text-sand-600">
              Paid orders are settled. Unpaid orders can be cleared at the counter or by admin from the dashboard.
            </p>
          </div>
        </div>

        <div className="space-y-4">
          {bill.orders.map((order) => (
            <article key={order.id} className="rounded-3xl bg-white p-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-sm text-sand-600">Order #{order.id}</p>
                  <p className="font-semibold text-sand-900">
                    {order.paymentMethod?.replaceAll("_", " ")} • {order.paymentStatus}
                  </p>
                  {order.appliedCouponCode && (
                    <p className="text-sm text-emerald-700">Coupon: {order.appliedCouponCode}</p>
                  )}
                  {order.paymentReference && (
                    <p className="text-sm text-sand-600">Ref: {order.paymentReference}</p>
                  )}
                </div>
                <p className="text-lg font-bold text-sand-900">Rs. {Number(order.totalAmount).toFixed(2)}</p>
              </div>
            </article>
          ))}
        </div>
      </div>
    </main>
  );
}
