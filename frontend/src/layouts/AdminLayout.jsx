import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function AdminLayout() {
  const { logout, role, username } = useAuth();

  return (
    <div className="min-h-screen bg-sand-50 p-4 md:p-8">
      <div className="mx-auto max-w-7xl space-y-6">
        <header className="glass-card flex flex-col gap-4 p-6 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm uppercase tracking-[0.3em] text-paprika-500">Admin Console</p>
            <h1 className="font-display text-3xl text-sand-900">Restaurant Operations</h1>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <div className="rounded-2xl bg-white px-4 py-2 text-sm text-sand-700">
              <span className="font-semibold text-sand-900">{username || "staff"}</span>
              <span className="ml-2 text-xs uppercase tracking-[0.2em] text-paprika-500">{role || "ADMIN"}</span>
            </div>
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
