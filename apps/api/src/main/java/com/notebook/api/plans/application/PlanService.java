package com.notebook.api.plans.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.notebook.api.plans.domain.PlanRecord;
import com.notebook.api.plans.domain.PlanStatus;
import com.notebook.api.plans.infrastructure.persistence.PlanRepository;

@Service
public class PlanService {

	private final PlanRepository plans;

	public PlanService(PlanRepository plans) {
		this.plans = plans;
	}

	@Transactional(readOnly = true)
	public List<PlanSummary> list(UUID workspaceContextId) {
		return this.plans.findByWorkspaceContextIdOrderByUpdatedAtDesc(workspaceContextId).stream()
				.map(PlanSummary::from)
				.toList();
	}

	@Transactional
	public PlanSummary create(UUID ownerAccountId, UUID workspaceContextId, String title, String goal, String items,
			String linkedResources) {
		return PlanSummary.from(this.plans.save(PlanRecord.create(ownerAccountId, workspaceContextId, title, goal, items,
				linkedResources, Instant.now())));
	}

	@Transactional
	public PlanSummary update(UUID planId, UUID workspaceContextId, String title, String goal, String items,
			String linkedResources) {
		PlanRecord plan = findWorkspacePlan(planId, workspaceContextId);
		plan.update(title, goal, items, linkedResources, Instant.now());
		return PlanSummary.from(plan);
	}

	@Transactional
	public PlanSummary updateStatus(UUID planId, UUID workspaceContextId, PlanStatus status) {
		PlanRecord plan = findWorkspacePlan(planId, workspaceContextId);
		plan.setStatus(status, Instant.now());
		return PlanSummary.from(plan);
	}

	@Transactional
	public void delete(UUID planId, UUID workspaceContextId) {
		this.plans.delete(findWorkspacePlan(planId, workspaceContextId));
	}

	private PlanRecord findWorkspacePlan(UUID planId, UUID workspaceContextId) {
		return this.plans.findByIdAndWorkspaceContextId(planId, workspaceContextId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found."));
	}
}

