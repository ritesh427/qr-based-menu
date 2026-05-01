import { useEffect, useMemo, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import {
  createCoupon,
  createTables,
  createCategory,
  createMenuItem,
  deleteMenuItem,
  fetchAdminCategories,
  fetchAdminFinalBill,
  fetchAdminMenuItems,
  fetchAdminReviews,
  fetchCoupons,
  fetchKitchenOrders,
  fetchOrderStats,
  fetchOrders,
  fetchQrCodes,
  fetchServiceRequests,
  fetchTableSessions,
  toggleMenuItemAvailability,
  uploadMenuImage,
  updateMenuItem,
  updateOrderPayment,
  updateServiceRequestStatus,
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
  stockQuantity: 0,
  estimatedPreparationTime: 10,
  categoryId: "",
  variants: [],
  addons: [],
  translations: []
};

const defaultVariant = {
  name: "",
  priceAdjustment: 0,
  stockQuantity: 0,
  available: true,
  estimatedPreparationTime: 10
};

const defaultAddon = {
  name: "",
  price: 0,
  stockQuantity: 0,
  available: true,
  estimatedPreparationTime: 10
};

const defaultTranslation = {
  languageCode: "hi",
  name: "",
  description: ""
};

export default function AdminDashboardPage() {
  const [categories, setCategories] = useState([]);
  const [menuItems, setMenuItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [stats, setStats] = useState(null);
  const [qrs, setQrs] = useState([]);
  const [kitchenOrders, setKitchenOrders] = useState([]);
  const [serviceRequests, setServiceRequests] = useState([]);
  const [tableSessions, setTableSessions] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [categoryName, setCategoryName] = useState("");
  const [coupons, setCoupons] = useState([]);
  const [couponForm, setCouponForm] = useState({
    code: "",
    description: "",
    discountValue: "",
    percentage: true,
    active: true,
    minimumOrderAmount: 0,
    maxDiscountAmount: 0
  });
  const [itemForm, setItemForm] = useState(defaultItem);
  const [itemImageFile, setItemImageFile] = useState(null);
  const [itemImagePreview, setItemImagePreview] = useState("");
  const [tableForm, setTableForm] = useState({ startTableNumber: "", count: 1 });
  const [stockDrafts, setStockDrafts] = useState({});
  const [selectedBillToken, setSelectedBillToken] = useState("");
  const [billPreview, setBillPreview] = useState(null);
  const [activeWorkspace, setActiveWorkspace] = useState("operations");

  const load = async () => {
    const [categoryData, menuData, orderData, statsData, qrData, kitchenData, requestData, sessionData, couponData, reviewData] = await Promise.all([
      fetchAdminCategories(),
      fetchAdminMenuItems(),
      fetchOrders(),
      fetchOrderStats(),
      fetchQrCodes(),
      fetchKitchenOrders(),
      fetchServiceRequests(),
      fetchTableSessions(),
      fetchCoupons(),
      fetchAdminReviews()
    ]);
    setCategories(categoryData);
    setMenuItems(menuData);
    setOrders(orderData);
    setStats(statsData);
    setQrs(qrData);
    setKitchenOrders(kitchenData);
    setServiceRequests(requestData);
    setTableSessions(sessionData);
    setCoupons(couponData);
    setReviews(reviewData);
    setStockDrafts(Object.fromEntries(menuData.map((item) => [item.id, item.stockQuantity ?? 0])));
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
        setKitchenOrders((current) => {
          const activeStatuses = ["CONFIRMED", "PREPARING", "READY"];
          const existing = current.find((entry) => entry.id === incoming.id);
          if (!activeStatuses.includes(incoming.status)) {
            return current.filter((entry) => entry.id !== incoming.id);
          }
          if (!existing) {
            return [...current, incoming];
          }
          return current.map((entry) => (entry.id === incoming.id ? incoming : entry));
        });
      });
      client.subscribe(`/topic/assistance/${restaurantId}`, (message) => {
        const incoming = JSON.parse(message.body);
        setServiceRequests((current) => {
          const existing = current.find((entry) => entry.id === incoming.id);
          if (!existing) {
            return [incoming, ...current];
          }
          return current.map((entry) => (entry.id === incoming.id ? incoming : entry));
        });
      });
      client.subscribe(`/topic/table-sessions/${restaurantId}`, (message) => {
        const incoming = JSON.parse(message.body);
        setTableSessions((current) => {
          const existing = current.find((entry) => entry.qrToken === incoming.qrToken);
          if (!existing) {
            return [...current, incoming].sort((a, b) => a.tableNumber - b.tableNumber);
          }
          return current
            .map((entry) => (entry.qrToken === incoming.qrToken ? incoming : entry))
            .sort((a, b) => a.tableNumber - b.tableNumber);
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

  const workspaceTabs = useMemo(
    () => [
      { id: "operations", label: "Operations", detail: `${orders.length} orders` },
      { id: "menu", label: "Menu Setup", detail: `${menuItems.length} items` },
      { id: "tables", label: "Tables & Bills", detail: `${qrs.length} tables` },
      { id: "coupons", label: "Coupons", detail: `${coupons.length} active` },
      { id: "growth", label: "Growth", detail: `${reviews.length} reviews` }
    ],
    [orders.length, menuItems.length, qrs.length, coupons.length, reviews.length]
  );

  const handleCategorySubmit = async (event) => {
    event.preventDefault();
    await createCategory({ name: categoryName, description: `${categoryName} items` });
    setCategoryName("");
    load();
  };

  const handleCouponSubmit = async (event) => {
    event.preventDefault();
    await createCoupon({
      ...couponForm,
      discountValue: Number(couponForm.discountValue),
      minimumOrderAmount: Number(couponForm.minimumOrderAmount),
      maxDiscountAmount: Number(couponForm.maxDiscountAmount)
    });
    setCouponForm({
      code: "",
      description: "",
      discountValue: "",
      percentage: true,
      active: true,
      minimumOrderAmount: 0,
      maxDiscountAmount: 0
    });
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
      stockQuantity: Number(itemForm.stockQuantity),
      categoryId: Number(itemForm.categoryId),
      estimatedPreparationTime: Number(itemForm.estimatedPreparationTime),
      variants: itemForm.variants.map((variant) => ({
        ...variant,
        priceAdjustment: Number(variant.priceAdjustment),
        stockQuantity: Number(variant.stockQuantity),
        estimatedPreparationTime: Number(variant.estimatedPreparationTime)
      })),
      addons: itemForm.addons.map((addon) => ({
        ...addon,
        price: Number(addon.price),
        stockQuantity: Number(addon.stockQuantity),
        estimatedPreparationTime: Number(addon.estimatedPreparationTime)
      })),
      translations: itemForm.translations.map((translation) => ({
        ...translation,
        languageCode: translation.languageCode.toLowerCase()
      })),
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

  const handleStockSave = async (item) => {
    const nextStock = Math.max(0, Number(stockDrafts[item.id] ?? item.stockQuantity ?? 0));
    await updateMenuItem(item.id, {
      name: item.name,
      description: item.description,
      price: Number(item.price),
      imageUrl: item.imageUrl,
      available: item.available && nextStock > 0,
      vegetarian: item.vegetarian,
      stockQuantity: nextStock,
      estimatedPreparationTime: item.estimatedPreparationTime ?? 10,
      categoryId: item.categoryId,
      variants: (item.variants ?? []).map((variant) => ({
        name: variant.name,
        priceAdjustment: Number(variant.priceAdjustment),
        stockQuantity: Number(variant.stockQuantity),
        available: variant.available,
        estimatedPreparationTime: Number(variant.estimatedPreparationTime ?? 10)
      })),
      addons: (item.addons ?? []).map((addon) => ({
        name: addon.name,
        price: Number(addon.price),
        stockQuantity: Number(addon.stockQuantity),
        available: addon.available,
        estimatedPreparationTime: Number(addon.estimatedPreparationTime ?? 10)
      })),
      translations: (item.translations ?? []).map((translation) => ({
        languageCode: translation.languageCode,
        name: translation.name,
        description: translation.description
      }))
    });
    load();
  };

  const addVariantRow = () => {
    setItemForm((current) => ({
      ...current,
      variants: [...current.variants, { ...defaultVariant }]
    }));
  };

  const updateVariantRow = (index, field, value) => {
    setItemForm((current) => ({
      ...current,
      variants: current.variants.map((variant, variantIndex) =>
        variantIndex === index ? { ...variant, [field]: value } : variant
      )
    }));
  };

  const removeVariantRow = (index) => {
    setItemForm((current) => ({
      ...current,
      variants: current.variants.filter((_, variantIndex) => variantIndex !== index)
    }));
  };

  const addAddonRow = () => {
    setItemForm((current) => ({
      ...current,
      addons: [...current.addons, { ...defaultAddon }]
    }));
  };

  const updateAddonRow = (index, field, value) => {
    setItemForm((current) => ({
      ...current,
      addons: current.addons.map((addon, addonIndex) =>
        addonIndex === index ? { ...addon, [field]: value } : addon
      )
    }));
  };

  const removeAddonRow = (index) => {
    setItemForm((current) => ({
      ...current,
      addons: current.addons.filter((_, addonIndex) => addonIndex !== index)
    }));
  };

  const addTranslationRow = () => {
    setItemForm((current) => ({
      ...current,
      translations: [...current.translations, { ...defaultTranslation }]
    }));
  };

  const updateTranslationRow = (index, field, value) => {
    setItemForm((current) => ({
      ...current,
      translations: current.translations.map((translation, translationIndex) =>
        translationIndex === index ? { ...translation, [field]: value } : translation
      )
    }));
  };

  const removeTranslationRow = (index) => {
    setItemForm((current) => ({
      ...current,
      translations: current.translations.filter((_, translationIndex) => translationIndex !== index)
    }));
  };

  const loadBillPreview = async (qrToken) => {
    if (!qrToken) {
      setBillPreview(null);
      return;
    }
    setSelectedBillToken(qrToken);
    const data = await fetchAdminFinalBill(qrToken);
    setBillPreview(data);
  };

  return (
    <div className="space-y-6">
      <div className="space-y-6">
        <section className="grid gap-4 md:grid-cols-5">
          {statCards.map(([label, value]) => (
            <div key={label} className="glass-card p-4">
              <p className="text-xs uppercase tracking-[0.3em] text-sand-500">{label}</p>
              <p className="mt-2 font-display text-3xl text-sand-900">{value}</p>
            </div>
          ))}
        </section>

        <section className="glass-card p-2">
          <div className="grid gap-2 md:grid-cols-5">
            {workspaceTabs.map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveWorkspace(tab.id)}
                className={`rounded-2xl px-4 py-3 text-left transition ${
                  activeWorkspace === tab.id
                    ? "bg-sand-900 text-white shadow-glow"
                    : "bg-white text-sand-700 hover:bg-sand-100"
                }`}
              >
                <span className="block text-sm font-semibold">{tab.label}</span>
                <span className={`mt-1 block text-xs ${activeWorkspace === tab.id ? "text-sand-100" : "text-sand-500"}`}>
                  {tab.detail}
                </span>
              </button>
            ))}
          </div>
        </section>

        <section className={activeWorkspace === "operations" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Kitchen Board</h2>
          <div className="mt-5 grid gap-4 lg:grid-cols-3">
            {["CONFIRMED", "PREPARING", "READY"].map((lane) => (
              <div key={lane} className="rounded-3xl bg-white p-4">
                <h3 className="text-sm font-semibold uppercase tracking-[0.25em] text-sand-500">{lane}</h3>
                <div className="mt-4 space-y-3">
                  {kitchenOrders.filter((order) => order.status === lane).map((order) => (
                    <article key={order.id} className="rounded-2xl bg-sand-50 p-3">
                      <p className="font-semibold text-sand-900">Table {order.tableNumber}</p>
                      <p className="text-sm text-sand-600">Order #{order.id}</p>
                      <p className="text-sm text-sand-600">ETA {order.estimatedReadyInMinutes || 10} min</p>
                      <div className="mt-3 space-y-1 text-sm text-sand-700">
                        {order.items.map((item) => (
                          <div key={`${order.id}-${item.menuItemId}-${item.variantName || "base"}-${item.addonNames || "none"}`}>
                            {item.itemName}
                            {item.variantName ? ` (${item.variantName})` : ""}
                            {item.addonNames ? ` + ${item.addonNames}` : ""} x {item.quantity}
                          </div>
                        ))}
                      </div>
                    </article>
                  ))}
                  {kitchenOrders.filter((order) => order.status === lane).length === 0 && (
                    <p className="text-sm text-sand-500">No tickets in this lane.</p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className={activeWorkspace === "operations" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Live Orders</h2>
          <div className="mt-5 space-y-4">
            {orders.map((order) => (
              <article key={order.id} className="rounded-3xl bg-white p-5">
                <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p className="text-sm text-sand-600">Order #{order.id}</p>
                    <p className="font-semibold text-sand-900">
                      Table {order.tableNumber} - Rs. {order.totalAmount}
                    </p>
                    <p className="text-sm text-sand-600">ETA {order.estimatedReadyInMinutes || 10} min</p>
                    <p className="text-sm text-sand-600">
                      {order.paymentMethod?.replaceAll("_", " ")} - {order.paymentStatus}
                    </p>
                    {order.appliedCouponCode && (
                      <p className="text-sm text-emerald-700">Coupon: {order.appliedCouponCode}</p>
                    )}
                  </div>
                  <div className="flex flex-wrap items-center gap-3">
                    <OrderStatusBadge status={order.status} />
                    <select
                      value={order.paymentStatus}
                      onChange={async (event) => {
                        await updateOrderPayment(order.id, {
                          paymentStatus: event.target.value,
                          paymentMethod: order.paymentMethod
                        });
                        load();
                      }}
                      className="rounded-xl border border-sand-200 px-3 py-2 text-sm"
                    >
                      {["PENDING", "PAID"].map((status) => (
                        <option key={status} value={status}>
                          {status}
                        </option>
                      ))}
                    </select>
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
                    <div key={`${order.id}-${item.menuItemId}-${item.variantName || "base"}-${item.addonNames || "none"}`} className="rounded-2xl bg-sand-50 px-3 py-2">
                      {item.itemName}
                      {item.variantName ? ` (${item.variantName})` : ""}
                      {item.addonNames ? ` + ${item.addonNames}` : ""} x {item.quantity}
                    </div>
                  ))}
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className={activeWorkspace === "menu" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Menu Items</h2>
          <div className="mt-5 grid gap-4">
            {menuItems.map((item) => (
              <div key={item.id} className="flex flex-col gap-4 rounded-3xl bg-white p-4 md:flex-row md:items-center md:justify-between">
                <div>
                  <p className="font-semibold text-sand-900">{item.name}</p>
                  <p className="text-sm text-sand-600">
                    {item.categoryName} - Rs. {item.price}
                  </p>
                  <p className="text-sm text-sand-600">Stock: {item.stockQuantity}</p>
                  <p className="text-sm text-sand-600">ETA: {item.estimatedPreparationTime || 10} min</p>
                  <p className="text-sm text-sand-600">
                    {item.variants?.length || 0} variant(s) - {item.addons?.length || 0} add-on(s)
                  </p>
                  <p className="text-sm text-sand-600">
                    {Number(item.averageRating || 0).toFixed(1)} rating - {item.reviewCount || 0} review(s) - {item.orderCount || 0} order(s)
                  </p>
                </div>
                <div className="flex flex-wrap items-center gap-3">
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      min="0"
                      value={stockDrafts[item.id] ?? item.stockQuantity ?? 0}
                      onChange={(event) =>
                        setStockDrafts((current) => ({
                          ...current,
                          [item.id]: event.target.value
                        }))
                      }
                      className="w-24 rounded-xl border border-sand-200 px-3 py-2 text-sm"
                    />
                    <button
                      type="button"
                      onClick={() => handleStockSave(item)}
                      className="rounded-full bg-amber-100 px-4 py-2 text-sm font-semibold text-amber-800"
                    >
                      Save Stock
                    </button>
                  </div>
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
        <section className={activeWorkspace === "operations" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Service Requests</h2>
          <div className="mt-4 space-y-3">
            {serviceRequests.map((request) => (
              <article key={request.id} className="rounded-3xl bg-white p-4">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="font-semibold text-sand-900">Table {request.tableNumber}</p>
                    <p className="text-sm text-sand-600">{request.type.replace("_", " ")}</p>
                  </div>
                  <select
                    value={request.status}
                    onChange={async (event) => {
                      await updateServiceRequestStatus(request.id, event.target.value);
                      load();
                    }}
                    className="rounded-xl border border-sand-200 px-3 py-2 text-sm"
                  >
                    {["OPEN", "ACKNOWLEDGED", "RESOLVED"].map((status) => (
                      <option key={status} value={status}>{status}</option>
                    ))}
                  </select>
                </div>
                {request.note && <p className="mt-3 text-sm text-sand-700">{request.note}</p>}
              </article>
            ))}
            {serviceRequests.length === 0 && <p className="text-sm text-sand-500">No service requests yet.</p>}
          </div>
        </section>

        <section className={activeWorkspace === "tables" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Table Sessions</h2>
          <div className="mt-4 space-y-3">
            {tableSessions.map((session) => (
              <article key={session.qrToken} className="rounded-3xl bg-white p-4">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="font-semibold text-sand-900">Table {session.tableNumber}</p>
                    <p className="text-sm text-sand-600">
                      {session.activeOrderCount} active order(s) - Rs. {session.activeOrderTotal}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs uppercase tracking-[0.25em] text-sand-500">Latest</p>
                    <p className="text-sm font-semibold text-sand-900">{session.latestOrderStatus || "IDLE"}</p>
                  </div>
                </div>
                {session.openRequests.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-2">
                    {session.openRequests.map((request) => (
                      <span key={request.id} className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-800">
                        {request.type.replace("_", " ")}
                      </span>
                    ))}
                  </div>
                )}
              </article>
            ))}
          </div>
          <div className="mt-5 rounded-3xl bg-white p-4">
            <div className="flex flex-col gap-3 md:flex-row">
              <select
                value={selectedBillToken}
                onChange={(event) => loadBillPreview(event.target.value)}
                className="w-full rounded-2xl border border-sand-200 px-4 py-3"
              >
                <option value="">Select table for bill preview</option>
                {tableSessions.map((session) => (
                  <option key={session.qrToken} value={session.qrToken}>
                    Table {session.tableNumber}
                  </option>
                ))}
              </select>
            </div>
            {billPreview && (
              <div className="mt-4 space-y-2 text-sm text-sand-700">
                <div className="flex justify-between">
                  <span>Subtotal</span>
                  <span>Rs. {Number(billPreview.subtotalAmount).toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Discount</span>
                  <span>Rs. {Number(billPreview.discountAmount).toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Total</span>
                  <span>Rs. {Number(billPreview.payableAmount).toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-emerald-700">
                  <span>Paid</span>
                  <span>Rs. {Number(billPreview.paidAmount).toFixed(2)}</span>
                </div>
                <div className="flex justify-between font-semibold text-red-700">
                  <span>Pending</span>
                  <span>Rs. {Number(billPreview.pendingAmount).toFixed(2)}</span>
                </div>
              </div>
            )}
          </div>
        </section>

        <section className={activeWorkspace === "menu" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Category Manager</h2>
          <form className="mt-4 grid gap-3 md:grid-cols-[1fr_auto]" onSubmit={handleCategorySubmit}>
            <input
              value={categoryName}
              onChange={(event) => setCategoryName(event.target.value)}
              placeholder="Category name"
              className="w-full rounded-2xl border border-sand-200 px-4 py-3"
            />
            <button type="submit" className="rounded-2xl bg-paprika-500 px-5 py-3 font-semibold text-white">
              Create Category
            </button>
          </form>
          <div className="mt-4 flex flex-wrap gap-2">
            {categories.map((category) => (
              <span key={category.id} className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-sand-700">
                {category.name}
              </span>
            ))}
          </div>
        </section>

        <section className={activeWorkspace === "coupons" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Coupons</h2>
          <form className="mt-4 space-y-3" onSubmit={handleCouponSubmit}>
            <input
              value={couponForm.code}
              onChange={(event) => setCouponForm((current) => ({ ...current, code: event.target.value.toUpperCase() }))}
              placeholder="Coupon code"
              className="w-full rounded-2xl border border-sand-200 px-4 py-3"
            />
            <input
              value={couponForm.description}
              onChange={(event) => setCouponForm((current) => ({ ...current, description: event.target.value }))}
              placeholder="Description"
              className="w-full rounded-2xl border border-sand-200 px-4 py-3"
            />
            <div className="grid gap-3 sm:grid-cols-2">
              <input
                type="number"
                min="0"
                value={couponForm.discountValue}
                onChange={(event) => setCouponForm((current) => ({ ...current, discountValue: event.target.value }))}
                placeholder="Discount"
                className="rounded-2xl border border-sand-200 px-4 py-3"
              />
              <label className="rounded-2xl bg-white px-4 py-3 text-sm">
                <input
                  type="checkbox"
                  checked={couponForm.percentage}
                  onChange={() => setCouponForm((current) => ({ ...current, percentage: !current.percentage }))}
                /> Percentage discount
              </label>
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <input
                type="number"
                min="0"
                value={couponForm.minimumOrderAmount}
                onChange={(event) => setCouponForm((current) => ({ ...current, minimumOrderAmount: event.target.value }))}
                placeholder="Minimum order"
                className="rounded-2xl border border-sand-200 px-4 py-3"
              />
              <input
                type="number"
                min="0"
                value={couponForm.maxDiscountAmount}
                onChange={(event) => setCouponForm((current) => ({ ...current, maxDiscountAmount: event.target.value }))}
                placeholder="Max discount"
                className="rounded-2xl border border-sand-200 px-4 py-3"
              />
            </div>
            <button type="submit" className="w-full rounded-2xl bg-paprika-500 px-4 py-3 font-semibold text-white">
              Create Coupon
            </button>
          </form>
          <div className="mt-4 space-y-3">
            {coupons.map((coupon) => (
              <div key={coupon.id} className="rounded-2xl bg-white p-4 text-sm text-sand-700">
                <p className="font-semibold text-sand-900">{coupon.code}</p>
                <p>{coupon.description}</p>
                <p className="mt-1">
                  {coupon.percentage ? `${coupon.discountValue}% off` : `Rs. ${coupon.discountValue} off`}
                </p>
              </div>
            ))}
          </div>
        </section>

        <section className={activeWorkspace === "growth" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Reviews & Recommendations</h2>
          <div className="mt-5 grid gap-5 lg:grid-cols-2">
            <div className="rounded-3xl bg-white p-4">
              <p className="font-semibold text-sand-900">Top Menu Signals</p>
              <div className="mt-4 space-y-3">
                {[...menuItems]
                  .sort((left, right) =>
                    Number(right.averageRating || 0) * 20 + Number(right.orderCount || 0) * 2
                    - (Number(left.averageRating || 0) * 20 + Number(left.orderCount || 0) * 2)
                  )
                  .slice(0, 6)
                  .map((item) => (
                    <div key={item.id} className="flex items-center justify-between rounded-2xl bg-sand-50 px-4 py-3 text-sm">
                      <span className="font-semibold text-sand-900">{item.name}</span>
                      <span className="text-sand-600">
                        {Number(item.averageRating || 0).toFixed(1)} rating - {item.orderCount || 0} ordered
                      </span>
                    </div>
                  ))}
              </div>
            </div>
            <div className="rounded-3xl bg-white p-4">
              <p className="font-semibold text-sand-900">Recent Reviews</p>
              <div className="mt-4 space-y-3">
                {reviews.map((review) => (
                  <article key={review.id} className="rounded-2xl bg-sand-50 p-4 text-sm text-sand-700">
                    <p className="font-semibold text-sand-900">
                      {review.itemName} - {review.rating} rating
                    </p>
                    {review.comment && <p className="mt-1">{review.comment}</p>}
                    <p className="mt-2 text-xs text-sand-500">Table QR: {review.qrToken}</p>
                  </article>
                ))}
                {reviews.length === 0 && <p className="text-sm text-sand-500">No customer reviews yet.</p>}
              </div>
            </div>
          </div>
        </section>

        <section className={activeWorkspace === "menu" ? "glass-card p-6" : "hidden"}>
          <h2 className="font-display text-2xl text-sand-900">Add Menu Item</h2>
          <form className="mt-4 space-y-3" onSubmit={handleItemSubmit}>
            <input value={itemForm.name} onChange={(e) => setItemForm({ ...itemForm, name: e.target.value })} placeholder="Name" className="w-full rounded-2xl border border-sand-200 px-4 py-3" />
            <textarea value={itemForm.description} onChange={(e) => setItemForm({ ...itemForm, description: e.target.value })} placeholder="Description" rows={3} className="w-full rounded-2xl border border-sand-200 px-4 py-3" />
            <input value={itemForm.price} onChange={(e) => setItemForm({ ...itemForm, price: e.target.value })} placeholder="Price" className="w-full rounded-2xl border border-sand-200 px-4 py-3" />
            <input
              type="number"
              min="0"
              value={itemForm.stockQuantity}
              onChange={(e) => setItemForm({ ...itemForm, stockQuantity: e.target.value })}
              placeholder="Stock quantity"
              className="w-full rounded-2xl border border-sand-200 px-4 py-3"
            />
            <input
              type="number"
              min="1"
              value={itemForm.estimatedPreparationTime}
              onChange={(e) => setItemForm({ ...itemForm, estimatedPreparationTime: e.target.value })}
              placeholder="Estimated prep time (min)"
              className="w-full rounded-2xl border border-sand-200 px-4 py-3"
            />
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
            <div className="rounded-3xl bg-white p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-semibold text-sand-900">Variants</p>
                  <p className="text-sm text-sand-600">Sizes or portions with separate price and stock.</p>
                </div>
                <button
                  type="button"
                  onClick={addVariantRow}
                  className="rounded-full bg-sand-900 px-4 py-2 text-sm font-semibold text-white"
                >
                  Add Variant
                </button>
              </div>
              <div className="mt-4 space-y-3">
                {itemForm.variants.map((variant, index) => (
                  <div key={`variant-${index}`} className="grid gap-3 rounded-2xl bg-sand-50 p-3 md:grid-cols-5">
                    <input
                      value={variant.name}
                      onChange={(e) => updateVariantRow(index, "name", e.target.value)}
                      placeholder="Variant name"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <input
                      type="number"
                      min="0"
                      value={variant.priceAdjustment}
                      onChange={(e) => updateVariantRow(index, "priceAdjustment", e.target.value)}
                      placeholder="Price +"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <input
                      type="number"
                      min="0"
                      value={variant.stockQuantity}
                      onChange={(e) => updateVariantRow(index, "stockQuantity", e.target.value)}
                      placeholder="Stock"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <input
                      type="number"
                      min="1"
                      value={variant.estimatedPreparationTime}
                      onChange={(e) => updateVariantRow(index, "estimatedPreparationTime", e.target.value)}
                      placeholder="ETA"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <div className="flex items-center justify-between gap-3 rounded-2xl bg-white px-3 py-2 text-sm">
                      <label className="flex items-center gap-2">
                        <input
                          type="checkbox"
                          checked={variant.available}
                          onChange={() => updateVariantRow(index, "available", !variant.available)}
                        />
                        Active
                      </label>
                      <button
                        type="button"
                        onClick={() => removeVariantRow(index)}
                        className="font-semibold text-red-600"
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                ))}
                {itemForm.variants.length === 0 && (
                  <p className="text-sm text-sand-500">No variants yet. Add one for half/full, regular/large, and more.</p>
                )}
              </div>
            </div>
            <div className="rounded-3xl bg-white p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-semibold text-sand-900">Add-ons</p>
                  <p className="text-sm text-sand-600">Extras with separate stock and pricing.</p>
                </div>
                <button
                  type="button"
                  onClick={addAddonRow}
                  className="rounded-full bg-paprika-500 px-4 py-2 text-sm font-semibold text-white"
                >
                  Add Add-on
                </button>
              </div>
              <div className="mt-4 space-y-3">
                {itemForm.addons.map((addon, index) => (
                  <div key={`addon-${index}`} className="grid gap-3 rounded-2xl bg-sand-50 p-3 md:grid-cols-5">
                    <input
                      value={addon.name}
                      onChange={(e) => updateAddonRow(index, "name", e.target.value)}
                      placeholder="Add-on name"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <input
                      type="number"
                      min="0"
                      value={addon.price}
                      onChange={(e) => updateAddonRow(index, "price", e.target.value)}
                      placeholder="Price"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <input
                      type="number"
                      min="0"
                      value={addon.stockQuantity}
                      onChange={(e) => updateAddonRow(index, "stockQuantity", e.target.value)}
                      placeholder="Stock"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <input
                      type="number"
                      min="1"
                      value={addon.estimatedPreparationTime}
                      onChange={(e) => updateAddonRow(index, "estimatedPreparationTime", e.target.value)}
                      placeholder="ETA"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <div className="flex items-center justify-between gap-3 rounded-2xl bg-white px-3 py-2 text-sm">
                      <label className="flex items-center gap-2">
                        <input
                          type="checkbox"
                          checked={addon.available}
                          onChange={() => updateAddonRow(index, "available", !addon.available)}
                        />
                        Active
                      </label>
                      <button
                        type="button"
                        onClick={() => removeAddonRow(index)}
                        className="font-semibold text-red-600"
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                ))}
                {itemForm.addons.length === 0 && (
                  <p className="text-sm text-sand-500">No add-ons yet. Add one for cheese, naan, sauces, or extras.</p>
                )}
              </div>
            </div>
            <div className="rounded-3xl bg-white p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-semibold text-sand-900">Translations</p>
                  <p className="text-sm text-sand-600">Optional menu copy for Hindi, Spanish, or another language code.</p>
                </div>
                <button
                  type="button"
                  onClick={addTranslationRow}
                  className="rounded-full bg-sand-900 px-4 py-2 text-sm font-semibold text-white"
                >
                  Add Translation
                </button>
              </div>
              <div className="mt-4 space-y-3">
                {itemForm.translations.map((translation, index) => (
                  <div key={`translation-${index}`} className="grid gap-3 rounded-2xl bg-sand-50 p-3 md:grid-cols-[100px_1fr_1fr_auto]">
                    <input
                      value={translation.languageCode}
                      onChange={(event) => updateTranslationRow(index, "languageCode", event.target.value)}
                      placeholder="hi"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <input
                      value={translation.name}
                      onChange={(event) => updateTranslationRow(index, "name", event.target.value)}
                      placeholder="Translated name"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <input
                      value={translation.description}
                      onChange={(event) => updateTranslationRow(index, "description", event.target.value)}
                      placeholder="Translated description"
                      className="rounded-2xl border border-sand-200 px-3 py-2"
                    />
                    <button
                      type="button"
                      onClick={() => removeTranslationRow(index)}
                      className="rounded-2xl bg-white px-3 py-2 text-sm font-semibold text-red-600"
                    >
                      Remove
                    </button>
                  </div>
                ))}
                {itemForm.translations.length === 0 && (
                  <p className="text-sm text-sand-500">No translations yet. English remains the fallback.</p>
                )}
              </div>
            </div>
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

        <section className={activeWorkspace === "tables" ? "glass-card p-6" : "hidden"}>
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
          <div className="mt-4 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
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
