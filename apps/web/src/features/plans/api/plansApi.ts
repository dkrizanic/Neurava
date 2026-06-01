import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { Plan, PlanStatus, UpsertPlanInput } from '../types';

export async function fetchPlans(signal?: AbortSignal): Promise<Plan[]> {
  const response = await fetch(`${apiBaseUrl}/api/v1/plans`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    signal,
  });
  if (!response.ok) {
    throw new Error('Unable to load plans');
  }
  return response.json() as Promise<Plan[]>;
}

export async function createPlan(input: UpsertPlanInput): Promise<Plan> {
  const response = await fetch(`${apiBaseUrl}/api/v1/plans`, {
    body: JSON.stringify(input),
    credentials: 'include',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error('Unable to create plan');
  }
  return response.json() as Promise<Plan>;
}

export async function updatePlanStatus(id: string, status: PlanStatus): Promise<Plan> {
  const response = await fetch(`${apiBaseUrl}/api/v1/plans/${id}/status`, {
    body: JSON.stringify({ status }),
    credentials: 'include',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    method: 'PATCH',
  });
  if (!response.ok) {
    throw new Error('Unable to update plan');
  }
  return response.json() as Promise<Plan>;
}

