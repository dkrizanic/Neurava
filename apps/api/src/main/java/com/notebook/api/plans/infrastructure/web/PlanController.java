package com.notebook.api.plans.infrastructure.web;

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
import com.notebook.api.plans.application.PlanService;
import com.notebook.api.plans.application.PlanSummary;
import com.notebook.api.plans.domain.PlanStatus;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/plans")
class PlanController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final PlanService plans;

	PlanController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions, PlanService plans) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.plans = plans;
	}

	@GetMapping
	List<PlanSummary> list(Authentication authentication) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.plans.list(activeWorkspaceId(accountId));
	}

	@PostMapping
	PlanSummary create(Authentication authentication, @Valid @RequestBody UpsertPlanRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.plans.create(accountId, activeWorkspaceId(accountId), request.title(), request.goal(),
				request.items(), request.linkedResources());
	}

	@PatchMapping("/{planId}")
	PlanSummary update(Authentication authentication, @PathVariable UUID planId,
			@Valid @RequestBody UpsertPlanRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.plans.update(planId, activeWorkspaceId(accountId), request.title(), request.goal(),
				request.items(), request.linkedResources());
	}

	@PatchMapping("/{planId}/status")
	PlanSummary updateStatus(Authentication authentication, @PathVariable UUID planId,
			@Valid @RequestBody UpdatePlanStatusRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.plans.updateStatus(planId, activeWorkspaceId(accountId), request.status());
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	record UpsertPlanRequest(
			@NotBlank(message = "Title is required.")
			@Size(max = 180, message = "Title must be 180 characters or fewer.")
			String title,
			@Size(max = 20000, message = "Goal must be 20000 characters or fewer.")
			String goal,
			@Size(max = 20000, message = "Items must be 20000 characters or fewer.")
			String items,
			@Size(max = 1024, message = "Linked resources must be 1024 characters or fewer.")
			String linkedResources
	) {
	}

	record UpdatePlanStatusRequest(PlanStatus status) {
		UpdatePlanStatusRequest {
			if (status == null) {
				status = PlanStatus.ACTIVE;
			}
		}
	}
}

