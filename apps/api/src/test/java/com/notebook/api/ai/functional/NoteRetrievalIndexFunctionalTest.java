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

	@Test
	void assistantActionRoutesSourceAwareAnswers() throws Exception {
		createNote("action-answer-user", "action-answer@example.com",
				"API action decision", "We decided the assistant action API should stay typed.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/actions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"answer_question","input":{"question":"What did we decide about the assistant action API?"}}
								""")
						.with(user("action-answer-user", "action-answer@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.action").value("answer_question"))
				.andExpect(jsonPath("$.result.enoughSourceContext").value(true))
				.andExpect(jsonPath("$.result.sources[0].title").value("API action decision"));
	}

	@Test
	void assistantActionRoutesHistorySummaries() throws Exception {
		createNote("action-summary-user", "action-summary@example.com",
				"Action summary", "We decided action routing is open until tests pass. Next action is to document it.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/actions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"summarize_history","input":{"topic":"action routing"}}
								""")
						.with(user("action-summary-user", "action-summary@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.action").value("summarize_history"))
				.andExpect(jsonPath("$.result.enoughSourceContext").value(true))
				.andExpect(jsonPath("$.result.sections.keyEvents[0]").value(org.hamcrest.Matchers.containsString("Action summary")))
				.andExpect(jsonPath("$.result.sources[0].title").value("Action summary"));
	}

	@Test
	void assistantActionRoutesWeakFragmentSearch() throws Exception {
		createNote("action-search-user", "action-search@example.com",
				"Action search", "The typed action router can recover weak fragments.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/actions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"search_memory","input":{"query":"typed action router"}}
								""")
						.with(user("action-search-user", "action-search@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.action").value("search_memory"))
				.andExpect(jsonPath("$.result[0].sourceType").value("note"))
				.andExpect(jsonPath("$.result[0].title").value("Action search"));
	}

	@Test
	void assistantActionRejectsUnsupportedActionsWithProblemDetails() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/actions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"delete_notes","input":{"query":"all"}}
								""")
						.with(user("unsupported-action-user", "unsupported-action@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unsupported assistant action"))
				.andExpect(jsonPath("$.detail").value("Assistant action is not supported."))
				.andExpect(jsonPath("$.action").value("delete_notes"));
	}

	@Test
	void assistantActionRejectsInvalidActionInputWithProblemDetails() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/actions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"answer_question","input":{"question":"   "}}
								""")
						.with(user("invalid-action-user", "invalid-action@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors[0].field").value("question"));
	}

	@Test
	void assistantActionDoesNotUseOtherWorkspaceSources() throws Exception {
		createNote("other-action-user", "other-action@example.com",
				"Other action answer", "The typed action router belongs to another workspace.");
		createNote("isolated-action-user", "isolated-action@example.com",
				"Personal note", "Buy tea after lunch.");

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/actions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"answer_question","input":{"question":"What happened with the typed action router?"}}
								""")
						.with(user("isolated-action-user", "isolated-action@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.action").value("answer_question"))
				.andExpect(jsonPath("$.result.enoughSourceContext").value(false))
				.andExpect(jsonPath("$.result.sources").isEmpty());
	}

	@Test
	void assistantActionPreviewReturnsCreateNoteDraftWithoutPersistingNote() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-previews")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"create_note","input":{"text":"API follow-up\\nWe need to document the preview contract and apply flow."}}
								""")
						.with(user("preview-note-user", "preview-note@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.action").value("create_note"))
				.andExpect(jsonPath("$.entityType").value("note"))
				.andExpect(jsonPath("$.changeType").value("create"))
				.andExpect(jsonPath("$.summary").value(org.hamcrest.Matchers.containsString("Create a new note draft")))
				.andExpect(jsonPath("$.preview.title").value("API follow-up"))
				.andExpect(jsonPath("$.preview.body").value(org.hamcrest.Matchers.containsString("preview contract")))
				.andExpect(jsonPath("$.preview.tags").value(org.hamcrest.Matchers.containsString("follow")));

		this.mockMvc.perform(get(ApiPaths.API_V1 + "/notes").param("q", "preview contract")
						.with(user("preview-note-user", "preview-note@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void assistantActionPreviewRejectsBlankCreateNoteInputWithProblemDetails() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-previews")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"create_note","input":{"text":"   "}}
								""")
						.with(user("blank-preview-user", "blank-preview@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors[0].field").value("text"));
	}

	@Test
	void assistantActionPreviewRejectsUnsupportedActionsWithProblemDetails() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-previews")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"delete_notes","input":{"text":"all"}}
								""")
						.with(user("unsupported-preview-user", "unsupported-preview@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unsupported assistant action preview"))
				.andExpect(jsonPath("$.detail").value("Assistant action preview is not supported."))
				.andExpect(jsonPath("$.action").value("delete_notes"));
	}

	@Test
	void assistantActionApplicationCreatesNoteFromApprovedPreview() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"create_note","input":{"title":"Applied AI note","body":"Approved body from preview.","tags":"approved,preview","noteDate":"2026-06-01"}}
								""")
						.with(user("apply-note-user", "apply-note@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.action").value("create_note"))
				.andExpect(jsonPath("$.entityType").value("note"))
				.andExpect(jsonPath("$.changeType").value("create"))
				.andExpect(jsonPath("$.summary").value("Created note \"Applied AI note\"."))
				.andExpect(jsonPath("$.entity.title").value("Applied AI note"))
				.andExpect(jsonPath("$.entity.body").value("Approved body from preview."))
				.andExpect(jsonPath("$.entity.tags").value("approved,preview"))
				.andExpect(jsonPath("$.entity.noteDate").value("2026-06-01"));

		this.mockMvc.perform(get(ApiPaths.API_V1 + "/notes").param("q", "Approved body")
						.with(user("apply-note-user", "apply-note@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Applied AI note"));

		this.mockMvc.perform(get(ApiPaths.API_V1 + "/ai/action-history")
						.with(user("apply-note-user", "apply-note@example.com")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].action").value("create_note"))
				.andExpect(jsonPath("$[0].entityType").value("note"))
				.andExpect(jsonPath("$[0].changeType").value("create"))
				.andExpect(jsonPath("$[0].summary").value("Created note \"Applied AI note\"."))
				.andExpect(jsonPath("$[0].ownerAccountId").exists())
				.andExpect(jsonPath("$[0].workspaceContextId").exists())
				.andExpect(jsonPath("$[0].entityId").exists())
				.andExpect(jsonPath("$[0].previousState").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$[0].currentState").value(org.hamcrest.Matchers.containsString("Applied AI note")))
				.andExpect(jsonPath("$[0].createdAt").exists());
	}

	@Test
	void assistantActionHistoryRevertsAiCreatedNote() throws Exception {
		String marker = "revert-" + UUID.randomUUID();
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"create_note","input":{"title":"Revert me","body":"%s","tags":"revert","noteDate":"2026-06-01"}}
								""".formatted(marker))
						.with(user("revert-note-user", "revert-note@example.com")))
				.andExpect(status().isOk());

		UUID historyId = this.jdbc.queryForObject("""
				SELECT id
				FROM ai_action_history
				WHERE current_state::text LIKE ?
				ORDER BY created_at DESC
				LIMIT 1
				""", UUID.class, "%" + marker + "%");
		UUID noteId = this.jdbc.queryForObject("""
				SELECT entity_id
				FROM ai_action_history
				WHERE id = ?
				""", UUID.class, historyId);

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-history/{historyId}/revert", historyId)
						.with(user("revert-note-user", "revert-note@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.revertedAt").exists())
				.andExpect(jsonPath("$.revertSummary").value("Removed AI-created note."));

		Integer noteCount = this.jdbc.queryForObject("""
				SELECT count(*)
				FROM note
				WHERE id = ?
				""", Integer.class, noteId);
		Integer indexCount = this.jdbc.queryForObject("""
				SELECT count(*)
				FROM note_retrieval_index
				WHERE note_id = ?
				""", Integer.class, noteId);
		assertThat(noteCount).isZero();
		assertThat(indexCount).isZero();
	}

	@Test
	void assistantActionHistoryExplainsWhenRevertedNoteNoLongerExists() throws Exception {
		String marker = "missing-revert-" + UUID.randomUUID();
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"create_note","input":{"title":"Missing revert note","body":"%s","tags":"missing","noteDate":"2026-06-01"}}
								""".formatted(marker))
						.with(user("missing-revert-user", "missing-revert@example.com")))
				.andExpect(status().isOk());

		UUID historyId = this.jdbc.queryForObject("""
				SELECT id
				FROM ai_action_history
				WHERE current_state::text LIKE ?
				ORDER BY created_at DESC
				LIMIT 1
				""", UUID.class, "%" + marker + "%");
		UUID entityId = this.jdbc.queryForObject("""
				SELECT entity_id
				FROM ai_action_history
				WHERE id = ?
				""", UUID.class, historyId);
		this.jdbc.update("DELETE FROM note WHERE id = ?", entityId);

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-history/{historyId}/revert", historyId)
						.with(user("missing-revert-user", "missing-revert@example.com")))
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("AI action cannot be reverted"))
				.andExpect(jsonPath("$.detail").value("The note created by this AI action no longer exists."));
	}

	@Test
	void assistantGrammarFixPreviewsAppliesHistoryAndReverts() throws Exception {
		String created = this.mockMvc.perform(post(ApiPaths.API_V1 + "/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Grammar note","body":"i dont recieve teh update","noteDate":"2026-06-01"}
								""")
						.with(user("grammar-fix-user", "grammar-fix@example.com")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		UUID noteId = UUID.fromString(created.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1"));

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-previews")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"fix_note_grammar","input":{"noteId":"%s","title":"Grammar note","body":"i dont recieve teh update"}}
								""".formatted(noteId))
						.with(user("grammar-fix-user", "grammar-fix@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.action").value("fix_note_grammar"))
				.andExpect(jsonPath("$.changeType").value("update"))
				.andExpect(jsonPath("$.preview.currentBody").value("i dont recieve teh update"))
				.andExpect(jsonPath("$.preview.proposedBody").value("I don't receive the update"));

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"fix_note_grammar","input":{"noteId":"%s","title":"Grammar note","body":"i dont recieve teh update","proposedBody":"I don't receive the update"}}
								""".formatted(noteId))
						.with(user("grammar-fix-user", "grammar-fix@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.action").value("fix_note_grammar"))
				.andExpect(jsonPath("$.changeType").value("update"))
				.andExpect(jsonPath("$.entity.body").value("I don't receive the update"));

		UUID historyId = this.jdbc.queryForObject("""
				SELECT id
				FROM ai_action_history
				WHERE action = 'fix_note_grammar'
				  AND entity_id = ?
				ORDER BY created_at DESC
				LIMIT 1
				""", UUID.class, noteId);
		this.mockMvc.perform(get(ApiPaths.API_V1 + "/ai/action-history")
						.with(user("grammar-fix-user", "grammar-fix@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].action").value("fix_note_grammar"))
				.andExpect(jsonPath("$[0].previousState").value(org.hamcrest.Matchers.containsString("i dont recieve")))
				.andExpect(jsonPath("$[0].currentState").value(org.hamcrest.Matchers.containsString("I don't receive")));

		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-history/{historyId}/revert", historyId)
						.with(user("grammar-fix-user", "grammar-fix@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.revertSummary").value("Restored note text before the grammar fix."));
		String body = this.jdbc.queryForObject("SELECT body FROM note WHERE id = ?", String.class, noteId);
		assertThat(body).isEqualTo("i dont recieve teh update");
	}

	@Test
	void assistantActionApplicationRejectsUnsupportedActionsWithProblemDetails() throws Exception {
		this.mockMvc.perform(post(ApiPaths.API_V1 + "/ai/action-applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"delete_notes","input":{"title":"all"}}
								""")
						.with(user("unsupported-apply-user", "unsupported-apply@example.com")))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unsupported assistant action application"))
				.andExpect(jsonPath("$.detail").value("Assistant action application is not supported."))
				.andExpect(jsonPath("$.action").value("delete_notes"));
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
