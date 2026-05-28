import { AlertTriangle, ArrowLeft } from 'lucide-react';
import { isRouteErrorResponse, Link, useRouteError } from 'react-router';
import { EmptyState } from '../../shared/ui';

export function RouteError() {
  const error = useRouteError();
  const title = isRouteErrorResponse(error)
    ? `${error.status} ${error.statusText}`
    : 'This view could not load';

  return (
    <EmptyState
      action={
        <Link className="ui-link-button ui-link-button--primary" to="/">
          <ArrowLeft aria-hidden="true" size={18} />
          <span>Return home</span>
        </Link>
      }
      description="The route failed safely. Try returning to the overview and opening the section again."
      icon={<AlertTriangle aria-hidden="true" size={24} />}
      title={title}
    />
  );
}
