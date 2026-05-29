package com.notebook.api.workspace.infrastructure.web;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.shared.infrastructure.web.ApiPaths;
import com.notebook.api.workspace.application.CompanyRegistration;
import com.notebook.api.workspace.application.CompanyRegistrationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/workspaces/companies")
class CompanyRegistrationController {

	private final AuthAccountService accounts;
	private final CompanyRegistrationService registrations;

	CompanyRegistrationController(AuthAccountService accounts, CompanyRegistrationService registrations) {
		this.accounts = accounts;
		this.registrations = registrations;
	}

	@PostMapping
	CompanyRegistration registerCompany(Authentication authentication, @Valid @RequestBody RegisterCompanyRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.registrations.register(accountId, request.name());
	}

	record RegisterCompanyRequest(
			@NotBlank(message = "Company name is required.")
			@Size(max = 160, message = "Company name must be 160 characters or fewer.")
			String name
	) {
	}
}
