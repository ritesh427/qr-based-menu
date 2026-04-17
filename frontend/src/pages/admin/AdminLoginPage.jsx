import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { adminLogin } from "../../api/menuApi";
import { useAuth } from "../../context/AuthContext";

export default function AdminLoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ username: "admin", password: "admin123" });
  const [error, setError] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      const data = await adminLogin(form);
      login(data);
      navigate("/admin");
    } catch {
      setError("Login failed. Check credentials.");
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center px-4">
      <form onSubmit={handleSubmit} className="glass-card w-full max-w-md space-y-5 p-8">
        <div>
          <p className="text-xs uppercase tracking-[0.4em] text-paprika-500">Secure Access</p>
          <h1 className="mt-2 font-display text-3xl text-sand-900">Admin Login</h1>
        </div>
        <input
          value={form.username}
          onChange={(event) => setForm({ ...form, username: event.target.value })}
          className="w-full rounded-2xl border border-sand-200 px-4 py-3"
          placeholder="Username"
        />
        <input
          type="password"
          value={form.password}
          onChange={(event) => setForm({ ...form, password: event.target.value })}
          className="w-full rounded-2xl border border-sand-200 px-4 py-3"
          placeholder="Password"
        />
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button type="submit" className="w-full rounded-2xl bg-sand-900 px-4 py-3 font-semibold text-white">
          Sign In
        </button>
      </form>
    </main>
  );
}
