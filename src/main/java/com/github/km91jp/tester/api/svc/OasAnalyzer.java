package com.github.km91jp.tester.api.svc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.github.km91jp.tester.api.model.ApiRequestProperty;
import com.github.km91jp.tester.api.model.ApiTelegram;
import com.github.km91jp.tester.api.model.ApiTelegramBuilder;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

@Service
public class OasAnalyzer {

	public Map<String, ApiTelegram> analyze(Swagger oas) {

		Map<String, ApiTelegram> apis = new LinkedHashMap<>();

		if (oas == null) {
			return apis;
		}

		oas.getPaths().forEach((path, info) -> {
			info.getOperationMap().forEach((method, operation) -> {
				ApiTelegramBuilder builder = new ApiTelegramBuilder(operation.getSummary(), method.toString(),
						oas.getSchemes(), oas.getHost(), oas.getBasePath(), path);
				if (operation.getConsumes() != null) {
					operation.getConsumes().forEach((e) -> builder.addConsume(e));
				}
				if (operation.getProduces() != null) {
					operation.getProduces().forEach((e) -> builder.addProduce(e));
				}
				operation.getParameters().forEach((p) -> {
					getApiRequestPropertiesFromParameter(oas, p).forEach((name, apiReqProp) -> {
						builder.addParameter(p.getIn(), apiReqProp.getType(), apiReqProp.getName(),
								apiReqProp.getDescription(), apiReqProp.getCandidates());
					});
				});
				apis.put(method.toString().concat(":").concat(operation.getSummary()), builder.build());
			});
		});

		return apis;

	}

	private Map<String, ApiRequestProperty> getApiRequestPropertiesFromParameter(Swagger oas, Parameter p) {

		if (p instanceof RefParameter) {
			return getApiRequestPropertiesFromParameter(oas,
					oas.getParameters().get(((RefParameter) p).getSimpleRef()));
		}

		if (p instanceof BodyParameter) {
			BodyParameter bp = (BodyParameter) p;
			return getApiRequestPropertiesFromBodyParameter(oas, bp);
		}

		return getApiRequestPropertiesFromSerializableParameter(oas, (SerializableParameter) p);

	}

	private Map<String, ApiRequestProperty> getApiRequestPropertiesFromSerializableParameter(Swagger oas,
			SerializableParameter sp) {

		Map<String, ApiRequestProperty> retMap = new HashMap<>();

		if (isArray(sp.getType())) {
			retMap.putAll(getApiRequestPropertiesFromProperty(oas, sp.getName(), sp.getType(), sp.getItems()));
		} else {
			ApiRequestProperty apiProp = new ApiRequestProperty();
			apiProp.setName(sp.getName());
			apiProp.setDescription(sp.getDescription());
			apiProp.setType(sp.getType());
			if (sp.getEnum() != null) {
				apiProp.setCandidates(sp.getEnum());
			}
			retMap.put(apiProp.getName(), apiProp);
		}

		return retMap;
	}

	private boolean isArray(String type) {
		return type.equalsIgnoreCase("array");
	}

	private Map<String, ApiRequestProperty> getApiRequestPropertiesFromBodyParameter(Swagger oas, BodyParameter bp) {
		Map<String, ApiRequestProperty> retMap = new LinkedHashMap<>();
		Model bm = bp.getSchema();
		if (bm instanceof RefModel) {
			String refKey = ((RefModel) bm).getSimpleRef();
			bm = oas.getDefinitions().get(refKey);
		}
		if (bm instanceof ArrayModel) {
			Property itemProp = ((ArrayModel) bm).getItems();
			retMap.putAll(
					getApiRequestPropertiesFromProperty(oas, bp.getName(), ((ArrayModel) bm).getType(), itemProp));
		} else {
			bm.getProperties().forEach((name, prop) -> {
				retMap.putAll(getApiRequestPropertiesFromProperty(oas, name, prop.getType(), prop));
			});
		}
		return retMap;
	}

	private Map<String, ApiRequestProperty> getApiRequestPropertiesFromProperty(Swagger oas, String name, String type,
			Property prop) {
		Map<String, ApiRequestProperty> retMap = new LinkedHashMap<>();
		if (prop instanceof RefProperty) {
			Model m = oas.getDefinitions().get(((RefProperty) prop).getSimpleRef());
			if (m instanceof RefModel) {
				String refKey = ((RefModel) m).getSimpleRef();
				m = oas.getDefinitions().get(refKey);
			}
			if (m instanceof ArrayModel) {
				Property itemProp = ((ArrayModel) m).getItems();
				retMap.putAll(
						getApiRequestPropertiesFromProperty(oas, prop.getName(), ((ArrayModel) m).getType(), itemProp));
			} else {
				final String parentType = ((ModelImpl) m).getType();
				m.getProperties().forEach((mn, mp) -> {
					retMap.putAll(getApiRequestPropertiesFromProperty(oas, name + "::" + mn, isArray(type)
							? type + "::" + parentType + "::" + mp.getType() : parentType + "::" + mp.getType(), mp));
				});
			}
		} else if (prop instanceof ArrayProperty) {
			Property itemProp = ((ArrayProperty) prop).getItems();
			retMap.putAll(getApiRequestPropertiesFromProperty(oas, name, prop.getType(), itemProp));
		} else if (prop instanceof ObjectProperty) {
			((ObjectProperty) prop).getProperties().forEach((oName, op) -> {
				retMap.putAll(getApiRequestPropertiesFromProperty(oas, oName, op.getType(), op));
			});
		} else {
			ApiRequestProperty apiProp = new ApiRequestProperty();
			apiProp.setName(name);
			if (isArray(type)) {
				apiProp.setType(type + "::" + prop.getType());
			} else {
				apiProp.setType(type);
			}
			if (prop instanceof StringProperty) {
				StringProperty strProp = (StringProperty) prop;
				if (strProp.getEnum() != null) {
					apiProp.setCandidates(strProp.getEnum());
				}
			}
			apiProp.setDescription(prop.getDescription());
			retMap.put(apiProp.getName(), apiProp);
		}
		return retMap;
	}

}
