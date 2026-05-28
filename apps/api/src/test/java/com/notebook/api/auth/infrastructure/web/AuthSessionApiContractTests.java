package com.notebook.api.auth.infrastructure.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AuthSessionApiContractTests {

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
}
