package com.github.km91jp.tester.api.model;

import java.util.ArrayList;
import java.util.List;

public class ApiRequestParameter {

	private String in;

	private String type;

	private String name;

	private String description;

	private List<String> values;

	public ApiRequestParameter(String in, String type, String name, String description) {
		this.in = in;
		this.type = type;
		this.name = name;
		this.description = description;
		this.values = new ArrayList<>();
	}

	public String getType() {
		return type;
	}

	public void addValue(String value) {
		this.values.add(value);
	}

	public String getName() {
		return name;
	}

	public List<String> getValues() {
		return values;
	}

	public String getIn() {
		return in;
	}

	public String getDescription() {
		return description;
	}

}
