package com.notebook.api.integrations.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "integration_connection")
public class IntegrationConnection {

	@Id
	private UUID id;

	@Column(name = "owner_account_id", nullable = false)
	private UUID ownerAccountId;

	@Column(name = "workspace_context_id", nullable = false)
	private UUID workspaceContextId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private IntegrationProvider provider;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "permission_summary", nullable = false, length = 512)
	private String permissionSummary;

	@Column(name = "connected_at")
	private Instant connectedAt;

	@Column(name = "disconnected_at")
	private Instant disconnectedAt;

	protected IntegrationConnection() {
	}

	private IntegrationConnection(UUID ownerAccountId, UUID workspaceContextId, IntegrationProvider provider,
			String permissionSummary, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.workspaceContextId = workspaceContextId;
		this.provider = provider;
		connect(permissionSummary, now);
	}

	public static IntegrationConnection connect(UUID ownerAccountId, UUID workspaceContextId,
			IntegrationProvider provider, String permissionSummary, Instant now) {
		return new IntegrationConnection(ownerAccountId, workspaceContextId, provider, permissionSummary, now);
	}

	public void connect(String permissionSummary, Instant now) {
		this.enabled = true;
		this.permissionSummary = permissionSummary == null ? "" : permissionSummary.trim();
		this.connectedAt = now;
		this.disconnectedAt = null;
	}

	public void disconnect(Instant now) {
		this.enabled = false;
		this.disconnectedAt = now;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getOwnerAccountId() {
		return this.ownerAccountId;
	}

	public UUID getWorkspaceContextId() {
		return this.workspaceContextId;
	}

	public IntegrationProvider getProvider() {
		return this.provider;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getPermissionSummary() {
		return this.permissionSummary;
	}

	public Instant getConnectedAt() {
		return this.connectedAt;
	}

	public Instant getDisconnectedAt() {
		return this.disconnectedAt;
	}
}

