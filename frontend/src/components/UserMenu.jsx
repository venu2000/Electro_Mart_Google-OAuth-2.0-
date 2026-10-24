import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function UserMenu() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="user-menu">
      <img
        src={user.pictureUrl || '/images/products/placeholder.svg'}
        alt={`${user.name} avatar`}
        onClick={() => setOpen((v) => !v)}
        referrerPolicy="no-referrer"
      />
      {open && (
        <div className="dropdown">
          <div className="muted" style={{ padding: '8px 16px' }}>{user.email}</div>
          <Link to="/profile" onClick={() => setOpen(false)}>Profile</Link>
          <button type="button" onClick={handleLogout}>Log out</button>
        </div>
      )}
    </div>
  );
}
