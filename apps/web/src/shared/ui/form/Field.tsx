import type { InputHTMLAttributes, ReactNode } from 'react';

type FieldProps = InputHTMLAttributes<HTMLInputElement> & {
  description?: string;
  error?: string;
  label: string;
  trailing?: ReactNode;
};

export function Field({
  description,
  error,
  id,
  label,
  trailing,
  ...props
}: FieldProps) {
  const inputId = id ?? props.name ?? label.toLowerCase().replace(/\s+/g, '-');
  const descriptionId = description ? `${inputId}-description` : undefined;
  const errorId = error ? `${inputId}-error` : undefined;

  return (
    <label className="field" htmlFor={inputId}>
      <span className="field__label">{label}</span>
      <span className="field__control">
        <input
          aria-describedby={[descriptionId, errorId].filter(Boolean).join(' ') || undefined}
          aria-invalid={error ? true : undefined}
          id={inputId}
          {...props}
        />
        {trailing ? <span className="field__trailing">{trailing}</span> : null}
      </span>
      {description ? (
        <span className="field__description" id={descriptionId}>
          {description}
        </span>
      ) : null}
      {error ? (
        <span className="field__error" id={errorId}>
          {error}
        </span>
      ) : null}
    </label>
  );
}
