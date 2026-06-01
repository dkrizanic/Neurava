package com.notebook.api.projects.application;

import java.time.Instant;
import java.util.UUID;

import com.notebook.api.projects.domain.ProjectRecord;
import com.notebook.api.projects.domain.ProjectStatus;

public record ProjectSummary(
		UUID id,
		UUID ownerAccountId,
		UUID workspaceContextId,
		String name,
		String description,
		ProjectStatus status,
		Instant createdAt,
		Instant updatedAt
) {

	public static ProjectSummary from(ProjectRecord project) {
		return new ProjectSummary(
				project.getId(),
				project.getOwnerAccountId(),
				project.getWorkspaceContextId(),
				project.getName(),
				project.getDescription(),
				project.getStatus(),
				project.getCreatedAt(),
				project.getUpdatedAt());
	}
}

