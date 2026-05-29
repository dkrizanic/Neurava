package com.notebook.api.workspace.application;

import java.util.UUID;

import com.notebook.api.workspace.domain.RegisteredCompany;
import com.notebook.api.workspace.domain.WorkspaceContext;

public record CompanyRegistration(
		CompanySummary company,
		WorkspaceSession.WorkspaceSummary businessWorkspace
) {

	public static CompanyRegistration from(RegisteredCompany company) {
		WorkspaceContext workspace = company.getWorkspaceContext();
		return new CompanyRegistration(
				new CompanySummary(company.getId(), company.getName()),
				new WorkspaceSession.WorkspaceSummary(workspace.getId(), workspace.getName(), workspace.getType().name()));
	}

	public record CompanySummary(
			UUID id,
			String name
	) {
	}
}
