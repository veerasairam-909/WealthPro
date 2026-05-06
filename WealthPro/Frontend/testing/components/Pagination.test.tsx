import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Pagination from '@/components/Pagination';

describe('Pagination', () => {
  it('renders nothing when all items fit on one page', () => {
    const { container } = render(
      <Pagination page={0} total={5} pageSize={10} onChange={vi.fn()} />
    );
    expect(container.firstChild).toBeNull();
  });

  it('renders nothing when total is zero', () => {
    const { container } = render(
      <Pagination page={0} total={0} pageSize={10} onChange={vi.fn()} />
    );
    expect(container.firstChild).toBeNull();
  });

  it('renders nothing when total exactly equals pageSize', () => {
    const { container } = render(
      <Pagination page={0} total={10} pageSize={10} onChange={vi.fn()} />
    );
    expect(container.firstChild).toBeNull();
  });

  it('shows correct range text for first page', () => {
    render(<Pagination page={0} total={50} pageSize={10} onChange={vi.fn()} />);
    expect(screen.getByText(/Showing 1–10 of 50/)).toBeInTheDocument();
  });

  it('shows correct range text for second page', () => {
    render(<Pagination page={1} total={50} pageSize={10} onChange={vi.fn()} />);
    expect(screen.getByText(/Showing 11–20 of 50/)).toBeInTheDocument();
  });

  it('shows correct range text on last page with fewer items', () => {
    render(<Pagination page={2} total={25} pageSize={10} onChange={vi.fn()} />);
    expect(screen.getByText(/Showing 21–25 of 25/)).toBeInTheDocument();
  });

  it('disables Previous button on the first page', () => {
    render(<Pagination page={0} total={30} pageSize={10} onChange={vi.fn()} />);
    expect(screen.getByLabelText('Previous page')).toBeDisabled();
  });

  it('disables Next button on the last page', () => {
    render(<Pagination page={2} total={30} pageSize={10} onChange={vi.fn()} />);
    expect(screen.getByLabelText('Next page')).toBeDisabled();
  });

  it('enables both buttons on a middle page', () => {
    render(<Pagination page={1} total={30} pageSize={10} onChange={vi.fn()} />);
    expect(screen.getByLabelText('Previous page')).not.toBeDisabled();
    expect(screen.getByLabelText('Next page')).not.toBeDisabled();
  });

  it('calls onChange with page-1 when Previous is clicked', async () => {
    const onChange = vi.fn();
    render(<Pagination page={2} total={50} pageSize={10} onChange={onChange} />);
    await userEvent.click(screen.getByLabelText('Previous page'));
    expect(onChange).toHaveBeenCalledWith(1);
  });

  it('calls onChange with page+1 when Next is clicked', async () => {
    const onChange = vi.fn();
    render(<Pagination page={0} total={50} pageSize={10} onChange={onChange} />);
    await userEvent.click(screen.getByLabelText('Next page'));
    expect(onChange).toHaveBeenCalledWith(1);
  });

  it('calls onChange with correct page when a numbered button is clicked', async () => {
    const onChange = vi.fn();
    render(<Pagination page={0} total={30} pageSize={10} onChange={onChange} />);
    // page button "2" maps to 0-indexed page 1
    await userEvent.click(screen.getByRole('button', { name: '2' }));
    expect(onChange).toHaveBeenCalledWith(1);
  });

  it('renders page number buttons for small page counts', () => {
    render(<Pagination page={0} total={30} pageSize={10} onChange={vi.fn()} />);
    expect(screen.getByRole('button', { name: '1' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '2' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '3' })).toBeInTheDocument();
  });
});
