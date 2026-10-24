import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="center-page">
      <h1>Page not found</h1>
      <p className="muted">The page you are looking for does not exist.</p>
      <Link className="button" to="/products">Go to products</Link>
    </div>
  );
}
