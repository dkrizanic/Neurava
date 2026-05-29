package com.notebook.api.ai.application;

import java.util.Arrays;

public record EmbeddingVector(double[] values) {

	public String toPgVectorLiteral() {
		return Arrays.stream(this.values)
				.mapToObj(value -> String.format(java.util.Locale.ROOT, "%.8f", value))
				.collect(java.util.stream.Collectors.joining(",", "[", "]"));
	}
}
