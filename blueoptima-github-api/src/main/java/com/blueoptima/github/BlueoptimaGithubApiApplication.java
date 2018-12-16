package com.blueoptima.github;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BlueoptimaGithubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlueoptimaGithubApiApplication.class, args);
	}

}
