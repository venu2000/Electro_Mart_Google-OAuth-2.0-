import { Link } from 'react-router-dom';
import UserMenu from './UserMenu';

export default function Navbar() {
  return (
    <header className="navbar">
      <Link to="/products" className="brand">ElectroMart</Link>
      <nav>
        <Link to="/products">Products</Link>
      </nav>
      <UserMenu />
    </header>
  );
}
