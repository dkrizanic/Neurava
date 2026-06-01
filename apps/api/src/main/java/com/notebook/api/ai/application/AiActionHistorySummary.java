package com.notebook.api.ai.application;

import java.time.Instant;
import java.util.UUID;

import com.notebook.api.ai.domain.AiActionHistoryRecord;

public record AiActionHistorySummary(
		UUID id,
		UUID ownerAccountId,
		UUID workspaceContextId,
		String action,
		String entityType,
		UUID entityId,
		String changeType,
		String summary,
		String previousState,
		String currentState,
		Instant createdAt
) {

	public static AiActionHistorySummary from(AiActionHistoryRecord record) {
		return new AiActionHistorySummary(
				record.getId(),
				record.getOwnerAccountId(),
				record.getWorkspaceContextId(),
				record.getAction(),
				record.getEntityType(),
				record.getEntityId(),
				record.getChangeType(),
				record.getSummary(),
				record.getPreviousState(),
				record.getCurrentState(),
				record.getCreatedAt());
	}
}
