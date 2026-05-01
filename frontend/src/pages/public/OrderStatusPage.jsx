import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { createReview, fetchOrderStatus, fetchReviews } from "../../api/menuApi";
import OrderStatusBadge from "../../components/OrderStatusBadge";
import { getApiBaseUrl } from "../../utils/runtime";

export default function OrderStatusPage() {
  const { qrToken } = useParams();
  const [orders, setOrders] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [reviewForm, setReviewForm] = useState({ menuItemId: "", customerName: "", rating: 5, comment: "" });
  const [reviewMessage, setReviewMessage] = useState("");

  useEffect(() => {
    fetchOrderStatus(qrToken).then(setOrders);
    fetchReviews(qrToken).then(setReviews);
  }, [qrToken]);

  const servedItems = orders
    .filter((order) => order.status === "SERVED")
    .flatMap((order) => order.items)
    .filter((item, index, list) => list.findIndex((entry) => entry.menuItemId === item.menuItemId) === index);

  const handleReviewSubmit = async (event) => {
    event.preventDefault();
    await createReview({
      qrToken,
      menuItemId: Number(reviewForm.menuItemId),
      customerName: reviewForm.customerName,
      rating: Number(reviewForm.rating),
      comment: reviewForm.comment
    });
    setReviewForm({ menuItemId: "", customerName: "", rating: 5, comment: "" });
    setReviewMessage("Thanks for the feedback. Your review is live.");
    fetchReviews(qrToken).then(setReviews);
  };

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${getApiBaseUrl()}/ws/orders`),
      reconnectDelay: 5000
    });

    client.onConnect = () => {
      client.subscribe(`/topic/orders/table/${qrToken}`, (message) => {
        const order = JSON.parse(message.body);
        setOrders((current) => {
          const existing = current.find((entry) => entry.id === order.id);
          if (!existing) {
            return [order, ...current];
          }
          return current.map((entry) => (entry.id === order.id ? order : entry));
        });
      });
    };

    client.activate();
    return () => client.deactivate();
  }, [qrToken]);

  return (
    <main className="mx-auto max-w-4xl px-4 py-6 md:px-8">
      <div className="glass-card space-y-5 p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-paprika-500">Live Table Updates</p>
            <h1 className="font-display text-3xl text-sand-900">Order Status</h1>
          </div>
          <div className="flex items-center gap-4">
            <Link to={`/bill/${qrToken}`} className="text-sm font-semibold text-sand-900">
              Final Bill
            </Link>
            <Link to={`/menu/${qrToken}`} className="text-sm font-semibold text-paprika-700">
              Order More
            </Link>
          </div>
        </div>
        <div className="space-y-4">
          {orders.map((order) => (
            <article key={order.id} className="rounded-3xl bg-white p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-sand-600">Order #{order.id}</p>
                  <p className="font-semibold text-sand-900">Table {order.tableNumber}</p>
                  <p className="text-sm text-sand-600">ETA: {order.estimatedReadyInMinutes || 10} min</p>
                  <p className="text-sm text-sand-600">
                    {order.paymentMethod?.replaceAll("_", " ")} - {order.paymentStatus}
                  </p>
                  {order.paymentReference && (
                    <p className="text-sm text-sand-600">Ref: {order.paymentReference}</p>
                  )}
                </div>
                <OrderStatusBadge status={order.status} />
              </div>
              <div className="mt-4 space-y-2 text-sm text-sand-700">
                {order.items.map((item) => (
                  <div key={`${order.id}-${item.menuItemId}-${item.variantName || "base"}-${item.addonNames || "none"}`} className="flex justify-between gap-4">
                    <span>
                      {item.itemName}
                      {item.variantName ? ` (${item.variantName})` : ""}
                      {item.addonNames ? ` + ${item.addonNames}` : ""}
                      {" "}x {item.quantity}
                    </span>
                    <span>Rs. {item.lineTotal}</span>
                  </div>
                ))}
              </div>
            </article>
          ))}
          {orders.length === 0 && <p className="text-sand-700">No orders yet for this table.</p>}
        </div>

        <section className="rounded-3xl bg-white p-5">
          <h2 className="font-display text-2xl text-sand-900">Rate Served Items</h2>
          {reviewMessage && <p className="mt-3 text-sm font-semibold text-emerald-700">{reviewMessage}</p>}
          {servedItems.length > 0 ? (
            <form className="mt-4 grid gap-3" onSubmit={handleReviewSubmit}>
              <select
                value={reviewForm.menuItemId}
                onChange={(event) => setReviewForm((current) => ({ ...current, menuItemId: event.target.value }))}
                required
                className="rounded-2xl border border-sand-200 px-4 py-3"
              >
                <option value="">Choose served item</option>
                {servedItems.map((item) => (
                  <option key={item.menuItemId} value={item.menuItemId}>
                    {item.itemName}
                  </option>
                ))}
              </select>
              <div className="grid gap-3 md:grid-cols-[1fr_140px]">
                <input
                  value={reviewForm.customerName}
                  onChange={(event) => setReviewForm((current) => ({ ...current, customerName: event.target.value }))}
                  placeholder="Name (optional)"
                  className="rounded-2xl border border-sand-200 px-4 py-3"
                />
                <select
                  value={reviewForm.rating}
                  onChange={(event) => setReviewForm((current) => ({ ...current, rating: event.target.value }))}
                  className="rounded-2xl border border-sand-200 px-4 py-3"
                >
                  {[5, 4, 3, 2, 1].map((rating) => (
                    <option key={rating} value={rating}>
                      {rating} rating
                    </option>
                  ))}
                </select>
              </div>
              <textarea
                value={reviewForm.comment}
                onChange={(event) => setReviewForm((current) => ({ ...current, comment: event.target.value }))}
                placeholder="What should the kitchen know?"
                rows={3}
                className="rounded-2xl border border-sand-200 px-4 py-3"
              />
              <button type="submit" className="rounded-2xl bg-sand-900 px-5 py-3 font-semibold text-white">
                Submit Review
              </button>
            </form>
          ) : (
            <p className="mt-3 text-sm text-sand-600">Reviews open once an order is marked served.</p>
          )}
        </section>

        {reviews.length > 0 && (
          <section className="rounded-3xl bg-white p-5">
            <h2 className="font-display text-2xl text-sand-900">Table Reviews</h2>
            <div className="mt-4 space-y-3">
              {reviews.map((review) => (
                <article key={review.id} className="rounded-2xl bg-sand-50 p-4 text-sm text-sand-700">
                  <p className="font-semibold text-sand-900">
                    {review.itemName} - {review.rating} rating
                  </p>
                  {review.comment && <p className="mt-1">{review.comment}</p>}
                </article>
              ))}
            </div>
          </section>
        )}
      </div>
    </main>
  );
}
