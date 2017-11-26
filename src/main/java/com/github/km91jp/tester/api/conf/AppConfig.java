package com.github.km91jp.tester.api.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.km91jp.tester.api.ctrl.ApiTester;
import com.github.km91jp.tester.api.svc.ApiRequestSender;
import com.github.km91jp.tester.api.svc.OasAnalyzer;
import com.github.km91jp.tester.api.svc.OasFileSearchService;

@SpringBootApplication
@PropertySource("classpath:config.properties")
public class AppConfig {

	@Value("${oasFilesDir}")
	private String oasFilesDir;

	@Bean
	public OasFileSearchService getOasFileSearchService() {
		return new OasFileSearchService(oasFilesDir);
	}

	@Bean
	public ApiTester apiTester() {
		return new ApiTester();
	}

	@Bean
	public OasAnalyzer oasAnalyzer() {
		return new OasAnalyzer();
	}

	@Bean
	public ObjectMapper mapper() {
		return new ObjectMapper();
	}

	@Bean
	public ApiRequestSender apiRequestSender() {
		return new ApiRequestSender();
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(AppConfig.class, args);
	}

}
