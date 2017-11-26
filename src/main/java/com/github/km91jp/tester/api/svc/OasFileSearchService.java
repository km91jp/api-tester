package com.github.km91jp.tester.api.svc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

public class OasFileSearchService {

	private static SwaggerParser oasParser = new SwaggerParser();

	private String oasFilesDir;

	public OasFileSearchService(String oasFilesDir) {
		this.oasFilesDir = oasFilesDir;
	}

	public List<String> findAll() throws IOException {
		List<String> fileList = new ArrayList<>();
		Files.newDirectoryStream(Paths.get(oasFilesDir)).forEach((path) -> {
			fileList.add(path.getFileName().toString());
		});
		return fileList;
	}

	public Swagger getOas(String fileName) {
		Swagger targetOas = null;
		try {
			targetOas = oasParser.parse(Files.readAllLines(Paths.get(oasFilesDir, fileName)).stream().reduce("",
					(prevLine, nextLine) -> prevLine.concat(nextLine).concat(System.lineSeparator())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return targetOas;
	}

}
