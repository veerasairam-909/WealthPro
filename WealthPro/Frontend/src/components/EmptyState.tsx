import type { ReactNode } from 'react';

interface Props {
  icon: ReactNode;
  title: string;
  description?: string;
  action?: ReactNode;
}

export default function EmptyState({ icon, title, description, action }: Props) {
  return (
    <div className="panel-b flex flex-col items-center justify-center py-14 text-center">
      <div className="w-14 h-14 rounded-full bg-surface-2 flex items-center justify-center text-text-3 mb-4">
        {icon}
      </div>
      <p className="font-semibold text-text mb-1">{title}</p>
      {description && (
        <p className="text-sm text-text-2 max-w-xs">{description}</p>
      )}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
