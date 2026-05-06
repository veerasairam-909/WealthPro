import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Skeleton, TableSkeleton, CardSkeleton } from '@/components/Skeleton';

describe('Skeleton', () => {
  it('renders with animate-pulse class', () => {
    const { container } = render(<Skeleton />);
    expect(container.firstChild).toHaveClass('animate-pulse');
  });

  it('applies a custom className alongside defaults', () => {
    const { container } = render(<Skeleton className="w-24 h-4" />);
    expect(container.firstChild).toHaveClass('w-24', 'h-4', 'animate-pulse');
  });

  it('renders a div element', () => {
    const { container } = render(<Skeleton />);
    expect(container.firstChild?.nodeName).toBe('DIV');
  });
});

describe('TableSkeleton', () => {
  it('renders a table element', () => {
    render(<TableSkeleton />);
    expect(screen.getByRole('table')).toBeInTheDocument();
  });

  it('renders 6 tbody rows by default', () => {
    const { container } = render(<TableSkeleton />);
    const rows = container.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(6);
  });

  it('renders 5 columns by default', () => {
    const { container } = render(<TableSkeleton />);
    const firstRow = container.querySelector('tbody tr');
    expect(firstRow?.querySelectorAll('td')).toHaveLength(5);
  });

  it('renders custom row count', () => {
    const { container } = render(<TableSkeleton rows={3} />);
    const rows = container.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(3);
  });

  it('renders custom column count', () => {
    const { container } = render(<TableSkeleton cols={4} />);
    const firstRow = container.querySelector('tbody tr');
    expect(firstRow?.querySelectorAll('td')).toHaveLength(4);
  });

  it('renders custom rows and cols together', () => {
    const { container } = render(<TableSkeleton rows={2} cols={3} />);
    const rows = container.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(2);
    expect(rows[0]?.querySelectorAll('td')).toHaveLength(3);
  });

  it('renders header columns equal to cols prop', () => {
    const { container } = render(<TableSkeleton cols={4} />);
    const headerCols = container.querySelectorAll('thead th');
    expect(headerCols).toHaveLength(4);
  });
});

describe('CardSkeleton', () => {
  it('renders 4 cards by default', () => {
    const { container } = render(<CardSkeleton />);
    const cards = container.querySelectorAll('.panel');
    expect(cards).toHaveLength(4);
  });

  it('renders a custom number of cards', () => {
    const { container } = render(<CardSkeleton count={2} />);
    const cards = container.querySelectorAll('.panel');
    expect(cards).toHaveLength(2);
  });

  it('renders 1 card when count is 1', () => {
    const { container } = render(<CardSkeleton count={1} />);
    const cards = container.querySelectorAll('.panel');
    expect(cards).toHaveLength(1);
  });
});
