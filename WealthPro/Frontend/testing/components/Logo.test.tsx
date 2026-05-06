import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import Logo from '@/components/Logo';

describe('Logo', () => {
  it('renders the W initial', () => {
    render(<Logo />);
    expect(screen.getByText('W')).toBeInTheDocument();
  });

  it('renders the WealthPro brand name', () => {
    render(<Logo />);
    expect(screen.getByText('WealthPro')).toBeInTheDocument();
  });

  it('renders both W and WealthPro together', () => {
    render(<Logo />);
    expect(screen.getByText('W')).toBeInTheDocument();
    expect(screen.getByText('WealthPro')).toBeInTheDocument();
  });
});
