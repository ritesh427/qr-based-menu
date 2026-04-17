import axios from "axios";
import { getApiBaseUrl } from "../utils/runtime";

const api = axios.create({
  baseURL: getApiBaseUrl()
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("adminToken");
  const restaurantId = localStorage.getItem("restaurantId");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (restaurantId) {
    config.headers["X-Restaurant-Id"] = restaurantId;
  }
  return config;
});

export default api;
