import { useEffect, useMemo, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import {
  createTables,
  createCategory,
  createMenuItem,
  deleteMenuItem,
  fetchAdminCategories,
  fetchAdminMenuItems,
  fetchOrderStats,
  fetchOrders,
  fetchQrCodes,
  toggleMenuItemAvailability,
  uploadMenuImage,
  updateOrderStatus
} from "../../api/menuApi";
import OrderStatusBadge from "../../components/OrderStatusBadge";
import { getApiBaseUrl } from "../../utils/runtime";

const defaultItem = {
  name: "",
  description: "",
  price: "",
  imageUrl: "",
  available: true,
  vegetarian: true,
  estimatedPreparationTime: 10,
  categoryId: ""
};

export default function AdminDashboardPage() {
  const [categories, setCategories] = useState([]);
  const [menuItems, setMenuItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [stats, setStats] = useState(null);
  const [qrs, setQrs] = useState([]);
  const [categoryName, setCategoryName] = useState("");
  const [itemForm, setItemForm] = useState(defaultItem);
  const [itemImageFile, setItemImageFile] = useState(null);
  const [itemImagePreview, setItemImagePreview] = useState("");
  const [tableForm, setTableForm] = useState({ startTableNumber: "", count: 1 });

  const load = async () => {
    const [categoryData, menuData, orderData, statsData, qrData] = await Promise.all([
      fetchAdminCategories(),
      fetchAdminMenuItems(),
      fetchOrders(),
      fetchOrderStats(),
      fetchQrCodes()
    ]);
    setCategories(categoryData);
    setMenuItems(menuData);
    setOrders(orderData);
    setStats(statsData);
    setQrs(qrData);
    if (!itemForm.categoryId && categoryData[0]) {
      setItemForm((current) => ({ ...current, categoryId: categoryData[0].id }));
    }
  };

  useEffect(() => {
    load();
  }, []);

  useEffect(() => {
    const restaurantId = localStorage.getItem("restaurantId");
    if (!restaurantId) {
      return undefined;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(`${getApiBaseUrl()}/ws/orders`),
      reconnectDelay: 5000
    });

    client.onConnect = () => {
      client.subscribe(`/topic/orders/${restaurantId}`, (message) => {
        const incoming = JSON.parse(message.body);
        setOrders((current) => {
          const existing = current.find((entry) => entry.id === incoming.id);
          if (!existing) {
            return [incoming, ...current];
          }
          return current.map((entry) => (entry.id === incoming.id ? incoming : entry));
        });
      });
    };

    client.activate();
    return () => client.deactivate();
  }, []);

  const statCards = useMemo(
    () =>
      stats
        ? [
            ["Created", stats.created],
            ["Confirmed", stats.confirmed],
            ["Preparing", stats.preparing],
            ["Ready", stats.ready],
            ["Served", stats.served]
          ]
        : [],
    [stats]
  );

  const handleCategorySubmit = async (event) => {
    event.preventDefault();
    await createCategory({ name: categoryName, description: `${categoryName} items` });
    setCategoryName("");
    load();
  };

  const handleItemSubmit = async (event) => {
    event.preventDefault();
    let imageUrl = itemForm.imageUrl;

    if (itemImageFile) {
      const upload = await uploadMenuImage(itemImageFile);
      imageUrl = upload.imageUrl;
    }

    await createMenuItem({
      ...itemForm,
      price: Number(itemForm.price),
      categoryId: Number(itemForm.categoryId),
      imageUrl
    });
    setItemForm({ ...defaultItem, categoryId: categories[0]?.id || "" });
    setItemImageFile(null);
    setItemImagePreview("");
    load();
  };

  const handleTableSubmit = async (event) => {
    event.preventDefault();
    await createTables({
      startTableNumber: Number(tableForm.startTableNumber),
      count: Number(tableForm.count)
    });
    setTableForm({ startTableNumber: "", count: 1 });
    load();
  };

  return (
    <div className="grid gap-6 xl:grid-cols-[1.3fr_0.7fr]">
      <div className="space-y-6">
        <section className="grid gap-4 md:grid-cols-5">
          {statCards.map(([label, value]) => (
            <div key={label} className="glass-card p-4">
              <p className="text-xs uppercase tracking-[0.3em] text-sand-500">{label}</p>
              <p className="mt-2 font-display text-3xl text-sand-900">{value}</p>
            </div>
          ))}
        </section>

        <section className="glass-card p-6">
          <h2 className="font-display text-2xl text-sand-900">Live Orders</h2>
          <div className="mt-5 space-y-4">
            {orders.map((order) => (
              <article key={order.id} className="rounded-3xl bg-white p-5">
                <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p className="text-sm text-sand-600">Order #{order.id}</p>
                    <p className="font-semibold text-sand-900">
                      Table {order.tableNumber} • Rs. {order.totalAmount}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <OrderStatusBadge status={order.status} />
                    <select
                      value={order.status}
                      onChange={async (event) => {
                        await updateOrderStatus(order.id, event.target.value);
                        load();
                      }}
                      className="rounded-xl border border-sand-200 px-3 py-2 text-sm"
                    >
                      {["CREATED", "CONFIRMED", "PREPARING", "READY", "SERVED"].map((status) => (
                        <option key={status} value={status}>
                          {status}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="mt-4 grid gap-2 text-sm text-sand-700 md:grid-cols-2">
                  {order.items.map((item) => (
                    <div key={`${order.id}-${item.menuItemId}`} className="rounded-2xl bg-sand-50 px-3 py-2">
                      {item.itemName} x {item.quantity}
                    </div>
                  ))}
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="glass-card p-6">
          <h2 className="font-display text-2xl text-sand-900">Menu Items</h2>
          <div className="mt-5 grid gap-4">
            {menuItems.map((item) => (
              <div key={item.id} className="flex flex-col gap-4 rounded-3xl bg-white p-4 md:flex-row md:items-center md:justify-between">
                <div>
                  <p className="font-semibold text-sand-900">{item.name}</p>
                  <p className="text-sm text-sand-600">
                    {item.categoryName} • Rs. {item.price}
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  <button
                    type="button"
                    onClick={async () => {
                      await toggleMenuItemAvailability(item.id, !item.available);
                      load();
                    }}
                    className={`rounded-full px-4 py-2 text-sm font-semibold ${
                      item.available ? "bg-emerald-100 text-emerald-700" : "bg-red-100 text-red-700"
                    }`}
                  >
                    {item.available ? "In Stock" : "Out of Stock"}
                  </button>
                  <button
                    type="button"
                    onClick={async () => {
                      await deleteMenuItem(item.id);
                      load();
                    }}
                    className="rounded-full bg-sand-900 px-4 py-2 text-sm font-semibold text-white"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>

      <div className="space-y-6">
        <section className="glass-card p-6">
          <h2 className="font-display text-2xl text-sand-900">Add Category</h2>
          <form className="mt-4 space-y-3" onSubmit={handleCategorySubmit}>
            <input
              value={categoryName}
              onChange={(event) => setCategoryName(event.target.value)}
              placeholder="Category name"
              className="w-full rounded-2xl border border-sand-200 px-4 py-3"
            />
            <button type="submit" className="w-full rounded-2xl bg-paprika-500 px-4 py-3 font-semibold text-white">
              Create Category
            </button>
          </form>
        </section>

        <section className="glass-card p-6">
          <h2 className="font-display text-2xl text-sand-900">Add Menu Item</h2>
          <form className="mt-4 space-y-3" onSubmit={handleItemSubmit}>
            <input value={itemForm.name} onChange={(e) => setItemForm({ ...itemForm, name: e.target.value })} placeholder="Name" className="w-full rounded-2xl border border-sand-200 px-4 py-3" />
            <textarea value={itemForm.description} onChange={(e) => setItemForm({ ...itemForm, description: e.target.value })} placeholder="Description" rows={3} className="w-full rounded-2xl border border-sand-200 px-4 py-3" />
            <input value={itemForm.price} onChange={(e) => setItemForm({ ...itemForm, price: e.target.value })} placeholder="Price" className="w-full rounded-2xl border border-sand-200 px-4 py-3" />
            <input value={itemForm.imageUrl} onChange={(e) => setItemForm({ ...itemForm, imageUrl: e.target.value })} placeholder="Image URL (optional if uploading file)" className="w-full rounded-2xl border border-sand-200 px-4 py-3" />
            <label className="block rounded-2xl border border-dashed border-sand-300 bg-white px-4 py-4 text-sm text-sand-700">
              <span className="mb-2 block font-semibold text-sand-900">Upload Item Image</span>
              <input
                type="file"
                accept="image/png,image/jpeg,image/webp"
                onChange={(e) => {
                  const file = e.target.files?.[0] || null;
                  setItemImageFile(file);
                  setItemImagePreview(file ? URL.createObjectURL(file) : "");
                }}
                className="block w-full text-sm"
              />
            </label>
            {itemImagePreview && (
              <img
                src={itemImagePreview}
                alt="Menu item preview"
                className="h-36 w-full rounded-2xl object-cover"
              />
            )}
            <select value={itemForm.categoryId} onChange={(e) => setItemForm({ ...itemForm, categoryId: e.target.value })} className="w-full rounded-2xl border border-sand-200 px-4 py-3">
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
            <div className="grid grid-cols-2 gap-3">
              <label className="rounded-2xl bg-white px-4 py-3 text-sm">
                <input type="checkbox" checked={itemForm.available} onChange={() => setItemForm({ ...itemForm, available: !itemForm.available })} /> Available
              </label>
              <label className="rounded-2xl bg-white px-4 py-3 text-sm">
                <input type="checkbox" checked={itemForm.vegetarian} onChange={() => setItemForm({ ...itemForm, vegetarian: !itemForm.vegetarian })} /> Vegetarian
              </label>
            </div>
            <button type="submit" className="w-full rounded-2xl bg-sand-900 px-4 py-3 font-semibold text-white">
              Create Item
            </button>
          </form>
        </section>

        <section className="glass-card p-6">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-2xl text-sand-900">Tables & QR Codes</h2>
          </div>
          <form className="mt-4 grid gap-3 md:grid-cols-[1fr_120px_auto]" onSubmit={handleTableSubmit}>
            <input
              value={tableForm.startTableNumber}
              onChange={(event) => setTableForm((current) => ({ ...current, startTableNumber: event.target.value }))}
              placeholder="Start table number"
              className="w-full rounded-2xl border border-sand-200 px-4 py-3"
            />
            <input
              type="number"
              min="1"
              value={tableForm.count}
              onChange={(event) => setTableForm((current) => ({ ...current, count: event.target.value }))}
              className="w-full rounded-2xl border border-sand-200 px-4 py-3"
            />
            <button type="submit" className="rounded-2xl bg-paprika-500 px-4 py-3 font-semibold text-white">
              Create Tables
            </button>
          </form>
          <div className="mt-4 space-y-4">
            {qrs.map((qr) => (
              <article key={qr.qrToken} className="rounded-3xl bg-white p-4">
                <p className="font-semibold text-sand-900">Table {qr.tableNumber}</p>
                <img
                  src={`data:image/png;base64,${qr.imageBase64}`}
                  alt={`QR for table ${qr.tableNumber}`}
                  className="mt-3 h-28 w-28 rounded-2xl bg-sand-50 p-2"
                />
                <p className="mt-3 break-all text-xs text-sand-600">{qr.menuUrl}</p>
              </article>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
