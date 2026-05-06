import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import EmptyState from '@/components/EmptyState';

describe('EmptyState', () => {
  it('renders the title', () => {
    render(<EmptyState icon={<span>icon</span>} title="No data found" />);
    expect(screen.getByText('No data found')).toBeInTheDocument();
  });

  it('renders the icon', () => {
    render(<EmptyState icon={<span data-testid="my-icon">★</span>} title="Empty" />);
    expect(screen.getByTestId('my-icon')).toBeInTheDocument();
  });

  it('renders description when provided', () => {
    render(
      <EmptyState
        icon={<span>icon</span>}
        title="Empty"
        description="Nothing here yet"
      />
    );
    expect(screen.getByText('Nothing here yet')).toBeInTheDocument();
  });

  it('does not render description when omitted', () => {
    render(<EmptyState icon={<span>icon</span>} title="Empty" />);
    expect(screen.queryByText('Nothing here yet')).not.toBeInTheDocument();
  });

  it('renders action node when provided', () => {
    render(
      <EmptyState
        icon={<span>icon</span>}
        title="Empty"
        action={<button>Add item</button>}
      />
    );
    expect(screen.getByRole('button', { name: 'Add item' })).toBeInTheDocument();
  });

  it('does not render action when omitted', () => {
    render(<EmptyState icon={<span>icon</span>} title="Empty" />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('renders both description and action when both are provided', () => {
    render(
      <EmptyState
        icon={<span>icon</span>}
        title="Empty"
        description="Try adding something"
        action={<button>Add</button>}
      />
    );
    expect(screen.getByText('Try adding something')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add' })).toBeInTheDocument();
  });
});
