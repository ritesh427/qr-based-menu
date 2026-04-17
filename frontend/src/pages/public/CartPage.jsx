import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { placeOrder } from "../../api/menuApi";
import { useCart } from "../../context/CartContext";

export default function CartPage() {
  const { qrToken } = useParams();
  const navigate = useNavigate();
  const { items, updateQuantity, total, clearCart } = useCart();
  const [customerName, setCustomerName] = useState("");
  const [notes, setNotes] = useState("");
  const [loading, setLoading] = useState(false);

  const handleOrder = async () => {
    setLoading(true);
    try {
      await placeOrder({
        qrToken,
        customerName,
        notes,
        items: items.map((item) => ({ menuItemId: item.id, quantity: item.quantity }))
      });
      clearCart();
      navigate(`/orders/${qrToken}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto max-w-3xl px-4 py-6 md:px-8">
      <div className="glass-card space-y-5 p-6">
        <div className="flex items-center justify-between">
          <h1 className="font-display text-3xl text-sand-900">Cart & Checkout</h1>
          <div className="flex items-center gap-4">
            <Link to={`/orders/${qrToken}`} className="text-sm font-semibold text-sand-900">
              My Orders
            </Link>
            <Link to={`/menu/${qrToken}`} className="text-sm font-semibold text-paprika-700">
              Back to Menu
            </Link>
          </div>
        </div>
        {items.length === 0 ? (
          <p className="text-sand-700">Your cart is empty.</p>
        ) : (
          <>
            <div className="space-y-3">
              {items.map((item) => (
                <div key={item.id} className="flex items-center justify-between rounded-2xl bg-white p-4">
                  <div>
                    <p className="font-semibold text-sand-900">{item.name}</p>
                    <p className="text-sm text-sand-600">Rs. {item.price}</p>
                    <p className="text-sm font-medium text-sand-700">Line Total: Rs. {(Number(item.price) * item.quantity).toFixed(2)}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => updateQuantity(item.id, item.quantity - 1)}
                      className="h-9 w-9 rounded-full bg-sand-100"
                    >
                      -
                    </button>
                    <span className="w-8 text-center">{item.quantity}</span>
                    <button
                      type="button"
                      onClick={() => updateQuantity(item.id, item.quantity + 1)}
                      className="h-9 w-9 rounded-full bg-sand-100"
                    >
                      +
                    </button>
                  </div>
                </div>
              ))}
            </div>
            <input
              value={customerName}
              onChange={(event) => setCustomerName(event.target.value)}
              placeholder="Name for the order (optional)"
              className="w-full rounded-2xl border border-sand-200 bg-white px-4 py-3"
            />
            <textarea
              value={notes}
              onChange={(event) => setNotes(event.target.value)}
              placeholder="Special instructions"
              rows={4}
              className="w-full rounded-2xl border border-sand-200 bg-white px-4 py-3"
            />
            <div className="flex items-center justify-between">
              <p className="text-lg font-bold text-sand-900">Total: Rs. {total.toFixed(2)}</p>
              <button
                type="button"
                onClick={handleOrder}
                disabled={loading}
                className="rounded-2xl bg-paprika-500 px-5 py-3 font-semibold text-white disabled:opacity-50"
              >
                {loading ? "Placing..." : "Place Order"}
              </button>
            </div>
          </>
        )}
      </div>
    </main>
  );
}
