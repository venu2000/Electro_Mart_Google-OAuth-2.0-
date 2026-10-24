import { useAuth } from '../context/AuthContext';

export default function ProfilePage() {
  const { user } = useAuth();

  return (
    <section className="details">
      <img
        src={user.pictureUrl || '/images/products/placeholder.svg'}
        alt={`${user.name} avatar`}
        referrerPolicy="no-referrer"
      />
      <div>
        <h1>{user.name}</h1>
        <p><strong>Email:</strong> {user.email}</p>
        <p><strong>Role:</strong> {user.role}</p>
        <p className="muted">Profile data comes from your Google account and is refreshed at each sign-in.</p>
      </div>
    </section>
  );
}
