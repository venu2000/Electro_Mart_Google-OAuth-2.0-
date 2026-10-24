import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchProductById } from '../services/productService';
import { getErrorMessage } from '../services/api';
import { formatPrice, stockLabel } from '../utils/format';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';

const PLACEHOLDER = '/images/products/placeholder.svg';

export default function ProductDetailsPage() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    fetchProductById(id)
      .then((data) => active && setProduct(data))
      .catch((err) => active && setError(getErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [id]);

  if (loading) return <LoadingSpinner label="Loading product..." />;

  if (error) {
    return (
      <section>
        <ErrorMessage message={error} />
        <Link to="/products">Back to products</Link>
      </section>
    );
  }

  return (
    <section>
      <Link to="/products">&larr; Back to products</Link>
      <div className="details" style={{ marginTop: 16 }}>
        <img
          src={product.imageUrl || PLACEHOLDER}
          alt={product.name}
          onError={(e) => { e.currentTarget.src = PLACEHOLDER; }}
        />
        <div>
          <p className="muted">{product.brand} · {product.category}</p>
          <h1>{product.name}</h1>
          <p className="price">{formatPrice(product.price)}</p>
          <p>{product.description}</p>
          <p><span className={`badge ${product.stock === 0 ? 'out' : ''}`}>{stockLabel(product.stock)}</span></p>
        </div>
      </div>
    </section>
  );
}
