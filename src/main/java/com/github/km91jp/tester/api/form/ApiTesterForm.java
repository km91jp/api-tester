package com.github.km91jp.tester.api.form;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.github.km91jp.tester.api.model.ApiTelegram;

public class ApiTesterForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private String selectedFileName;

	private String selectedApi;

	private Map<String, ApiTelegram> readMap;

	private Map<String, String> alreadyInputs = new HashMap<>();

	public String getSelectedFileName() {
		return selectedFileName;
	}

	public void setSelectedFileName(String selectedFileName) {
		this.selectedFileName = selectedFileName;
	}

	public String getSelectedApi() {
		return selectedApi;
	}

	public void setSelectedApi(String selectedApi) {
		this.selectedApi = selectedApi;
	}

	public Map<String, ApiTelegram> getReadMap() {
		return readMap;
	}

	public void setReadMap(Map<String, ApiTelegram> readMap) {
		this.readMap = readMap;
	}

	public Map<String, String> getAlreadyInputs() {
		return alreadyInputs;
	}

	public void clearAlreadyInputs(Map<String, String> alreadyInputs) {
		this.alreadyInputs = new HashMap<>();
	}

	public void addAlreadyInputs(String key, String value) {
		if (this.alreadyInputs.containsKey(key)) {
			this.alreadyInputs.remove(key);
		}
		this.alreadyInputs.put(key, value);
	}

}
