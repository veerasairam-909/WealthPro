import { describe, it, expect, vi, beforeEach } from 'vitest';
import { api } from '@/api/client';
import {
  getGoalsByClientId,
  createGoal,
  updateGoal,
  deleteGoal,
  updateGoalStatus,
} from '@/api/goals';

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('goals API', () => {
  beforeEach(() => vi.clearAllMocks());

  // ─── getGoalsByClientId ──────────────────────────────────────────────────────

  it('getGoalsByClientId calls correct endpoint', async () => {
    const goals = [{ goalId: 1, goalType: 'RETIREMENT' }];
    vi.mocked(api.get).mockResolvedValue({ data: goals });

    const result = await getGoalsByClientId(10);

    expect(vi.mocked(api.get)).toHaveBeenCalledWith('/api/goals/client/10');
    expect(result).toEqual(goals);
  });

  it('getGoalsByClientId returns empty list', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: [] });

    const result = await getGoalsByClientId(99);

    expect(result).toEqual([]);
  });

  // ─── createGoal ──────────────────────────────────────────────────────────────

  it('createGoal posts to /api/goals', async () => {
    const payload = {
      clientId: 10,
      goalType: 'RETIREMENT',
      targetAmount: 1000000,
      targetDate: '2040-01-01',
      priority: 1,
      status: 'ACTIVE',
    };
    const created = { goalId: 5, ...payload };
    vi.mocked(api.post).mockResolvedValue({ data: created });

    const result = await createGoal(payload);

    expect(vi.mocked(api.post)).toHaveBeenCalledWith('/api/goals', payload);
    expect(result.goalId).toBe(5);
  });

  it('createGoal returns the created goal', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { goalId: 8, goalType: 'EDUCATION' } });

    const result = await createGoal({
      clientId: 3,
      goalType: 'EDUCATION',
      targetAmount: 500000,
      targetDate: '2030-06-01',
      priority: 2,
      status: 'ACTIVE',
    });

    expect(result.goalType).toBe('EDUCATION');
  });

  // ─── updateGoal ──────────────────────────────────────────────────────────────

  it('updateGoal puts to /api/goals/{id}', async () => {
    const update = { targetAmount: 1200000 };
    vi.mocked(api.put).mockResolvedValue({ data: { goalId: 5, ...update } });

    const result = await updateGoal(5, update);

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/goals/5', update);
    expect(result.targetAmount).toBe(1200000);
  });

  it('updateGoal returns updated goal', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { goalId: 3, status: 'COMPLETED' } });

    const result = await updateGoal(3, { status: 'COMPLETED' });

    expect(result.status).toBe('COMPLETED');
  });

  // ─── updateGoalStatus ────────────────────────────────────────────────────────

  it('updateGoalStatus puts to /api/goals/{id}', async () => {
    vi.mocked(api.put).mockResolvedValue({ data: { goalId: 6, status: 'COMPLETED' } });

    const result = await updateGoalStatus(6, { status: 'COMPLETED' });

    expect(vi.mocked(api.put)).toHaveBeenCalledWith('/api/goals/6', { status: 'COMPLETED' });
    expect(result.status).toBe('COMPLETED');
  });

  // ─── deleteGoal ──────────────────────────────────────────────────────────────

  it('deleteGoal sends DELETE to /api/goals/{id}', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: null });

    await deleteGoal(5);

    expect(vi.mocked(api.delete)).toHaveBeenCalledWith('/api/goals/5');
  });

  it('deleteGoal returns response data', async () => {
    vi.mocked(api.delete).mockResolvedValue({ data: { message: 'deleted' } });

    const result = await deleteGoal(7);

    expect(result).toEqual({ message: 'deleted' });
  });
});
