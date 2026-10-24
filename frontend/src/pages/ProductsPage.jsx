import { useEffect, useState } from 'react';
import { fetchProducts } from '../services/productService';
import { getErrorMessage, isUnauthorized } from '../services/api';
import { useAuth } from '../context/AuthContext';
import ProductCard from '../components/ProductCard';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import { useNavigate } from 'react-router-dom';

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { refreshUser } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;
    fetchProducts()
      .then((data) => active && setProducts(data))
      .catch((err) => {
        if (!active) return;
        if (isUnauthorized(err)) {
          // Session expired: clear user state and send the user to login.
          refreshUser().finally(() => navigate('/login', { replace: true }));
          return;
        }
        setError(getErrorMessage(err));
      })
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [navigate, refreshUser]);

  if (loading) return <LoadingSpinner label="Loading products..." />;

  return (
    <section>
      <h1>Products</h1>
      <ErrorMessage message={error} />
      <div className="grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </section>
  );
}
