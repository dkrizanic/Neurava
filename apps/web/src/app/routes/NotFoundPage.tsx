import { Compass } from 'lucide-react';
import { Link } from 'react-router';
import { EmptyState } from '../../shared/ui';

export function NotFoundPage() {
  return (
    <EmptyState
      action={
        <Link className="ui-link-button ui-link-button--primary" to="/">
          Open overview
        </Link>
      }
      description="The address does not match an app section yet."
      icon={<Compass aria-hidden="true" size={24} />}
      title="Route not found"
    />
  );
}
