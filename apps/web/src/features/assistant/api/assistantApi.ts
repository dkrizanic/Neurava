import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { SourceAwareAnswer } from '../types';

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
