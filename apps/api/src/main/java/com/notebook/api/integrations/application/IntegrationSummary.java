package com.notebook.api.integrations.application;

import java.time.Instant;
import java.util.UUID;

import com.notebook.api.integrations.domain.IntegrationConnection;
import com.notebook.api.integrations.domain.IntegrationProvider;

public record IntegrationSummary(
		UUID id,
		UUID workspaceContextId,
		IntegrationProvider provider,
		boolean enabled,
		String permissionSummary,
		Instant connectedAt,
		Instant disconnectedAt
) {

	public static IntegrationSummary from(IntegrationConnection connection) {
		return new IntegrationSummary(
				connection.getId(),
				connection.getWorkspaceContextId(),
				connection.getProvider(),
				connection.isEnabled(),
				connection.getPermissionSummary(),
				connection.getConnectedAt(),
				connection.getDisconnectedAt());
	}
}

