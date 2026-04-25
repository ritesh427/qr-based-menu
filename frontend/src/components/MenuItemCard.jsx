import { useMemo, useState } from "react";

const buildCartKey = (itemId, variantId, addonIds) =>
  [itemId, variantId ?? "base", [...addonIds].sort((a, b) => a - b).join("-")].join(":");

export default function MenuItemCard({ item, onAdd, cartItems = [] }) {
  const [selectedVariantId, setSelectedVariantId] = useState(item.variants?.[0]?.id ?? null);
  const [selectedAddonIds, setSelectedAddonIds] = useState([]);

  const selectedVariant = useMemo(
    () => item.variants?.find((variant) => variant.id === selectedVariantId) ?? null,
    [item.variants, selectedVariantId]
  );

  const selectedAddons = useMemo(
    () => (item.addons ?? []).filter((addon) => selectedAddonIds.includes(addon.id)),
    [item.addons, selectedAddonIds]
  );

  const availableQuantity = useMemo(() => {
    const stockLevels = [item.stockQuantity ?? 0];
    if (selectedVariant) {
      stockLevels.push(selectedVariant.stockQuantity ?? 0);
    }
    selectedAddons.forEach((addon) => stockLevels.push(addon.stockQuantity ?? 0));
    return Math.min(...stockLevels);
  }, [item.stockQuantity, selectedVariant, selectedAddons]);

  const unitPrice = useMemo(() => {
    const variantDelta = selectedVariant?.priceAdjustment ?? 0;
    const addonTotal = selectedAddons.reduce((sum, addon) => sum + Number(addon.price), 0);
    return Number(item.price) + Number(variantDelta) + addonTotal;
  }, [item.price, selectedVariant, selectedAddons]);

  const eta = useMemo(() => {
    const baseEta = item.estimatedPreparationTime || 10;
    const variantEta = selectedVariant?.estimatedPreparationTime || 0;
    const addonEta = selectedAddons.reduce(
      (max, addon) => Math.max(max, addon.estimatedPreparationTime || 0),
      0
    );
    return Math.max(baseEta, variantEta, addonEta);
  }, [item.estimatedPreparationTime, selectedVariant, selectedAddons]);

  const cartKey = useMemo(
    () => buildCartKey(item.id, selectedVariant?.id ?? null, selectedAddons.map((addon) => addon.id)),
    [item.id, selectedVariant, selectedAddons]
  );

  const quantity = cartItems.find((entry) => entry.cartKey === cartKey)?.quantity || 0;
  const maxReached = quantity >= availableQuantity;
  const soldOut = !item.available || availableQuantity <= 0;

  const handleAddonToggle = (addonId) => {
    setSelectedAddonIds((current) =>
      current.includes(addonId) ? current.filter((id) => id !== addonId) : [...current, addonId]
    );
  };

  const handleAdd = () => {
    onAdd({
      id: item.id,
      cartKey,
      name: item.name,
      imageUrl: item.imageUrl,
      basePrice: item.price,
      price: unitPrice,
      stockQuantity: item.stockQuantity,
      availableQuantity,
      variantId: selectedVariant?.id ?? null,
      variantName: selectedVariant?.name ?? null,
      addonIds: selectedAddons.map((addon) => addon.id),
      addonNames: selectedAddons.map((addon) => addon.name),
      estimatedPreparationTime: eta
    });
  };

  return (
    <article className="glass-card overflow-hidden">
      <img
        src={item.imageUrl}
        alt={item.name}
        className="h-40 w-full object-cover"
      />
      <div className="space-y-3 p-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h3 className="font-display text-xl text-sand-900">{item.name}</h3>
            <p className="mt-1 text-sm text-sand-700">{item.description}</p>
          </div>
          <span className="rounded-full bg-sand-100 px-3 py-1 text-xs font-semibold text-sand-700">
            {item.vegetarian ? "Veg" : "Chef"}
          </span>
        </div>

        {item.variants?.length > 0 && (
          <label className="block text-sm text-sand-700">
            <span className="mb-1 block font-semibold text-sand-900">Choose Size</span>
            <select
              value={selectedVariantId ?? ""}
              onChange={(event) => setSelectedVariantId(event.target.value ? Number(event.target.value) : null)}
              className="w-full rounded-2xl border border-sand-200 bg-white px-3 py-2"
            >
              {item.variants.map((variant) => (
                <option key={variant.id} value={variant.id} disabled={!variant.available}>
                  {variant.name} {!variant.available ? "(Sold Out)" : variant.priceAdjustment ? `(+Rs. ${variant.priceAdjustment})` : ""}
                </option>
              ))}
            </select>
          </label>
        )}

        {item.addons?.length > 0 && (
          <div>
            <p className="mb-2 text-sm font-semibold text-sand-900">Add-ons</p>
            <div className="space-y-2">
              {item.addons.map((addon) => (
                <label key={addon.id} className="flex items-center justify-between rounded-2xl bg-white px-3 py-2 text-sm text-sand-700">
                  <span className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={selectedAddonIds.includes(addon.id)}
                      disabled={!addon.available}
                      onChange={() => handleAddonToggle(addon.id)}
                    />
                    {addon.name}
                  </span>
                  <span>{addon.available ? `+Rs. ${addon.price}` : "Sold Out"}</span>
                </label>
              ))}
            </div>
          </div>
        )}

        <div className="flex items-center justify-between">
          <div className="text-sm text-sand-700">
            <p className="text-lg font-bold text-paprika-700">Rs. {unitPrice.toFixed(2)}</p>
            <p>{eta} min prep</p>
            <p>{availableQuantity} available for this selection</p>
            {quantity > 0 && (
              <p className="mt-1 font-semibold text-emerald-700">{quantity} in cart</p>
            )}
          </div>
          <button
            type="button"
            disabled={soldOut || maxReached}
            onClick={handleAdd}
            className="rounded-full bg-sand-900 px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-sand-300"
          >
            {soldOut ? "Sold Out" : maxReached ? "Max Added" : quantity > 0 ? "Add More" : "Add"}
          </button>
        </div>
      </div>
    </article>
  );
}
