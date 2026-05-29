package com.notebook.api.workspace.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.workspace.domain.RegisteredCompany;

public interface RegisteredCompanyRepository extends JpaRepository<RegisteredCompany, UUID> {

	Optional<RegisteredCompany> findByOwnerAccountId(UUID ownerAccountId);
}
