package com.github.km91jp.tester.api.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.km91jp.tester.api.form.ApiTesterForm;
import com.github.km91jp.tester.api.model.ApiTesterResponse;
import com.github.km91jp.tester.api.svc.ApiRequestSender;
import com.github.km91jp.tester.api.svc.OasAnalyzer;
import com.github.km91jp.tester.api.svc.OasFileSearchService;

@Controller
@SessionAttributes("apiTesterForm")
public class ApiTester {

	@Inject
	OasFileSearchService oasFileSearchService;

	@Inject
	OasAnalyzer oasAnalyzer;

	@Inject
	ApiRequestSender requestSender;

	@Inject
	ObjectMapper mapper;

	@ModelAttribute
	public ApiTesterForm setUpForm() {
		return new ApiTesterForm();
	}

	@RequestMapping("/")
	String home(ApiTesterForm form, Model model) {
		List<String> oasfiles = new ArrayList<>();
		try {
			oasfiles = oasFileSearchService.findAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
		model.addAttribute("oasfiles", oasfiles);
		return "index";
	}

	@GetMapping("/apis")
	String apis(@RequestParam("fileName") String fileName, ApiTesterForm form, Model model) {
		if (!fileName.equals(form.getSelectedFileName())) {
			form.setSelectedFileName(fileName);
			form.setReadMap(oasAnalyzer.analyze(oasFileSearchService.getOas(fileName)));
		}
		model.addAttribute("selectedFileName", form.getSelectedFileName());
		model.addAttribute("oasData", form.getReadMap());
		return home(form, model);
	}

	@GetMapping("/apis/{apiName}")
	String requests(@PathVariable String apiName, ApiTesterForm form, Model model) {
		form.setSelectedApi(apiName);
		model.addAttribute("selectedApi", apiName);
		model.addAttribute("alreadyInputs", form.getAlreadyInputs());
		return apis(form.getSelectedFileName(), form, model);
	}

	@PostMapping("/send")
	String send(@RequestParam Map<String, String> requestParams, ApiTesterForm form, Model model) {
		requestParams.forEach((key, value) -> {
			form.addAlreadyInputs(key, value);
		});
		ApiTesterResponse response = requestSender.send(form.getReadMap(), requestParams);
		if (requestParams.get("produce").equalsIgnoreCase("application/json")) {
			addAlreadyInputs(form, response.getBody());
		}
		model.addAttribute("result", response);
		return requests(form.getSelectedApi(), form, model);
	}

	private void addAlreadyInputs(ApiTesterForm form, String body) {
		JsonNode jsonNode;
		try {
			jsonNode = mapper.readValue(body, JsonNode.class);
			jsonNode.fields().forEachRemaining((el) -> form.addAlreadyInputs(el.getKey(), el.getValue().asText()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
