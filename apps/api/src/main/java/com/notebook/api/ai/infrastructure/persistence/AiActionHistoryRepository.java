package com.notebook.api.ai.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.ai.domain.AiActionHistoryRecord;

public interface AiActionHistoryRepository extends JpaRepository<AiActionHistoryRecord, UUID> {

	List<AiActionHistoryRecord> findByWorkspaceContextIdOrderByCreatedAtDesc(UUID workspaceContextId);
}
