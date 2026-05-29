package com.notebook.api.workspace.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notebook.api.workspace.domain.RegisteredCompany;
import com.notebook.api.workspace.domain.WorkspaceContext;
import com.notebook.api.workspace.domain.WorkspaceContextType;
import com.notebook.api.workspace.domain.WorkspaceMembership;
import com.notebook.api.workspace.infrastructure.persistence.RegisteredCompanyRepository;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceContextRepository;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceMembershipRepository;

@Service
public class CompanyRegistrationService {

	private final RegisteredCompanyRepository companies;
	private final WorkspaceContextRepository workspaces;
	private final WorkspaceMembershipRepository memberships;
	private final ApplicationEventPublisher events;

	public CompanyRegistrationService(RegisteredCompanyRepository companies, WorkspaceContextRepository workspaces,
			WorkspaceMembershipRepository memberships, ApplicationEventPublisher events) {
		this.companies = companies;
		this.workspaces = workspaces;
		this.memberships = memberships;
		this.events = events;
	}

	@Transactional
	public CompanyRegistration register(UUID ownerAccountId, String companyName) {
		RegisteredCompany company = this.companies.findByOwnerAccountId(ownerAccountId)
				.orElseGet(() -> createRegistration(ownerAccountId, companyName));
		return CompanyRegistration.from(company);
	}

	private RegisteredCompany createRegistration(UUID ownerAccountId, String companyName) {
		Instant now = Instant.now();
		WorkspaceContext businessWorkspace = this.workspaces
				.findByOwnerAccountIdAndType(ownerAccountId, WorkspaceContextType.BUSINESS)
				.orElseGet(() -> this.workspaces.save(WorkspaceContext.businessFor(ownerAccountId, companyName, now)));

		if (!this.memberships.existsByAccountIdAndWorkspaceContext(ownerAccountId, businessWorkspace)) {
			this.memberships.save(WorkspaceMembership.owner(ownerAccountId, businessWorkspace, now));
		}

		RegisteredCompany company = this.companies.save(RegisteredCompany.register(ownerAccountId, businessWorkspace, companyName, now));
		this.events.publishEvent(new CompanyRegisteredEvent(
				company.getId(),
				company.getOwnerAccountId(),
				businessWorkspace.getId(),
				company.getName()));
		return company;
	}
}
