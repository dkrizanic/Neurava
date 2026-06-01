package com.notebook.api.notes.infrastructure.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.notes.application.NoteService;
import com.notebook.api.notes.application.NoteService.NoteFilters;
import com.notebook.api.notes.application.NoteSummary;
import com.notebook.api.notes.domain.EditorMode;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/notes")
class NoteController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final NoteService notes;

	NoteController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions, NoteService notes) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.notes = notes;
	}

	@GetMapping
	List<NoteSummary> list(Authentication authentication,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String tag,
			@RequestParam(required = false) Boolean favorite,
			@RequestParam(required = false) Boolean pinned,
			@RequestParam(required = false) LocalDate date,
			@RequestParam(defaultValue = "false") boolean archived) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		UUID workspaceId = activeWorkspaceId(accountId);
		return this.notes.list(workspaceId, new NoteFilters(q, tag, favorite, pinned, date, archived));
	}

	@PostMapping
	NoteSummary create(Authentication authentication, @Valid @RequestBody CreateNoteRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		UUID workspaceId = activeWorkspaceId(accountId);
		return this.notes.create(accountId, workspaceId, request.title(), request.body(), request.noteDate());
	}

	@PatchMapping("/{noteId}/organization")
	NoteSummary organize(Authentication authentication, @PathVariable UUID noteId,
			@Valid @RequestBody OrganizeNoteRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		UUID workspaceId = activeWorkspaceId(accountId);
		return this.notes.organize(noteId, workspaceId, request.tags(), request.favorite(), request.pinned(),
				request.editorMode(), request.linkedResources());
	}

	@PatchMapping("/{noteId}/archive")
	NoteSummary archive(Authentication authentication, @PathVariable UUID noteId) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.notes.archive(noteId, activeWorkspaceId(accountId));
	}

	@PatchMapping("/{noteId}/restore")
	NoteSummary restore(Authentication authentication, @PathVariable UUID noteId) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.notes.restore(noteId, activeWorkspaceId(accountId));
	}

	@PatchMapping("/{noteId}")
	NoteSummary update(Authentication authentication, @PathVariable UUID noteId,
			@Valid @RequestBody UpdateNoteRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		UUID workspaceId = activeWorkspaceId(accountId);
		return this.notes.update(noteId, workspaceId, request.title(), request.body());
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	record CreateNoteRequest(
			@NotBlank(message = "Title is required.")
			@Size(max = 180, message = "Title must be 180 characters or fewer.")
			String title,
			@Size(max = 20000, message = "Body must be 20000 characters or fewer.")
			String body,
			LocalDate noteDate
	) {
		CreateNoteRequest {
			if (noteDate == null) {
				noteDate = LocalDate.now();
			}
		}
	}

	record UpdateNoteRequest(
			@NotBlank(message = "Title is required.")
			@Size(max = 180, message = "Title must be 180 characters or fewer.")
			String title,
			@Size(max = 20000, message = "Body must be 20000 characters or fewer.")
			String body
	) {
	}

	record OrganizeNoteRequest(
			@Size(max = 512, message = "Tags must be 512 characters or fewer.")
			String tags,
			boolean favorite,
			boolean pinned,
			EditorMode editorMode,
			@Size(max = 1024, message = "Linked resources must be 1024 characters or fewer.")
			String linkedResources
	) {
		OrganizeNoteRequest {
			if (editorMode == null) {
				editorMode = EditorMode.RICH_TEXT;
			}
		}
	}
}
