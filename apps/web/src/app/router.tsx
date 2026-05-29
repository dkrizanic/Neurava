import { lazy, Suspense, type ReactNode } from 'react';
import { createBrowserRouter, Navigate } from 'react-router';
import { App } from './App';
import { LoadingPage } from './routes/LoadingPage';
import { RouteError } from './routes/RouteError';

const OverviewPage = lazy(() =>
  import('./routes/OverviewPage').then((module) => ({ default: module.OverviewPage })),
);
const SectionPage = lazy(() =>
  import('./routes/SectionPage').then((module) => ({ default: module.SectionPage })),
);
const NotesPage = lazy(() =>
  import('../features/notes/components/NotesPage').then((module) => ({ default: module.NotesPage })),
);
const NotFoundPage = lazy(() =>
  import('./routes/NotFoundPage').then((module) => ({ default: module.NotFoundPage })),
);

function withRouteLoading(element: ReactNode) {
  return <Suspense fallback={<LoadingPage />}>{element}</Suspense>;
}

export const router = createBrowserRouter([
  {
    element: <App />,
    errorElement: <RouteError />,
    hydrateFallbackElement: <LoadingPage />,
    path: '/',
    children: [
      {
        index: true,
        element: withRouteLoading(<OverviewPage />),
      },
      {
        element: withRouteLoading(<NotesPage />),
        path: 'notes',
      },
      {
        element: withRouteLoading(<SectionPage />),
        path: ':sectionId',
      },
      {
        element: <Navigate replace to="/assistant" />,
        path: 'assistant-search',
      },
      {
        element: <Navigate replace to="/plans" />,
        path: 'preparation',
      },
      {
        element: withRouteLoading(<NotFoundPage />),
        path: '*',
      },
    ],
  },
]);
