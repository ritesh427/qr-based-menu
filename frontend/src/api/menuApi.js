import api from "./client";

export const fetchMenu = async (qrToken) => {
  const { data } = await api.get(`/api/public/menu/${qrToken}`);
  return data;
};

export const placeOrder = async (payload) => {
  const { data } = await api.post("/api/public/orders", payload);
  return data;
};

export const fetchOrderStatus = async (qrToken) => {
  const { data } = await api.get(`/api/public/orders/${qrToken}`);
  return data;
};

export const adminLogin = async (payload) => {
  const { data } = await api.post("/api/admin/auth/login", payload);
  return data;
};

export const fetchAdminCategories = async () => (await api.get("/api/admin/categories")).data;
export const createCategory = async (payload) => (await api.post("/api/admin/categories", payload)).data;
export const updateCategory = async (id, payload) => (await api.put(`/api/admin/categories/${id}`, payload)).data;
export const deleteCategory = async (id) => (await api.delete(`/api/admin/categories/${id}`)).data;

export const fetchAdminMenuItems = async () => (await api.get("/api/admin/menu-items")).data;
export const createMenuItem = async (payload) => (await api.post("/api/admin/menu-items", payload)).data;
export const updateMenuItem = async (id, payload) => (await api.put(`/api/admin/menu-items/${id}`, payload)).data;
export const uploadMenuImage = async (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return (await api.post("/api/admin/menu-items/image-upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  })).data;
};
export const toggleMenuItemAvailability = async (id, available) =>
  (await api.patch(`/api/admin/menu-items/${id}/availability?available=${available}`)).data;
export const deleteMenuItem = async (id) => (await api.delete(`/api/admin/menu-items/${id}`)).data;
export const fetchQrCodes = async () => (await api.get("/api/admin/menu-items/qr-codes")).data;
export const createTables = async (payload) => (await api.post("/api/admin/tables", payload)).data;

export const fetchOrders = async () => (await api.get("/api/admin/orders")).data;
export const fetchOrderStats = async () => (await api.get("/api/admin/orders/stats")).data;
export const updateOrderStatus = async (id, status) =>
  (await api.patch(`/api/admin/orders/${id}/status`, { status })).data;
