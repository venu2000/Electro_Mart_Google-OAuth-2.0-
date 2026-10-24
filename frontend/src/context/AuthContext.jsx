import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { getCurrentUser, logout as logoutRequest } from '../services/authService';
import { isUnauthorized } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Asks the backend who is logged in. The JWT is in an HttpOnly cookie, so JavaScript never reads it.
  const refreshUser = useCallback(async () => {
    try {
      const me = await getCurrentUser();
      setUser(me);
      return me;
    } catch (error) {
      setUser(null);
      if (!isUnauthorized(error)) {
        console.error('Failed to load current user');
      }
      return null;
    }
  }, []);

  useEffect(() => {
    refreshUser().finally(() => setLoading(false));
  }, [refreshUser]);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } finally {
      // Clearing local state is what the UI can do; the cookie is cleared by the server response.
      setUser(null);
    }
  }, []);

  const value = useMemo(
    () => ({ user, loading, isAuthenticated: Boolean(user), refreshUser, logout }),
    [user, loading, refreshUser, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
