interface SkeletonProps {
  className?: string;
}

export function Skeleton({ className = '' }: SkeletonProps) {
  return (
    <div className={`animate-pulse bg-surface-2 rounded ${className}`} />
  );
}

interface TableSkeletonProps {
  rows?: number;
  cols?: number;
}

export function TableSkeleton({ rows = 6, cols = 5 }: TableSkeletonProps) {
  return (
    <table className="w-full text-sm">
      <thead className="bg-surface">
        <tr>
          {Array.from({ length: cols }).map((_, ci) => (
            <th key={ci} className="px-5 py-3">
              <Skeleton className={`h-3 ${ci === 0 ? 'max-w-[60px]' : 'w-full'}`} />
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {Array.from({ length: rows }).map((_, ri) => (
          <tr key={ri} className="border-t border-border-hairline">
            {Array.from({ length: cols }).map((_, ci) => (
              <td key={ci} className="px-5 py-3">
                <Skeleton className={`h-3 ${ci === 0 ? 'max-w-[60px]' : 'w-full'}`} />
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
}

interface CardSkeletonProps {
  count?: number;
}

export function CardSkeleton({ count = 4 }: CardSkeletonProps) {
  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-5">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="panel">
          <div className="panel-b">
            <Skeleton className="w-20 h-3 mb-2" />
            <Skeleton className="w-24 h-6" />
          </div>
        </div>
      ))}
    </div>
  );
}
