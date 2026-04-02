import { lazy, Suspense, useEffect } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';

import { AppShell } from './layouts/AppShell';
import { ProtectedRoute } from './router/ProtectedRoute';
import { PublicOnlyRoute } from './router/PublicOnlyRoute';
import { PageLoader } from './components/PageLoader';
import { useAuthStore } from './stores/authStore';

const LoginPage = lazy(() => import('./pages/LoginPage').then((module) => ({ default: module.LoginPage })));
const RegisterPage = lazy(() => import('./pages/RegisterPage').then((module) => ({ default: module.RegisterPage })));
const DocumentsPage = lazy(() => import('./pages/DocumentsPage').then((module) => ({ default: module.DocumentsPage })));
const DocumentInvitationPage = lazy(() =>
  import('./pages/DocumentInvitationPage').then((module) => ({ default: module.DocumentInvitationPage })),
);
const DocumentEditorPage = lazy(() =>
  import('./pages/DocumentEditorPage').then((module) => ({ default: module.DocumentEditorPage })),
);
const NotFoundPage = lazy(() => import('./pages/NotFoundPage').then((module) => ({ default: module.NotFoundPage })));

export default function App() {
  const bootstrap = useAuthStore((state) => state.bootstrap);
  const isBootstrapping = useAuthStore((state) => state.isBootstrapping);

  useEffect(() => {
    void bootstrap();
  }, [bootstrap]);

  if (isBootstrapping) {
    return <PageLoader label="Restoring your workspace" fullScreen />;
  }

  return (
    <Suspense fallback={<PageLoader label="Loading page" fullScreen />}>
      <Routes>
        <Route
          path="/login"
          element={
            <PublicOnlyRoute>
              <LoginPage />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="/register"
          element={
            <PublicOnlyRoute>
              <RegisterPage />
            </PublicOnlyRoute>
          }
        />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <AppShell />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/documents" replace />} />
          <Route path="documents" element={<DocumentsPage />} />
          <Route path="invitations/:token" element={<DocumentInvitationPage />} />
          <Route path="documents/:id" element={<DocumentEditorPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}
