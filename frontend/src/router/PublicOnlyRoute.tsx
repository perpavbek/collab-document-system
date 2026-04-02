import type { PropsWithChildren } from 'react';
import { Navigate } from 'react-router-dom';

import { useAuthStore } from '../stores/authStore';

export const PublicOnlyRoute = ({ children }: PropsWithChildren) => {
  const token = useAuthStore((state) => state.token);

  if (token) {
    return <Navigate to="/documents" replace />;
  }

  return children;
};
