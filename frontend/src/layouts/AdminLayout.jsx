import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function AdminLayout() {
  const { logout } = useAuth();

  return (
    <div className="min-h-screen bg-sand-50 p-4 md:p-8">
      <div className="mx-auto max-w-7xl space-y-6">
        <header className="glass-card flex flex-col gap-4 p-6 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm uppercase tracking-[0.3em] text-paprika-500">Admin Console</p>
            <h1 className="font-display text-3xl text-sand-900">Restaurant Operations</h1>
          </div>
          <div className="flex items-center gap-3">
            <Link to="/" className="rounded-full border border-sand-200 px-4 py-2 text-sm font-medium">
              View Demo Menu
            </Link>
            <button
              type="button"
              onClick={logout}
              className="rounded-full bg-sand-900 px-4 py-2 text-sm font-medium text-white"
            >
              Logout
            </button>
          </div>
        </header>
        <Outlet />
      </div>
    </div>
  );
}
