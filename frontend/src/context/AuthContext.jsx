import { createContext, useContext, useMemo, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("adminToken"));
  const [restaurantId, setRestaurantId] = useState(localStorage.getItem("restaurantId"));
  const [role, setRole] = useState(localStorage.getItem("adminRole") || (localStorage.getItem("adminToken") ? "ADMIN" : null));
  const [username, setUsername] = useState(localStorage.getItem("adminUsername") || (localStorage.getItem("adminToken") ? "admin" : null));

  const login = (payload) => {
    localStorage.setItem("adminToken", payload.token);
    localStorage.setItem("restaurantId", payload.restaurantId);
    localStorage.setItem("adminRole", payload.role);
    localStorage.setItem("adminUsername", payload.username);
    setToken(payload.token);
    setRestaurantId(String(payload.restaurantId));
    setRole(payload.role);
    setUsername(payload.username);
  };

  const logout = () => {
    localStorage.removeItem("adminToken");
    localStorage.removeItem("restaurantId");
    localStorage.removeItem("adminRole");
    localStorage.removeItem("adminUsername");
    setToken(null);
    setRestaurantId(null);
    setRole(null);
    setUsername(null);
  };

  const value = useMemo(
    () => ({
      token,
      restaurantId,
      role,
      username,
      isAuthenticated: Boolean(token),
      login,
      logout
    }),
    [token, restaurantId, role, username]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
