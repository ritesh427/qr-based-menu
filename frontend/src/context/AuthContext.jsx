import { createContext, useContext, useMemo, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("adminToken"));
  const [restaurantId, setRestaurantId] = useState(localStorage.getItem("restaurantId"));

  const login = (payload) => {
    localStorage.setItem("adminToken", payload.token);
    localStorage.setItem("restaurantId", payload.restaurantId);
    setToken(payload.token);
    setRestaurantId(String(payload.restaurantId));
  };

  const logout = () => {
    localStorage.removeItem("adminToken");
    localStorage.removeItem("restaurantId");
    setToken(null);
    setRestaurantId(null);
  };

  const value = useMemo(
    () => ({
      token,
      restaurantId,
      isAuthenticated: Boolean(token),
      login,
      logout
    }),
    [token, restaurantId]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
