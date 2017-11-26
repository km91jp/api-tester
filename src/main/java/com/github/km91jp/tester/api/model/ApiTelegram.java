package com.github.km91jp.tester.api.model;

import java.util.List;

import io.swagger.models.Scheme;

public class ApiTelegram {

	private String apiName;

	private String method;

	private List<Scheme> schemes;

	private String host;

	private String basePath;

	private String requestUri;

	private List<ApiRequestParameter> requests;

	private List<String> consumes;

	private List<String> produces;

	ApiTelegram(String apiName, String method, List<Scheme> schemes, String host, String basePath, String requestUri,
			List<ApiRequestParameter> requests, List<String> consumes, List<String> produces) {
		this.apiName = apiName;
		this.method = method;
		this.schemes = schemes;
		this.host = host;
		this.basePath = basePath;
		this.requestUri = requestUri;
		this.requests = requests;
		this.consumes = consumes;
		this.produces = produces;
	}

	public String getApiName() {
		return apiName;
	}

	public String getMethod() {
		return method;
	}

	public String getHost() {
		return host;
	}

	public String getBasePath() {
		return basePath;
	}

	public String getRequestUri() {
		return requestUri;
	}

	public List<ApiRequestParameter> getRequests() {
		return requests;
	}

	public List<Scheme> getSchemes() {
		return schemes;
	}

	public List<String> getConsumes() {
		return consumes;
	}

	public List<String> getProduces() {
		return produces;
	}

}