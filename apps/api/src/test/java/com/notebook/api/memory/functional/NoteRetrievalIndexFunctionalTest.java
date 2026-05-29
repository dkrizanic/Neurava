package com.notebook.api.memory.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.notebook.api.shared.infrastructure.web.ApiPaths;

@SpringBootTest
@AutoConfigureMockMvc
class NoteRetrievalIndexFunctionalTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void noteCreationWritesWorkspaceScopedPgvectorRetrievalRecord() throws Exception {
		String response = this.mockMvc.perform(post(ApiPaths.API_V1 + "/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Semantic seed\",\"body\":\"Find this by meaning later\"}")
						.with(oauth2Login().attributes(attributes -> {
							attributes.put("sub", "retrieval-index-user");
							attributes.put("email", "retrieval-index@example.com");
						})))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		UUID noteId = UUID.fromString(response.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1"));

		Integer count = this.jdbc.queryForObject("""
				SELECT count(*)
				FROM note_retrieval_index
				WHERE note_id = ?
				  AND workspace_context_id IS NOT NULL
				  AND embedding IS NOT NULL
				  AND status = 'INDEXED'
				  AND searchable_text LIKE '%Semantic seed%'
				""", Integer.class, noteId);

		assertThat(count).isEqualTo(1);
	}
}
