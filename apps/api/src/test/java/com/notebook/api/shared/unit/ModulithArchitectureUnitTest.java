package com.notebook.api.shared.unit;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import com.notebook.api.NotebookApiApplication;

class ModulithArchitectureUnitTest {

	@Test
	void applicationModulesRespectDeclaredBoundaries() {
		ApplicationModules.of(NotebookApiApplication.class).verify();
	}
}
