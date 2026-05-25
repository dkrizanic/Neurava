package com.notebook.api.shared.infrastructure.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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

@SpringBootTest
@AutoConfigureMockMvc
class SystemApiContractTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthEndpointIsPublicAndHealthy() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void versionEndpointReturnsStableApiMetadata() throws Exception {
		mockMvc.perform(get(ApiPaths.API_V1 + "/system/version"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name").value("Neurava API"))
				.andExpect(jsonPath("$.version").value("0.0.1-SNAPSHOT"))
				.andExpect(jsonPath("$.profiles", hasItem("local")))
				.andExpect(jsonPath("$.serverTime").exists());
	}

	@Test
	void validationErrorsUseProblemDetailsWithFieldDetails() throws Exception {
		mockMvc.perform(post(ApiPaths.API_V1 + "/system/validation-check")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"label\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.detail").value("Request validation failed."))
				.andExpect(jsonPath("$.instance").value(ApiPaths.API_V1 + "/system/validation-check"))
				.andExpect(jsonPath("$.errors[0].field").value("label"))
				.andExpect(jsonPath("$.errors[0].message").exists())
				.andExpect(content().string(not(containsString("Exception"))));
	}

	@Test
	void missingApiRoutesUseSanitizedProblemDetails() throws Exception {
		mockMvc.perform(get(ApiPaths.API_V1 + "/system/missing"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Resource not found"))
				.andExpect(jsonPath("$.detail").value("The requested API resource was not found."))
				.andExpect(jsonPath("$.instance").value(ApiPaths.API_V1 + "/system/missing"))
				.andExpect(content().string(not(containsString("trace"))))
				.andExpect(content().string(not(containsString("stackTrace"))))
				.andExpect(content().string(not(containsString("OPENAI_API_KEY"))));
	}
}
