package com.notebook.api.notes.functional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class NoteApiFunctionalTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void signedInUserCreatesAndListsWorkspaceNotes() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"First note\",\"body\":\"A useful memory\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "notes-user-subject");
							attributes.put("email", "notes@example.com");
							attributes.put("name", "Notes User");
						})))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.title").value("First note"))
				.andExpect(jsonPath("$.body").value("A useful memory"))
				.andExpect(jsonPath("$.ownerAccountId").exists())
				.andExpect(jsonPath("$.workspaceContextId").exists())
				.andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.updatedAt").exists());

		this.mockMvc.perform(get(ApiPaths.API_V1 + "/notes")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "notes-user-subject");
							attributes.put("email", "notes@example.com");
							attributes.put("name", "Notes User");
						})))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("First note"));
	}

	@Test
	void noteCreationValidatesTitle() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"\",\"body\":\"Body\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "invalid-note-user");
							attributes.put("email", "invalid-note@example.com");
						})))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors[0].field").value("title"));
	}

	@Test
	void signedInUserUpdatesWorkspaceNote() throws Exception {
		String response = this.mockMvc.perform(post(ApiPaths.API_V1 + "/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Draft\",\"body\":\"Old\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "edit-note-user");
							attributes.put("email", "edit-note@example.com");
						})))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String noteId = response.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

		this.mockMvc.perform(patch(ApiPaths.API_V1 + "/notes/" + noteId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Edited\",\"body\":\"New body\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "edit-note-user");
							attributes.put("email", "edit-note@example.com");
						})))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(noteId))
				.andExpect(jsonPath("$.title").value("Edited"))
				.andExpect(jsonPath("$.body").value("New body"));
	}

	@Test
	void noteUpdateValidatesTitle() throws Exception {
		String response = this.mockMvc.perform(post(ApiPaths.API_V1 + "/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Valid\",\"body\":\"Body\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "invalid-edit-note-user");
							attributes.put("email", "invalid-edit-note@example.com");
						})))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String noteId = response.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

		this.mockMvc.perform(patch(ApiPaths.API_V1 + "/notes/" + noteId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"\",\"body\":\"Body\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "invalid-edit-note-user");
							attributes.put("email", "invalid-edit-note@example.com");
						})))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[0].field").value("title"));
	}
}
