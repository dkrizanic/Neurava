import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { Reminder, UpsertReminderInput } from '../types';

export async function fetchReminders(includeCompleted: boolean, signal?: AbortSignal): Promise<Reminder[]> {
  const response = await fetch(`${apiBaseUrl}/api/v1/reminders?includeCompleted=${includeCompleted}`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    signal,
  });
  if (!response.ok) {
    throw new Error('Unable to load reminders');
  }
  return response.json() as Promise<Reminder[]>;
}

export async function createReminder(input: UpsertReminderInput): Promise<Reminder> {
  const response = await fetch(`${apiBaseUrl}/api/v1/reminders`, {
    body: JSON.stringify(input),
    credentials: 'include',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error('Unable to create reminder');
  }
  return response.json() as Promise<Reminder>;
}

export async function completeReminder(id: string): Promise<Reminder> {
  const response = await fetch(`${apiBaseUrl}/api/v1/reminders/${id}/complete`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    method: 'PATCH',
  });
  if (!response.ok) {
    throw new Error('Unable to complete reminder');
  }
  return response.json() as Promise<Reminder>;
}

export async function reopenReminder(id: string): Promise<Reminder> {
  const response = await fetch(`${apiBaseUrl}/api/v1/reminders/${id}/reopen`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    method: 'PATCH',
  });
  if (!response.ok) {
    throw new Error('Unable to reopen reminder');
  }
  return response.json() as Promise<Reminder>;
}

