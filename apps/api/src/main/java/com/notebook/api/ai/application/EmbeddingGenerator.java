package com.notebook.api.ai.application;

public interface EmbeddingGenerator {

	EmbeddingVector embed(String text);
}
