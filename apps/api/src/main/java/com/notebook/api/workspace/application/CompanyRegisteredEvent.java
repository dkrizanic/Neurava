package com.notebook.api.workspace.application;

import java.util.UUID;

public record CompanyRegisteredEvent(
		UUID companyId,
		UUID ownerAccountId,
		UUID businessWorkspaceId,
		String companyName
) {
}
