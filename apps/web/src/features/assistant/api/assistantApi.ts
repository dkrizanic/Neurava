import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { HistorySummary, SourceAwareAnswer } from '../types';

export async function answerQuestion(question: string, signal?: AbortSignal): Promise<SourceAwareAnswer> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/answers`, {
    body: JSON.stringify({ question }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throw new Error('Unable to answer question');
  }

  return response.json() as Promise<SourceAwareAnswer>;
}

export async function summarizeHistory(topic: string, signal?: AbortSignal): Promise<HistorySummary> {
  const response = await fetch(`${apiBaseUrl}/api/v1/ai/summaries`, {
    body: JSON.stringify({ topic }),
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    method: 'POST',
    signal,
  });

  if (!response.ok) {
    throw new Error('Unable to summarize history');
  }

  return response.json() as Promise<HistorySummary>;
}
