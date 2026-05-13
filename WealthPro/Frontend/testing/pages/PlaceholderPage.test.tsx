import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import PlaceholderPage from '@/pages/PlaceholderPage';

describe('PlaceholderPage', () => {
  it('renders the given title', () => {
    render(<PlaceholderPage title="Corporate Actions" />);
    expect(screen.getByRole('heading', { name: 'Corporate Actions' })).toBeInTheDocument();
  });

  it('renders the optional message when provided', () => {
    render(<PlaceholderPage title="Research Notes" message="Feature coming in Q3" />);
    expect(screen.getByText('Feature coming in Q3')).toBeInTheDocument();
  });

  it('does not render a message paragraph when message is not provided', () => {
    render(<PlaceholderPage title="Advisor Tools" />);
    expect(screen.queryByText('Feature coming in Q3')).not.toBeInTheDocument();
  });

  it('renders the "Coming soon" label', () => {
    render(<PlaceholderPage title="Some Page" />);
    expect(screen.getByText('Coming soon')).toBeInTheDocument();
  });

  it('renders the later-phase message', () => {
    render(<PlaceholderPage title="Some Page" />);
    expect(screen.getByText('This page will be built in a later phase.')).toBeInTheDocument();
  });

  it('renders the construction emoji', () => {
    render(<PlaceholderPage title="Some Page" />);
    expect(screen.getByText('🚧')).toBeInTheDocument();
  });

  it('can render with a different title each time', () => {
    const { rerender } = render(<PlaceholderPage title="First Title" />);
    expect(screen.getByRole('heading', { name: 'First Title' })).toBeInTheDocument();

    rerender(<PlaceholderPage title="Second Title" />);
    expect(screen.getByRole('heading', { name: 'Second Title' })).toBeInTheDocument();
  });
});
