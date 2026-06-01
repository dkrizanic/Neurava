package com.notebook.api.integrations.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.integrations.domain.IntegrationConnection;
import com.notebook.api.integrations.domain.IntegrationProvider;

public interface IntegrationConnectionRepository extends JpaRepository<IntegrationConnection, UUID> {

	List<IntegrationConnection> findByWorkspaceContextIdOrderByProviderAsc(UUID workspaceContextId);

	Optional<IntegrationConnection> findByWorkspaceContextIdAndProvider(UUID workspaceContextId,
			IntegrationProvider provider);
}

