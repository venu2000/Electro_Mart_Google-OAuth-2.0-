import { Link } from 'react-router-dom';
import { formatPrice, stockLabel } from '../utils/format';

const PLACEHOLDER = '/images/products/placeholder.svg';

export default function ProductCard({ product }) {
  const outOfStock = product.stock === 0;
  return (
    <article className="card">
      <img
        src={product.imageUrl || PLACEHOLDER}
        alt={product.name}
        onError={(e) => { e.currentTarget.src = PLACEHOLDER; }}
      />
      <span className="muted">{product.brand} · {product.category}</span>
      <h3>{product.name}</h3>
      <span className="price">{formatPrice(product.price)}</span>
      <span className={`badge ${outOfStock ? 'out' : ''}`}>{stockLabel(product.stock)}</span>
      <Link className="button secondary" to={`/products/${product.id}`}>View details</Link>
    </article>
  );
}
