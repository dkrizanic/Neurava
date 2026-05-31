package com.notebook.api.ai.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

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
						.with(user("retrieval-index-user", "retrieval-index@example.com")))
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

	@Test
	void weakFragmentSearchReturnsWorkspaceScopedNoteMatches() throws Exception {
		createNote("semantic-search-user", "semantic-search@example.com",
				"Planning memory", "We talked about the API problem and the migration risk.");

		this.mockMvc.perform(get(ApiPaths.API_V1 + "/ai/search").param("q", "api problem")
						.with(user("semantic-search-user", "semantic-search@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].sourceType").value("note"))
				.andExpect(jsonPath("$[0].sourceId").exists())
				.andExpect(jsonPath("$[0].title").value("Planning memory"))
				.andExpect(jsonPath("$[0].snippet").value(org.hamcrest.Matchers.containsString("API problem")))
				.andExpect(jsonPath("$[0].sourceUpdatedAt").exists())
				.andExpect(jsonPath("$[0].score").isNumber());
	}

	@Test
	void weakFragmentSearchRejectsBlankQueriesWithProblemDetails() throws Exception {
		this.mockMvc.perform(get(ApiPaths.API_V1 + "/ai/search").param("q", "   ")
						.with(user("blank-search-user", "blank-search@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors[0].field").value("search.query"));
	}

	@Test
	void weakFragmentSearchReturnsEmptyListWhenNoIndexedNoteMatches() throws Exception {
		createNote("no-result-search-user", "no-result-search@example.com",
				"Garden notes", "Tomatoes need watering after lunch.");

		this.mockMvc.perform(get(ApiPaths.API_V1 + "/ai/search").param("q", "quarterly budget")
						.with(user("no-result-search-user", "no-result-search@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void weakFragmentSearchDoesNotReturnOtherWorkspaceMatches() throws Exception {
		createNote("other-workspace-search-user", "other-workspace-search@example.com",
				"Private memory", "The API problem belongs to a different workspace.");
		createNote("isolated-search-user", "isolated-search@example.com",
				"Daily note", "Lunch and errands.");

		this.mockMvc.perform(get(ApiPaths.API_V1 + "/ai/search").param("q", "api problem")
						.with(user("isolated-search-user", "isolated-search@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void sourceAwareAnswerIncludesNoteSources() throws Exception {
		createNote("source-answer-user", "source-answer@example.com",
				"API decision", "We decided the API problem should be solved with stable problem details.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/answers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"question\":\"What did we decide about the API problem?\"}")
						.with(user("source-answer-user", "source-answer@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.enoughSourceContext").value(true))
				.andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("available notebook sources")))
				.andExpect(jsonPath("$.sources[0].type").value("note"))
				.andExpect(jsonPath("$.sources[0].title").value("API decision"))
				.andExpect(jsonPath("$.sources[0].snippet").value(org.hamcrest.Matchers.containsString("API problem")))
				.andExpect(jsonPath("$.sources[0].sourceUpdatedAt").exists());
	}

	@Test
	void sourceAwareAnswerRejectsBlankQuestionsWithProblemDetails() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/answers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"question\":\"   \"}")
						.with(user("blank-answer-user", "blank-answer@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors[0].field").value("question"));
	}

	@Test
	void sourceAwareAnswerIndicatesInsufficientContextWhenNoSourcesMatch() throws Exception {
		createNote("insufficient-answer-user", "insufficient-answer@example.com",
				"Garden notes", "Tomatoes need watering after lunch.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/answers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"question\":\"What did we decide about quarterly budget?\"}")
						.with(user("insufficient-answer-user", "insufficient-answer@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enoughSourceContext").value(false))
				.andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("not have enough source context")))
				.andExpect(jsonPath("$.sources").isEmpty());
	}

	@Test
	void sourceAwareAnswerDoesNotUseOtherWorkspaceSources() throws Exception {
		createNote("other-source-answer-user", "other-source-answer@example.com",
				"Other workspace answer", "The API problem was fixed in another workspace.");
		createNote("isolated-answer-user", "isolated-answer@example.com",
				"Personal errand", "Buy tea after lunch.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/answers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"question\":\"What happened with the API problem?\"}")
						.with(user("isolated-answer-user", "isolated-answer@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enoughSourceContext").value(false))
				.andExpect(jsonPath("$.sources").isEmpty());
	}

	@Test
	void historySummaryIncludesStructuredSectionsAndSources() throws Exception {
		createNote("summary-user", "summary@example.com",
				"API planning",
				"We decided to keep problem details stable. The migration risk remains open. Next action is to document the contract.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/summaries")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"topic\":\"Summarize API planning\"}")
						.with(user("summary-user", "summary@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.enoughSourceContext").value(true))
				.andExpect(jsonPath("$.sections.keyEvents[0]").value(org.hamcrest.Matchers.containsString("API planning")))
				.andExpect(jsonPath("$.sections.decisions[0]").value(org.hamcrest.Matchers.containsString("decided")))
				.andExpect(jsonPath("$.sections.unresolvedItems[0]").value(org.hamcrest.Matchers.containsString("risk")))
				.andExpect(jsonPath("$.sections.nextActions[0]").value(org.hamcrest.Matchers.containsString("Next action")))
				.andExpect(jsonPath("$.sources[0].type").value("note"))
				.andExpect(jsonPath("$.sources[0].title").value("API planning"));
	}

	@Test
	void historySummaryRejectsBlankTopicsWithProblemDetails() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/summaries")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"topic\":\"   \"}")
						.with(user("blank-summary-user", "blank-summary@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors[0].field").value("topic"));
	}

	@Test
	void historySummaryIndicatesInsufficientContextWhenNoSourcesMatch() throws Exception {
		createNote("insufficient-summary-user", "insufficient-summary@example.com",
				"Garden notes", "Tomatoes need watering after lunch.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/summaries")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"topic\":\"quarterly budget\"}")
						.with(user("insufficient-summary-user", "insufficient-summary@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enoughSourceContext").value(false))
				.andExpect(jsonPath("$.sections.keyEvents[0]").value(org.hamcrest.Matchers.containsString("Not enough source context")))
				.andExpect(jsonPath("$.sources").isEmpty());
	}

	@Test
	void historySummaryDoesNotUseOtherWorkspaceSources() throws Exception {
		createNote("other-summary-user", "other-summary@example.com",
				"Other API summary", "We decided the API problem in another workspace.");
		createNote("isolated-summary-user", "isolated-summary@example.com",
				"Personal errand", "Buy tea after lunch.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/summaries")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"topic\":\"API problem\"}")
						.with(user("isolated-summary-user", "isolated-summary@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enoughSourceContext").value(false))
				.andExpect(jsonPath("$.sources").isEmpty());
	}

	private void createNote(String subject, String email, String title, String body) throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"%s","body":"%s"}
								""".formatted(title, body))
						.with(user(subject, email)))
				.andExpect(status().isOk());
	}

	private static RequestPostProcessor user(String subject, String email) {
		return oauth2Login().attributes(attributes -> {
			attributes.put("sub", subject);
			attributes.put("email", email);
		});
	}
}
