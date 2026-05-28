import type { ReactNode } from 'react';
import { X } from 'lucide-react';
import { Button } from '../button';

type DialogProps = {
  children: ReactNode;
  description?: string;
  onClose: () => void;
  open: boolean;
  title: string;
};

export function Dialog({ children, description, onClose, open, title }: DialogProps) {
  if (!open) {
    return null;
  }

  return (
    <div className="dialog-backdrop" role="presentation">
      <section
        aria-describedby={description ? 'dialog-description' : undefined}
        aria-labelledby="dialog-title"
        aria-modal="true"
        className="dialog"
        role="dialog"
      >
        <header className="dialog__header">
          <div>
            <h2 id="dialog-title">{title}</h2>
            {description ? <p id="dialog-description">{description}</p> : null}
          </div>
          <Button
            aria-label="Close dialog"
            icon={<X aria-hidden="true" size={18} />}
            onClick={onClose}
            variant="ghost"
          >
            Close
          </Button>
        </header>
        <div className="dialog__body">{children}</div>
      </section>
    </div>
  );
}
