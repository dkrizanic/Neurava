package com.notebook.api.plans.application;

import java.time.Instant;
import java.util.UUID;

import com.notebook.api.plans.domain.PlanRecord;
import com.notebook.api.plans.domain.PlanStatus;

public record PlanSummary(
		UUID id,
		UUID ownerAccountId,
		UUID workspaceContextId,
		String title,
		String goal,
		String items,
		String linkedResources,
		PlanStatus status,
		Instant createdAt,
		Instant updatedAt
) {

	public static PlanSummary from(PlanRecord plan) {
		return new PlanSummary(
				plan.getId(),
				plan.getOwnerAccountId(),
				plan.getWorkspaceContextId(),
				plan.getTitle(),
				plan.getGoal(),
				plan.getItems(),
				plan.getLinkedResources(),
				plan.getStatus(),
				plan.getCreatedAt(),
				plan.getUpdatedAt());
	}
}

