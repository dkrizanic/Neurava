package com.notebook.api.plans.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.plans.domain.PlanRecord;

public interface PlanRepository extends JpaRepository<PlanRecord, UUID> {

	List<PlanRecord> findByWorkspaceContextIdOrderByUpdatedAtDesc(UUID workspaceContextId);

	Optional<PlanRecord> findByIdAndWorkspaceContextId(UUID id, UUID workspaceContextId);
}

