import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { placeOrder, quoteOrder } from "../../api/menuApi";
import { useCart } from "../../context/CartContext";

const paymentMethods = [
  { value: "PAY_AT_COUNTER", label: "Pay at Counter" },
  { value: "CASH", label: "Cash" },
  { value: "UPI", label: "UPI" },
  { value: "CARD", label: "Card" }
];

export default function CartPage() {
  const { qrToken } = useParams();
  const navigate = useNavigate();
  const { items, updateQuantity, total, clearCart } = useCart();
  const [customerName, setCustomerName] = useState("");
  const [notes, setNotes] = useState("");
  const [couponCode, setCouponCode] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("PAY_AT_COUNTER");
  const [payNow, setPayNow] = useState(false);
  const [quote, setQuote] = useState(null);
  const [quoteError, setQuoteError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (items.length === 0) {
      setQuote(null);
      setQuoteError("");
      return;
    }

    quoteOrder({
      qrToken,
      couponCode: couponCode || null,
      paymentMethod,
      items: items.map((item) => ({
        menuItemId: item.id,
        variantId: item.variantId,
        addonIds: item.addonIds,
        quantity: item.quantity
      }))
    })
      .then((data) => {
        setQuote(data);
        setQuoteError("");
      })
      .catch((error) => {
        setQuote(null);
        setQuoteError(error?.response?.data?.message || "Unable to apply quote right now.");
      });
  }, [items, qrToken, couponCode, paymentMethod]);

  const handleOrder = async () => {
    setLoading(true);
    try {
      await placeOrder({
        qrToken,
        customerName,
        notes,
        couponCode: couponCode || null,
        paymentMethod,
        payNow,
        items: items.map((item) => ({
          menuItemId: item.id,
          variantId: item.variantId,
          addonIds: item.addonIds,
          quantity: item.quantity
        }))
      });
      clearCart();
      navigate(`/bill/${qrToken}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto max-w-5xl px-4 py-6 md:px-8">
      <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
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
                  <div key={item.cartKey} className="flex items-center justify-between rounded-2xl bg-white p-4">
                    <div>
                      <p className="font-semibold text-sand-900">{item.name}</p>
                      {item.variantName && <p className="text-sm text-sand-600">Variant: {item.variantName}</p>}
                      {item.addonNames?.length > 0 && (
                        <p className="text-sm text-sand-600">Add-ons: {item.addonNames.join(", ")}</p>
                      )}
                      <p className="text-sm text-sand-600">Rs. {item.price}</p>
                      <p className="text-sm text-sand-600">ETA: {item.estimatedPreparationTime || 10} min</p>
                      <p className="text-sm text-sand-600">Available: {item.availableQuantity}</p>
                      <p className="text-sm font-medium text-sand-700">
                        Line Total: Rs. {(Number(item.price) * item.quantity).toFixed(2)}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => updateQuantity(item.cartKey, item.quantity - 1)}
                        className="h-9 w-9 rounded-full bg-sand-100"
                      >
                        -
                      </button>
                      <span className="w-8 text-center">{item.quantity}</span>
                      <button
                        type="button"
                        onClick={() => updateQuantity(item.cartKey, item.quantity + 1)}
                        disabled={item.quantity >= (item.availableQuantity ?? item.quantity)}
                        className="h-9 w-9 rounded-full bg-sand-100 disabled:cursor-not-allowed disabled:opacity-50"
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
            </>
          )}
        </div>

        <aside className="glass-card space-y-4 p-6">
          <h2 className="font-display text-2xl text-sand-900">Payment & Bill</h2>
          <input
            value={couponCode}
            onChange={(event) => setCouponCode(event.target.value.toUpperCase())}
            placeholder="Coupon code"
            className="w-full rounded-2xl border border-sand-200 bg-white px-4 py-3"
          />
          <select
            value={paymentMethod}
            onChange={(event) => setPaymentMethod(event.target.value)}
            className="w-full rounded-2xl border border-sand-200 bg-white px-4 py-3"
          >
            {paymentMethods.map((method) => (
              <option key={method.value} value={method.value}>
                {method.label}
              </option>
            ))}
          </select>
          <label className="flex items-center gap-3 rounded-2xl bg-white px-4 py-3 text-sm text-sand-700">
            <input
              type="checkbox"
              checked={payNow}
              onChange={() => setPayNow((current) => !current)}
              disabled={paymentMethod === "PAY_AT_COUNTER"}
            />
            Pay now and generate payment reference
          </label>
          {quoteError && (
            <p className="rounded-2xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">{quoteError}</p>
          )}
          <div className="rounded-3xl bg-white p-4 text-sm text-sand-700">
            <div className="flex justify-between">
              <span>Cart Subtotal</span>
              <span>Rs. {total.toFixed(2)}</span>
            </div>
            <div className="mt-2 flex justify-between">
              <span>Discount</span>
              <span>Rs. {Number(quote?.discountAmount ?? 0).toFixed(2)}</span>
            </div>
            <div className="mt-2 flex justify-between">
              <span>Tax</span>
              <span>Rs. {Number(quote?.taxAmount ?? 0).toFixed(2)}</span>
            </div>
            <div className="mt-2 flex justify-between">
              <span>Service Charge</span>
              <span>Rs. {Number(quote?.serviceChargeAmount ?? 0).toFixed(2)}</span>
            </div>
            <div className="mt-4 flex justify-between border-t border-sand-100 pt-4 text-base font-bold text-sand-900">
              <span>Payable</span>
              <span>Rs. {quote ? Number(quote.payableAmount).toFixed(2) : total.toFixed(2)}</span>
            </div>
            {quote?.appliedCouponCode && (
              <p className="mt-3 rounded-2xl bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-700">
                Coupon applied: {quote.appliedCouponCode}
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={handleOrder}
            disabled={loading || items.length === 0 || !!quoteError}
            className="w-full rounded-2xl bg-paprika-500 px-5 py-3 font-semibold text-white disabled:opacity-50"
          >
            {loading ? "Placing..." : payNow ? "Pay & Place Order" : "Place Order"}
          </button>
          <Link to={`/bill/${qrToken}`} className="block text-center text-sm font-semibold text-sand-900">
            View Current Table Bill
          </Link>
        </aside>
      </div>
    </main>
  );
}
