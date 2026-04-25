import { createContext, useContext, useEffect, useMemo, useState } from "react";

const CartContext = createContext(null);

const normalizeCartItem = (item) => ({
  ...item,
  cartKey: item.cartKey ?? String(item.id),
  addonIds: item.addonIds ?? [],
  addonNames: item.addonNames ?? [],
  availableQuantity: item.availableQuantity ?? item.stockQuantity ?? 0
});

export function CartProvider({ children }) {
  const [items, setItems] = useState(() => {
    const saved = localStorage.getItem("restaurantCart");
    return saved ? JSON.parse(saved).map(normalizeCartItem) : [];
  });

  useEffect(() => {
    localStorage.setItem("restaurantCart", JSON.stringify(items));
  }, [items]);

  const addItem = (item) => {
    const normalized = normalizeCartItem(item);
    setItems((current) => {
      const existing = current.find((entry) => entry.cartKey === normalized.cartKey);
      if (existing) {
        if (existing.quantity >= (existing.availableQuantity ?? 0)) {
          return current;
        }
        return current.map((entry) =>
          entry.cartKey === normalized.cartKey ? { ...entry, quantity: entry.quantity + 1 } : entry
        );
      }
      if ((normalized.availableQuantity ?? 0) <= 0) {
        return current;
      }
      return [...current, { ...normalized, quantity: 1 }];
    });
  };

  const updateQuantity = (cartKey, quantity) => {
    setItems((current) =>
      current
        .map((entry) => {
          if (entry.cartKey !== cartKey) {
            return entry;
          }
          const max = entry.availableQuantity ?? quantity;
          return { ...entry, quantity: Math.min(quantity, max) };
        })
        .filter((entry) => entry.quantity > 0)
    );
  };

  const clearCart = () => setItems([]);

  const value = useMemo(
    () => ({
      items,
      addItem,
      updateQuantity,
      clearCart,
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
      total: items.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0)
    }),
    [items]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export const useCart = () => useContext(CartContext);
