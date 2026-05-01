import { api } from './client';

export async function getGoalsByClientId(clientId: number) {
  const res = await api.get('/api/goals/client/' + clientId);
  return res.data;
}

export async function createGoal(data: {
  clientId: number;
  goalType: string;
  targetAmount: number;
  targetDate: string;
  priority: number;
  status: string;
}) {
  const res = await api.post('/api/goals', data);
  return res.data;
}

export async function updateGoal(goalId: number, data: any) {
  const res = await api.put('/api/goals/' + goalId, data);
  return res.data;
}

export async function deleteGoal(goalId: number) {
  const res = await api.delete('/api/goals/' + goalId);
  return res.data;
}

export async function updateGoalStatus(goalId: number, data: any) {
  const res = await api.put('/api/goals/' + goalId, data);
  return res.data;
}
