package com.notebook.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NotebookApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotebookApiApplication.class, args);
	}

}
