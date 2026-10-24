import { Link } from 'react-router-dom';

export default function AccessDeniedPage() {
  return (
    <div className="center-page">
      <h1>Access denied</h1>
      <p className="muted">You do not have permission to view this page.</p>
      <Link className="button" to="/products">Go to products</Link>
    </div>
  );
}
