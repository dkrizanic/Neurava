package com.notebook.api.workspace.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.auth.domain.AuthAccount;
import com.notebook.api.workspace.domain.WorkspaceContext;
import com.notebook.api.workspace.domain.WorkspaceContextType;

public interface WorkspaceContextRepository extends JpaRepository<WorkspaceContext, UUID> {

	Optional<WorkspaceContext> findByOwnerAccountAndType(AuthAccount ownerAccount, WorkspaceContextType type);
}
