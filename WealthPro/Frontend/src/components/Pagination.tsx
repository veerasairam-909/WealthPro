import { ChevronLeft, ChevronRight } from 'lucide-react';

interface Props {
  page: number;      // 0-indexed current page
  total: number;     // total number of items
  pageSize: number;
  onChange: (page: number) => void;
}

export default function Pagination({ page, total, pageSize, onChange }: Props) {
  const totalPages = Math.ceil(total / pageSize);
  if (totalPages <= 1) return null;

  const from = page * pageSize + 1;
  const to = Math.min((page + 1) * pageSize, total);

  // Build page number list with ellipsis — always show up to 7 buttons
  function getPageNumbers(): (number | '...')[] {
    if (totalPages <= 7) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }
    const pages: (number | '...')[] = [];
    if (page <= 3) {
      for (let i = 0; i < 5; i++) pages.push(i);
      pages.push('...');
      pages.push(totalPages - 1);
    } else if (page >= totalPages - 4) {
      pages.push(0);
      pages.push('...');
      for (let i = totalPages - 5; i < totalPages; i++) pages.push(i);
    } else {
      pages.push(0);
      pages.push('...');
      pages.push(page - 1);
      pages.push(page);
      pages.push(page + 1);
      pages.push('...');
      pages.push(totalPages - 1);
    }
    return pages;
  }

  const pageNumbers = getPageNumbers();

  return (
    <div className="flex items-center justify-between px-5 py-3 border-t border-border-hairline text-sm">
      <span className="text-xs text-text-2">
        Showing {from}–{to} of {total}
      </span>

      <div className="flex items-center gap-1">
        {/* Prev */}
        <button
          onClick={() => onChange(page - 1)}
          disabled={page === 0}
          className={`p-1.5 rounded border border-border hover:bg-surface disabled:opacity-30 disabled:cursor-not-allowed`}
          aria-label="Previous page"
        >
          <ChevronLeft size={14} />
        </button>

        {/* Page numbers */}
        {pageNumbers.map((p, idx) =>
          p === '...' ? (
            <span key={`ellipsis-${idx}`} className="px-2 text-text-3 select-none">…</span>
          ) : (
            <button
              key={p}
              onClick={() => onChange(p as number)}
              className={
                'w-7 h-7 rounded border text-xs font-medium ' +
                (p === page
                  ? 'bg-primary text-white border-primary'
                  : 'bg-white text-text border-border hover:bg-surface')
              }
            >
              {(p as number) + 1}
            </button>
          )
        )}

        {/* Next */}
        <button
          onClick={() => onChange(page + 1)}
          disabled={page === totalPages - 1}
          className="p-1.5 rounded border border-border hover:bg-surface disabled:opacity-30 disabled:cursor-not-allowed"
          aria-label="Next page"
        >
          <ChevronRight size={14} />
        </button>
      </div>
    </div>
  );
}
