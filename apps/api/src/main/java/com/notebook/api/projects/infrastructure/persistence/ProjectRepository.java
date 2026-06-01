package com.notebook.api.projects.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.projects.domain.ProjectRecord;

public interface ProjectRepository extends JpaRepository<ProjectRecord, UUID> {

	List<ProjectRecord> findByWorkspaceContextIdOrderByUpdatedAtDesc(UUID workspaceContextId);

	Optional<ProjectRecord> findByIdAndWorkspaceContextId(UUID id, UUID workspaceContextId);
}

