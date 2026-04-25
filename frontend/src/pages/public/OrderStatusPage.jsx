import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { fetchOrderStatus } from "../../api/menuApi";
import OrderStatusBadge from "../../components/OrderStatusBadge";
import { getApiBaseUrl } from "../../utils/runtime";

export default function OrderStatusPage() {
  const { qrToken } = useParams();
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    fetchOrderStatus(qrToken).then(setOrders);
  }, [qrToken]);

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
                    {order.paymentMethod?.replaceAll("_", " ")} • {order.paymentStatus}
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
      </div>
    </main>
  );
}
