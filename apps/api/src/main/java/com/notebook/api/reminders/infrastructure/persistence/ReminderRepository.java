package com.notebook.api.reminders.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.reminders.domain.Reminder;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

	List<Reminder> findByWorkspaceContextIdOrderByDueAtAsc(UUID workspaceContextId);

	Optional<Reminder> findByIdAndWorkspaceContextId(UUID id, UUID workspaceContextId);
}

