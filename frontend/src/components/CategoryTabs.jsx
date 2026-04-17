export default function CategoryTabs({ categories, active, onChange }) {
  return (
    <div className="no-scrollbar flex gap-3 overflow-x-auto pb-2">
      {categories.map((category) => (
        <button
          key={category.id}
          type="button"
          onClick={() => onChange(category.id)}
          className={`rounded-full px-4 py-2 text-sm font-semibold transition ${
            active === category.id
              ? "bg-paprika-500 text-white shadow-glow"
              : "bg-white/80 text-sand-900"
          }`}
        >
          {category.name}
        </button>
      ))}
    </div>
  );
}
