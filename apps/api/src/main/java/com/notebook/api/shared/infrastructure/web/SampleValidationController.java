package com.notebook.api.shared.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/system")
class SampleValidationController {

	@PostMapping("/validation-check")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void validateContract(@Valid @RequestBody ValidationCheckRequest request) {
	}

	record ValidationCheckRequest(
			@NotBlank(message = "must not be blank")
			@Size(max = 80, message = "must be at most 80 characters")
			String label) {
	}
}
