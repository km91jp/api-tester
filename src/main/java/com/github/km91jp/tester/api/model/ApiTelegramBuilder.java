package com.github.km91jp.tester.api.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.Scheme;

public class ApiTelegramBuilder {

	private String apiName;

	private String method;

	private List<Scheme> schemes;

	private String host;

	private List<String> consumes;

	private List<String> produces;

	private String basePath;

	private String requestUri;

	private List<ApiRequestParameter> requests;

	public ApiTelegramBuilder(String apiName, String method, List<Scheme> schemes, String host, String basePath,
			String requestUri) {
		this.apiName = apiName;
		this.method = method;
		this.schemes = schemes;
		this.host = host;
		this.basePath = basePath;
		this.requestUri = requestUri;
		this.requests = new ArrayList<>();
		this.consumes = new ArrayList<>();
		this.produces = new ArrayList<>();
	}

	public ApiTelegramBuilder addParameter(String in, String type, String name, String description,
			List<String> values) {
		ApiRequestParameter reqParam = new ApiRequestParameter(in, type, name, description);
		if (!values.isEmpty()) {
			values.forEach((e) -> reqParam.addValue(e));
		}
		requests.add(reqParam);
		return this;
	}

	public ApiTelegramBuilder addConsume(String consume) {
		consumes.add(consume);
		return this;
	}

	public ApiTelegramBuilder addProduce(String produce) {
		produces.add(produce);
		return this;
	}

	public ApiTelegram build() {
		return new ApiTelegram(apiName, method, schemes, host, basePath, requestUri, requests, consumes, produces);
	}

}
