package com.softclouds.kapture.kwatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(value = "com.softclouds.kapture.kwatcher.repository")
public class KwatcherApplication  {
	
	public static void main(String[] args) {
		SpringApplication.run(KwatcherApplication.class, args);
	}
}
