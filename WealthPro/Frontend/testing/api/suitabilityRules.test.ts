import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getAllSuitabilityRules,
  createSuitabilityRule,
  updateSuitabilityRule,
  deleteSuitabilityRule,
} from '@/api/suitabilityRules';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('suitabilityRules API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getAllSuitabilityRules ───────────────────────────────────────────────────

  it('getAllSuitabilityRules calls /api/suitability-rules', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    await getAllSuitabilityRules();

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/suitability-rules');
  });

  it('getAllSuitabilityRules returns list of rules', async () => {
    const rules = [{ ruleId: 1, expression: 'riskClass == CONSERVATIVE' }];
    vi.mocked(api.get).mockResolvedValue({ data: rules });

    const result = await getAllSuitabilityRules();

    expect(result).toHaveLength(1);
    expect(result[0].expression).toBe('riskClass == CONSERVATIVE');
  });

  it('getAllSuitabilityRules returns empty list', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    const result = await getAllSuitabilityRules();

    expect(result).toEqual([]);
  });

  // ─── createSuitabilityRule ───────────────────────────────────────────────────

  it('createSuitabilityRule posts to /api/suitability-rules', async () => {
    const payload = {
      description: 'Conservative only',
      expression: 'riskClass == CONSERVATIVE',
      status: 'ACTIVE',
    };
    const created = { ruleId: 5, ...payload };
    vi.mocked(api.post).mockResolvedValue({ data: created });

    const result = await createSuitabilityRule(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/suitability-rules', payload);
    expect(result.ruleId).toBe(5);
  });

  it('createSuitabilityRule returns created rule', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { ruleId: 10, status: 'ACTIVE' } });

    const result = await createSuitabilityRule({
      description: 'High risk',
      expression: 'riskClass == AGGRESSIVE',
      status: 'ACTIVE',
    });

    expect(result.status).toBe('ACTIVE');
  });

  // ─── updateSuitabilityRule ───────────────────────────────────────────────────

  it('updateSuitabilityRule puts to correct endpoint', async () => {
    const update = { status: 'INACTIVE' };
    vi.mocked(api.put).mockResolvedValue({ data: { ruleId: 3, ...update } });

    const result = await updateSuitabilityRule(3, update);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/suitability-rules/3', update);
    expect(result.status).toBe('INACTIVE');
  });

  it('updateSuitabilityRule can update expression', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { ruleId: 4, expression: 'riskClass == MODERATE' } });

    const result = await updateSuitabilityRule(4, { expression: 'riskClass == MODERATE' });

    expect(result.expression).toBe('riskClass == MODERATE');
  });

  // ─── deleteSuitabilityRule ───────────────────────────────────────────────────

  it('deleteSuitabilityRule sends DELETE to correct endpoint', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await deleteSuitabilityRule(7);

    expect(vi.mocked(api.delete)).toHaveBeenCalledWith('/api/suitability-rules/7');
  });

  it('deleteSuitabilityRule resolves without returning data', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await expect(deleteSuitabilityRule(7)).resolves.toBeUndefined();
  });
});
