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

import com.notebook.api.workspace.application.PersonalWorkspaceService;
import com.notebook.api.workspace.application.WorkspaceSession;
import com.notebook.api.workspace.domain.WorkspaceContext;
import com.notebook.api.workspace.domain.WorkspaceContextType;
import com.notebook.api.workspace.domain.WorkspaceMembership;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceContextRepository;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceMembershipRepository;

class PersonalWorkspaceServiceUnitTest {

	private final WorkspaceContextRepository workspaces = mock(WorkspaceContextRepository.class);
	private final WorkspaceMembershipRepository memberships = mock(WorkspaceMembershipRepository.class);
	private final PersonalWorkspaceService service = new PersonalWorkspaceService(this.workspaces, this.memberships);

	@Test
	void createsPersonalWorkspaceAndOwnerMembershipWhenMissing() {
		UUID accountId = UUID.randomUUID();
		when(this.workspaces.findByOwnerAccountIdAndType(accountId, WorkspaceContextType.PERSONAL)).thenReturn(Optional.empty());
		when(this.workspaces.save(any(WorkspaceContext.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(this.memberships.existsByAccountIdAndWorkspaceContext(any(), any())).thenReturn(false);
		when(this.memberships.save(any(WorkspaceMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(this.memberships.countByAccountId(accountId)).thenReturn(1L);

		WorkspaceSession session = this.service.ensurePersonalWorkspace(accountId);

		assertThat(session.activeWorkspace().name()).isEqualTo("Personal");
		assertThat(session.activeWorkspace().type()).isEqualTo("PERSONAL");
		assertThat(session.workspaceSwitcherAvailable()).isFalse();
		verify(this.workspaces).save(any(WorkspaceContext.class));
		verify(this.memberships).save(any(WorkspaceMembership.class));
	}

	@Test
	void reusesExistingPersonalWorkspaceAndDoesNotDuplicateMembership() {
		UUID accountId = UUID.randomUUID();
		WorkspaceContext existing = WorkspaceContext.personalFor(accountId, Instant.now());
		when(this.workspaces.findByOwnerAccountIdAndType(accountId, WorkspaceContextType.PERSONAL)).thenReturn(Optional.of(existing));
		when(this.memberships.existsByAccountIdAndWorkspaceContext(accountId, existing)).thenReturn(true);
		when(this.memberships.countByAccountId(accountId)).thenReturn(1L);

		WorkspaceSession session = this.service.ensurePersonalWorkspace(accountId);

		assertThat(session.activeWorkspace().id()).isEqualTo(existing.getId());
		assertThat(session.workspaceSwitcherAvailable()).isFalse();
		verify(this.workspaces, never()).save(any(WorkspaceContext.class));
		verify(this.memberships, never()).save(any(WorkspaceMembership.class));
	}

	@Test
	void reportsSwitcherAvailableWhenAccountHasMultipleMemberships() {
		UUID accountId = UUID.randomUUID();
		WorkspaceContext existing = WorkspaceContext.personalFor(accountId, Instant.now());
		when(this.workspaces.findByOwnerAccountIdAndType(accountId, WorkspaceContextType.PERSONAL)).thenReturn(Optional.of(existing));
		when(this.memberships.existsByAccountIdAndWorkspaceContext(accountId, existing)).thenReturn(true);
		when(this.memberships.countByAccountId(accountId)).thenReturn(2L);

		WorkspaceSession session = this.service.ensurePersonalWorkspace(accountId);

		assertThat(session.workspaceSwitcherAvailable()).isTrue();
	}
}
