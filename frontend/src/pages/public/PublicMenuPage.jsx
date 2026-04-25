import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import CategoryTabs from "../../components/CategoryTabs";
import MenuItemCard from "../../components/MenuItemCard";
import { createServiceRequest, fetchMenu } from "../../api/menuApi";
import { useCart } from "../../context/CartContext";
import { getApiBaseUrl } from "../../utils/runtime";

export default function PublicMenuPage() {
  const { qrToken } = useParams();
  const { addItem, items, itemCount } = useCart();
  const [menu, setMenu] = useState(null);
  const [error, setError] = useState("");
  const [activeCategory, setActiveCategory] = useState(null);
  const [search, setSearch] = useState("");
  const [vegOnly, setVegOnly] = useState(false);
  const [serviceMessage, setServiceMessage] = useState("");
  const [serviceLoading, setServiceLoading] = useState("");

  useEffect(() => {
    setError("");
    fetchMenu(qrToken)
      .then((data) => {
        setMenu(data);
        setActiveCategory(data.categories[0]?.id ?? null);
      })
      .catch(() => {
        setError(`Unable to load the menu. Make sure the backend is running on ${getApiBaseUrl()}.`);
      });
  }, [qrToken]);

  const filteredCategories = useMemo(() => {
    if (!menu) return [];
    return menu.categories
      .map((category) => ({
        ...category,
        items: category.items.filter((item) => {
          const matchesSearch = `${item.name} ${item.description}`.toLowerCase().includes(search.toLowerCase());
          const matchesVeg = vegOnly ? item.vegetarian : true;
          return matchesSearch && matchesVeg;
        })
      }))
      .filter((category) => category.items.length > 0);
  }, [menu, search, vegOnly]);

  const visibleCategory =
    filteredCategories.find((category) => category.id === activeCategory) || filteredCategories[0];

  const requestService = async (type) => {
    setServiceLoading(type);
    try {
      await createServiceRequest({ qrToken, type });
      setServiceMessage(type === "CALL_WAITER" ? "Waiter has been notified." : "Bill request sent to the counter.");
    } finally {
      setServiceLoading("");
    }
  };

  if (!menu) {
    return (
      <div className="p-6 text-center text-sand-700">
        {error || "Loading menu..."}
      </div>
    );
  }

  return (
    <main className="mx-auto max-w-6xl px-4 py-5 md:px-8">
      <section className="glass-card relative overflow-hidden p-6 md:p-8">
        <div className="pointer-events-none absolute inset-y-0 right-0 hidden w-1/3 bg-gradient-to-l from-paprika-100 to-transparent md:block" />
        <p className="text-xs uppercase tracking-[0.4em] text-paprika-500">Table {menu.tableNumber}</p>
        <h1 className="mt-3 max-w-xl font-display text-4xl text-sand-900 md:text-5xl">{menu.restaurantName}</h1>
        <p className="mt-3 max-w-2xl text-sm text-sand-700 md:text-base">
          Scan, browse, and order without waiting. Your table number is already attached to this session.
        </p>
        {serviceMessage && (
          <p className="mt-4 rounded-2xl bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {serviceMessage}
          </p>
        )}
        <div className="mt-6 flex flex-col gap-3 md:flex-row">
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search dishes, drinks, and specials"
            className="w-full rounded-2xl border border-sand-200 bg-white px-4 py-3 outline-none ring-0"
          />
          <button
            type="button"
            onClick={() => setVegOnly((current) => !current)}
            className={`rounded-2xl px-4 py-3 text-sm font-semibold transition ${
              vegOnly ? "bg-emerald-600 text-white" : "bg-white text-sand-700"
            }`}
          >
            {vegOnly ? "Veg Only On" : "Veg Only"}
          </button>
          <Link
            to={`/cart/${qrToken}`}
            className="rounded-2xl bg-paprika-500 px-5 py-3 text-center text-sm font-semibold text-white shadow-glow"
          >
            View Cart ({itemCount})
          </Link>
          <Link
            to={`/orders/${qrToken}`}
            className="rounded-2xl border border-sand-300 bg-white px-5 py-3 text-center text-sm font-semibold text-sand-900"
          >
            My Orders
          </Link>
          <Link
            to={`/bill/${qrToken}`}
            className="rounded-2xl border border-sand-300 bg-white px-5 py-3 text-center text-sm font-semibold text-sand-900"
          >
            Final Bill
          </Link>
        </div>
        <div className="mt-4 grid gap-3 md:grid-cols-2">
          <button
            type="button"
            onClick={() => requestService("CALL_WAITER")}
            disabled={serviceLoading === "CALL_WAITER"}
            className="rounded-2xl border border-sand-300 bg-white px-5 py-3 text-sm font-semibold text-sand-900 disabled:opacity-60"
          >
            {serviceLoading === "CALL_WAITER" ? "Sending..." : "Call Waiter"}
          </button>
          <button
            type="button"
            onClick={() => requestService("REQUEST_BILL")}
            disabled={serviceLoading === "REQUEST_BILL"}
            className="rounded-2xl border border-sand-300 bg-white px-5 py-3 text-sm font-semibold text-sand-900 disabled:opacity-60"
          >
            {serviceLoading === "REQUEST_BILL" ? "Sending..." : "Request Bill"}
          </button>
        </div>
      </section>

      <section className="mt-6 space-y-6">
        <CategoryTabs
          categories={filteredCategories}
          active={visibleCategory?.id}
          onChange={setActiveCategory}
        />
        {visibleCategory ? (
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {visibleCategory.items.map((item) => (
              <MenuItemCard
                key={item.id}
                item={item}
                onAdd={addItem}
                cartItems={items}
              />
            ))}
          </div>
        ) : (
          <div className="glass-card p-8 text-center text-sand-700">No items matched your filters.</div>
        )}
      </section>
    </main>
  );
}
