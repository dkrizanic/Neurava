type LoadingStateProps = {
  label?: string;
};

export function LoadingState({ label = 'Loading workspace' }: LoadingStateProps) {
  return (
    <div aria-live="polite" className="loading-state" role="status">
      <span className="loading-state__spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}
