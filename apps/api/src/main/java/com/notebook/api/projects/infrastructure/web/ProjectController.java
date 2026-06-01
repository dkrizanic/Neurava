package com.notebook.api.projects.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.projects.application.ProjectHistorySummary;
import com.notebook.api.projects.application.ProjectService;
import com.notebook.api.projects.application.ProjectSummary;
import com.notebook.api.projects.domain.ProjectStatus;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/projects")
class ProjectController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final ProjectService projects;

	ProjectController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions, ProjectService projects) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.projects = projects;
	}

	@GetMapping
	List<ProjectSummary> list(Authentication authentication) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.projects.list(activeWorkspaceId(accountId));
	}

	@PostMapping
	ProjectSummary create(Authentication authentication, @Valid @RequestBody UpsertProjectRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.projects.create(accountId, activeWorkspaceId(accountId), request.name(), request.description());
	}

	@PatchMapping("/{projectId}")
	ProjectSummary update(Authentication authentication, @PathVariable UUID projectId,
			@Valid @RequestBody UpsertProjectRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.projects.update(projectId, activeWorkspaceId(accountId), request.name(), request.description(),
				request.status());
	}

	@GetMapping("/{projectId}/summary")
	ProjectHistorySummary summarize(Authentication authentication, @PathVariable UUID projectId) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.projects.summarize(projectId, activeWorkspaceId(accountId));
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	record UpsertProjectRequest(
			@NotBlank(message = "Project name is required.")
			@Size(max = 180, message = "Project name must be 180 characters or fewer.")
			String name,
			@Size(max = 20000, message = "Description must be 20000 characters or fewer.")
			String description,
			ProjectStatus status
	) {
		UpsertProjectRequest {
			if (status == null) {
				status = ProjectStatus.ACTIVE;
			}
		}
	}
}

