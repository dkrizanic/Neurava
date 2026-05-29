package com.notebook.api.auth.functional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.notebook.api.shared.infrastructure.web.ApiPaths;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSessionFunctionalTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void anonymousSessionReturnsSignedOutState() throws Exception {
		mockMvc.perform(get(ApiPaths.API_V1 + "/auth/session"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.authenticated").value(false))
				.andExpect(jsonPath("$.account").doesNotExist())
				.andExpect(jsonPath("$.activeWorkspace").doesNotExist())
				.andExpect(jsonPath("$.workspaceSwitcherAvailable").value(false));
	}

	@Test
	void oauthSessionCreatesAccountAndReturnsSafeMetadata() throws Exception {
		mockMvc.perform(get(ApiPaths.API_V1 + "/auth/session")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "google-subject-123");
							attributes.put("email", "dario@example.com");
							attributes.put("name", "Dario Notebook");
							attributes.put("picture", "https://example.com/avatar.png");
						})))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.authenticated").value(true))
				.andExpect(jsonPath("$.account.id").exists())
				.andExpect(jsonPath("$.account.email").value("dario@example.com"))
				.andExpect(jsonPath("$.account.displayName").value("Dario Notebook"))
				.andExpect(jsonPath("$.account.avatarUrl").value("https://example.com/avatar.png"))
				.andExpect(jsonPath("$.activeWorkspace.id").exists())
				.andExpect(jsonPath("$.activeWorkspace.name").value("Personal"))
				.andExpect(jsonPath("$.activeWorkspace.type").value("PERSONAL"))
				.andExpect(jsonPath("$.workspaceSwitcherAvailable").value(false))
				.andExpect(content().string(not(containsString("access_token"))))
				.andExpect(content().string(not(containsString("refresh_token"))))
				.andExpect(content().string(not(containsString("google-subject-123"))));
	}

	@Test
	void repeatedOauthSessionReusesPersonalContext() throws Exception {
		String firstResponse = this.mockMvc.perform(get(ApiPaths.API_V1 + "/auth/session")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "repeat-user-subject");
							attributes.put("email", "repeat@example.com");
							attributes.put("name", "Repeat User");
						})))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String secondResponse = this.mockMvc.perform(get(ApiPaths.API_V1 + "/auth/session")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "repeat-user-subject");
							attributes.put("email", "repeat@example.com");
							attributes.put("name", "Repeat User");
						})))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String firstWorkspaceId = firstResponse.replaceAll(".*\"activeWorkspace\"\\s*:\\s*\\{\\s*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
		String secondWorkspaceId = secondResponse.replaceAll(".*\"activeWorkspace\"\\s*:\\s*\\{\\s*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

		org.assertj.core.api.Assertions.assertThat(secondWorkspaceId).isEqualTo(firstWorkspaceId);
	}

	@Test
	void companyRegistrationMakesWorkspaceSwitcherAvailable() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/workspaces/companies")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Acme Labs\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "company-owner-subject");
							attributes.put("email", "owner@example.com");
							attributes.put("name", "Company Owner");
						})))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.company.id").exists())
				.andExpect(jsonPath("$.company.name").value("Acme Labs"))
				.andExpect(jsonPath("$.businessWorkspace.id").exists())
				.andExpect(jsonPath("$.businessWorkspace.name").value("Acme Labs"))
				.andExpect(jsonPath("$.businessWorkspace.type").value("BUSINESS"));

		this.mockMvc.perform(get(ApiPaths.API_V1 + "/auth/session")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "company-owner-subject");
							attributes.put("email", "owner@example.com");
							attributes.put("name", "Company Owner");
						})))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activeWorkspace.type").value("PERSONAL"))
				.andExpect(jsonPath("$.workspaceSwitcherAvailable").value(true));
	}

	@Test
	void companyRegistrationValidatesCompanyName() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/workspaces/companies")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "invalid-company-subject");
							attributes.put("email", "invalid@example.com");
						})))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors[0].field").value("name"));
	}
}
