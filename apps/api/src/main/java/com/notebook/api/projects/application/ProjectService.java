package com.notebook.api.projects.application;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.notebook.api.notes.application.NoteService;
import com.notebook.api.notes.application.NoteService.NoteFilters;
import com.notebook.api.notes.application.NoteSummary;
import com.notebook.api.projects.domain.ProjectRecord;
import com.notebook.api.projects.domain.ProjectStatus;
import com.notebook.api.projects.infrastructure.persistence.ProjectRepository;

@Service
public class ProjectService {

	private final ProjectRepository projects;
	private final NoteService notes;

	public ProjectService(ProjectRepository projects, NoteService notes) {
		this.projects = projects;
		this.notes = notes;
	}

	@Transactional(readOnly = true)
	public List<ProjectSummary> list(UUID workspaceContextId) {
		return this.projects.findByWorkspaceContextIdOrderByUpdatedAtDesc(workspaceContextId).stream()
				.map(ProjectSummary::from)
				.toList();
	}

	@Transactional
	public ProjectSummary create(UUID ownerAccountId, UUID workspaceContextId, String name, String description) {
		return ProjectSummary.from(this.projects.save(ProjectRecord.create(ownerAccountId, workspaceContextId, name,
				description, Instant.now())));
	}

	@Transactional
	public ProjectSummary update(UUID projectId, UUID workspaceContextId, String name, String description,
			ProjectStatus status) {
		ProjectRecord project = findWorkspaceProject(projectId, workspaceContextId);
		project.update(name, description, status, Instant.now());
		return ProjectSummary.from(project);
	}

	@Transactional(readOnly = true)
	public ProjectHistorySummary summarize(UUID projectId, UUID workspaceContextId) {
		ProjectSummary project = ProjectSummary.from(findWorkspaceProject(projectId, workspaceContextId));
		String needle = project.name().toLowerCase(Locale.ROOT);
		List<NoteSummary> sources = this.notes.list(workspaceContextId,
				new NoteFilters(null, null, null, null, null, false)).stream()
				.filter(note -> note.linkedResources().toLowerCase(Locale.ROOT).contains(project.id().toString())
						|| note.linkedResources().toLowerCase(Locale.ROOT).contains(needle)
						|| note.title().toLowerCase(Locale.ROOT).contains(needle))
				.toList();
		List<String> timeline = sources.stream()
				.map(note -> "%s: %s".formatted(note.noteDate(), note.title()))
				.toList();
		List<String> nextActions = sources.isEmpty()
				? List.of("Add or link project notes to build a project history.")
				: List.of("Review linked notes and turn unresolved items into reminders or plan tasks.");
		return new ProjectHistorySummary(
				project,
				timeline,
				List.of("No explicit decisions detected in linked notes yet."),
				List.of("Review project notes for open questions."),
				nextActions,
				sources);
	}

	private ProjectRecord findWorkspaceProject(UUID projectId, UUID workspaceContextId) {
		return this.projects.findByIdAndWorkspaceContextId(projectId, workspaceContextId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
	}
}
