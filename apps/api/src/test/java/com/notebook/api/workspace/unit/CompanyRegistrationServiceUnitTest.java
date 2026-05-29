package com.notebook.api.workspace.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.notebook.api.workspace.application.CompanyRegistration;
import com.notebook.api.workspace.application.CompanyRegisteredEvent;
import com.notebook.api.workspace.application.CompanyRegistrationService;
import com.notebook.api.workspace.domain.RegisteredCompany;
import com.notebook.api.workspace.domain.WorkspaceContext;
import com.notebook.api.workspace.domain.WorkspaceContextType;
import com.notebook.api.workspace.domain.WorkspaceMembership;
import com.notebook.api.workspace.infrastructure.persistence.RegisteredCompanyRepository;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceContextRepository;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceMembershipRepository;

class CompanyRegistrationServiceUnitTest {

	private final RegisteredCompanyRepository companies = mock(RegisteredCompanyRepository.class);
	private final WorkspaceContextRepository workspaces = mock(WorkspaceContextRepository.class);
	private final WorkspaceMembershipRepository memberships = mock(WorkspaceMembershipRepository.class);
	private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
	private final CompanyRegistrationService service =
			new CompanyRegistrationService(this.companies, this.workspaces, this.memberships, this.events);

	@Test
	void createsBusinessWorkspaceCompanyAndOwnerMembership() {
		UUID accountId = UUID.randomUUID();
		when(this.companies.findByOwnerAccountId(accountId)).thenReturn(Optional.empty());
		when(this.workspaces.findByOwnerAccountIdAndType(accountId, WorkspaceContextType.BUSINESS)).thenReturn(Optional.empty());
		when(this.workspaces.save(any(WorkspaceContext.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(this.memberships.existsByAccountIdAndWorkspaceContext(any(), any())).thenReturn(false);
		when(this.memberships.save(any(WorkspaceMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(this.companies.save(any(RegisteredCompany.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CompanyRegistration registration = this.service.register(accountId, "Acme Labs");

		assertThat(registration.company().name()).isEqualTo("Acme Labs");
		assertThat(registration.businessWorkspace().name()).isEqualTo("Acme Labs");
		assertThat(registration.businessWorkspace().type()).isEqualTo("BUSINESS");
		verify(this.workspaces).save(any(WorkspaceContext.class));
		verify(this.memberships).save(any(WorkspaceMembership.class));
		verify(this.companies).save(any(RegisteredCompany.class));
		ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
		verify(this.events).publishEvent(event.capture());
		assertThat(event.getValue()).isInstanceOf(CompanyRegisteredEvent.class);
		assertThat(((CompanyRegisteredEvent) event.getValue()).ownerAccountId()).isEqualTo(accountId);
	}

	@Test
	void reusesExistingCompanyRegistration() {
		UUID accountId = UUID.randomUUID();
		WorkspaceContext workspace = WorkspaceContext.businessFor(accountId, "Acme Labs", Instant.now());
		RegisteredCompany existing = RegisteredCompany.register(accountId, workspace, "Acme Labs", Instant.now());
		when(this.companies.findByOwnerAccountId(accountId)).thenReturn(Optional.of(existing));

		CompanyRegistration registration = this.service.register(accountId, "Different Name");

		assertThat(registration.company().id()).isEqualTo(existing.getId());
		assertThat(registration.businessWorkspace().id()).isEqualTo(workspace.getId());
		verify(this.workspaces, never()).save(any(WorkspaceContext.class));
		verify(this.memberships, never()).save(any(WorkspaceMembership.class));
		verify(this.companies, never()).save(any(RegisteredCompany.class));
		verify(this.events, never()).publishEvent(any());
	}
}
