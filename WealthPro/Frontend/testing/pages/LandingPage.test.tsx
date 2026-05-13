import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LandingPage from '@/pages/public/LandingPage';

function renderLanding() {
  return render(
    <MemoryRouter>
      <LandingPage />
    </MemoryRouter>
  );
}

describe('LandingPage', () => {
  // ─── Branding ────────────────────────────────────────────────────────────────

  it('renders the WealthPro brand name', () => {
    renderLanding();
    // There are two "WealthPro" instances (navbar + footer)
    const brands = screen.getAllByText('WealthPro');
    expect(brands.length).toBeGreaterThanOrEqual(1);
  });

  it('renders a "W" logo mark', () => {
    renderLanding();
    const logos = screen.getAllByText('W');
    expect(logos.length).toBeGreaterThanOrEqual(1);
  });

  // ─── Navigation ──────────────────────────────────────────────────────────────

  it('renders a Log in link', () => {
    renderLanding();
    const loginLinks = screen.getAllByRole('link', { name: /log in/i });
    expect(loginLinks.length).toBeGreaterThanOrEqual(1);
    expect(loginLinks[0]).toHaveAttribute('href', '/login');
  });

  it('renders a "Get started" CTA link', () => {
    renderLanding();
    const ctaLink = screen.getByRole('link', { name: /get started/i });
    expect(ctaLink).toHaveAttribute('href', '/login');
  });

  // ─── Hero section ────────────────────────────────────────────────────────────

  it('renders the hero headline', () => {
    renderLanding();
    const matches = screen.getAllByText(/Grow Your/i);
    expect(matches.length).toBeGreaterThanOrEqual(1);
  });

  it('renders the "Start investing" CTA', () => {
    renderLanding();
    const startLink = screen.getByRole('link', { name: /Start investing/i });
    expect(startLink).toHaveAttribute('href', '/login');
  });

  it('renders the SEBI Registered badge', () => {
    renderLanding();
    expect(screen.getByText(/SEBI Registered Wealth Platform/i)).toBeInTheDocument();
  });

  // ─── Ticker bar ──────────────────────────────────────────────────────────────

  it('renders the LIVE indicator', () => {
    renderLanding();
    const liveEls = screen.getAllByText('LIVE');
    expect(liveEls.length).toBeGreaterThanOrEqual(1);
  });

  it('renders HDFCBANK ticker symbol', () => {
    renderLanding();
    const matches = screen.getAllByText('HDFCBANK');
    expect(matches.length).toBeGreaterThanOrEqual(1);
  });

  it('renders RELIANCE ticker symbol', () => {
    renderLanding();
    const matches = screen.getAllByText('RELIANCE');
    expect(matches.length).toBeGreaterThanOrEqual(1);
  });

  // ─── Features section ────────────────────────────────────────────────────────

  it('renders Platform Features heading', () => {
    renderLanding();
    expect(screen.getByText('Platform Features')).toBeInTheDocument();
  });

  it('renders Real-Time Portfolio Tracking feature', () => {
    renderLanding();
    expect(screen.getByText('Real-Time Portfolio Tracking')).toBeInTheDocument();
  });

  it('renders Goal-Based Investing feature', () => {
    renderLanding();
    expect(screen.getByText('Goal-Based Investing')).toBeInTheDocument();
  });

  it('renders Bank-Grade Security feature', () => {
    renderLanding();
    expect(screen.getByText('Bank-Grade Security')).toBeInTheDocument();
  });

  it('renders Seamless Order Execution feature', () => {
    renderLanding();
    expect(screen.getByText('Seamless Order Execution')).toBeInTheDocument();
  });

  it('renders Multi-Role Platform feature', () => {
    renderLanding();
    expect(screen.getByText('Multi-Role Platform')).toBeInTheDocument();
  });

  // ─── How It Works section ────────────────────────────────────────────────────

  it('renders the How It Works section', () => {
    renderLanding();
    expect(screen.getByText('From onboarding to growth in 4 steps')).toBeInTheDocument();
  });

  it('renders the 4 step numbers', () => {
    renderLanding();
    expect(screen.getByText('01')).toBeInTheDocument();
    expect(screen.getByText('02')).toBeInTheDocument();
    expect(screen.getByText('03')).toBeInTheDocument();
    expect(screen.getByText('04')).toBeInTheDocument();
  });

  it('renders step titles', () => {
    renderLanding();
    expect(screen.getByText('Create & Onboard')).toBeInTheDocument();
    expect(screen.getByText('Set Your Goals')).toBeInTheDocument();
    expect(screen.getByText('Get RM Advice')).toBeInTheDocument();
    expect(screen.getByText('Grow Your Wealth')).toBeInTheDocument();
  });

  // ─── Testimonials ────────────────────────────────────────────────────────────

  it('renders the Testimonials section heading', () => {
    renderLanding();
    expect(screen.getByText('Trusted by clients and advisors')).toBeInTheDocument();
  });

  it('renders Amit Rawal testimonial', () => {
    renderLanding();
    expect(screen.getByText('Amit Rawal')).toBeInTheDocument();
  });

  it('renders Priya Sharma testimonial', () => {
    renderLanding();
    expect(screen.getByText('Priya Sharma')).toBeInTheDocument();
  });

  // ─── CTA section ─────────────────────────────────────────────────────────────

  it('renders the Open your account CTA link', () => {
    renderLanding();
    const openLink = screen.getByRole('link', { name: /Open your account/i });
    expect(openLink).toHaveAttribute('href', '/login');
  });

  // ─── Footer ──────────────────────────────────────────────────────────────────

  it('renders the copyright notice', () => {
    renderLanding();
    expect(screen.getByText(/© 2026 WealthPro/)).toBeInTheDocument();
  });

  it('renders footer product links', () => {
    renderLanding();
    expect(screen.getByText('Portfolio Tracking')).toBeInTheDocument();
    expect(screen.getByText('Goal Planning')).toBeInTheDocument();
  });

  it('renders footer role links', () => {
    renderLanding();
    const rmTexts = screen.getAllByText('Relationship Manager');
    expect(rmTexts.length).toBeGreaterThanOrEqual(1);
    const dealerTexts = screen.getAllByText('Dealer');
    expect(dealerTexts.length).toBeGreaterThanOrEqual(1);
  });
});
