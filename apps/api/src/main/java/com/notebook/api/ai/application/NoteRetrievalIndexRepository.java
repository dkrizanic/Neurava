package com.notebook.api.ai.application;

public interface NoteRetrievalIndexRepository {

	void upsert(NoteRetrievalIndexRecord record);
}
