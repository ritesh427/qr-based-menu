export default function MenuItemCard({ item, onAdd, quantity = 0 }) {
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
        <div className="flex items-center justify-between">
          <div className="text-sm text-sand-700">
            <p className="text-lg font-bold text-paprika-700">Rs. {item.price}</p>
            <p>{item.estimatedPreparationTime || 10} min prep</p>
            {quantity > 0 && (
              <p className="mt-1 font-semibold text-emerald-700">{quantity} in cart</p>
            )}
          </div>
          <button
            type="button"
            disabled={!item.available}
            onClick={() => onAdd(item)}
            className="rounded-full bg-sand-900 px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-sand-300"
          >
            {item.available ? (quantity > 0 ? "Add More" : "Add") : "Sold Out"}
          </button>
        </div>
      </div>
    </article>
  );
}
