import { Navigate, Route, Routes } from "react-router-dom";
import PublicMenuPage from "./pages/public/PublicMenuPage";
import OrderStatusPage from "./pages/public/OrderStatusPage";
import CartPage from "./pages/public/CartPage";
import AdminLoginPage from "./pages/admin/AdminLoginPage";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import AdminLayout from "./layouts/AdminLayout";
import ProtectedRoute from "./components/ProtectedRoute";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/menu/saffron-table-t1" replace />} />
      <Route path="/menu/:qrToken" element={<PublicMenuPage />} />
      <Route path="/cart/:qrToken" element={<CartPage />} />
      <Route path="/orders/:qrToken" element={<OrderStatusPage />} />
      <Route path="/admin/login" element={<AdminLoginPage />} />
      <Route
        path="/admin"
        element={
          <ProtectedRoute>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<AdminDashboardPage />} />
      </Route>
    </Routes>
  );
}
