package com.github.km91jp.tester.api.svc;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.km91jp.tester.api.model.ApiRequestParameter;
import com.github.km91jp.tester.api.model.ApiTelegram;
import com.github.km91jp.tester.api.model.ApiTesterResponse;

@Service
public class ApiRequestSender {

	private HttpClient client = new HttpClient();

	@Inject
	ObjectMapper mapper;

	public ApiTesterResponse send(Map<String, ApiTelegram> map, Map<String, String> requestParams) {
		String api = requestParams.get("requestApi");
		ApiTelegram telegram = map.get(api);
		HttpMethod method = createHttpMethod(telegram, requestParams);
		try {
			client.executeMethod(method);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder headerStrBuilder = new StringBuilder(method.getStatusLine().toString())
				.append(System.lineSeparator());
		Arrays.stream(method.getResponseHeaders()).forEach((h) -> {
			headerStrBuilder.append(h.getName() + ":" + h.getValue()).append(System.lineSeparator());
		});

		ApiTesterResponse response = new ApiTesterResponse();
		response.setHeader(headerStrBuilder.toString());
		response.setBody(getBody(method));
		return response;
	}

	private String getBody(HttpMethod method) {
		InputStream responseBodyAsStream = null;
		try {
			responseBodyAsStream = method.getResponseBodyAsStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Header contentTypeHeader = method.getResponseHeader("Content-Type");
		if (contentTypeHeader.getValue().indexOf("application/json") >= 0) {
			return getJsonBody(responseBodyAsStream);
		} else {
			return getXmlBody(responseBodyAsStream);
		}
	}

	private String getXmlBody(InputStream responseBodyAsStream) {
		String xmlStr = null;
		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringWriter sw = new StringWriter();
			Document doc = docBuilder.parse(responseBodyAsStream);
			Transformer xmltrans = TransformerFactory.newInstance().newTransformer();
			xmltrans.setOutputProperty(OutputKeys.METHOD, "xml");
			xmltrans.setOutputProperty(OutputKeys.INDENT, "yes");
			xmltrans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			xmltrans.transform(new DOMSource(doc), new StreamResult(sw));
			sw.flush();
			xmlStr = sw.toString();
			sw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlStr;
	}

	private String getJsonBody(InputStream responseBodyAsStream) {
		String jsonStr = null;
		try {
			Object json = mapper.readValue(responseBodyAsStream, Object.class);
			jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}

	private HttpMethod createHttpMethod(ApiTelegram telegram, Map<String, String> requestParams) {
		HttpMethod method;
		if (telegram.getMethod().equalsIgnoreCase("get")) {
			GetMethod getMethod = new GetMethod(getUrl(telegram, requestParams));
			method = getMethod;
			setQueryString(method, telegram, requestParams);
			method = getMethod;
		} else if (telegram.getMethod().equalsIgnoreCase("post") || telegram.getMethod().equalsIgnoreCase("put")) {
			PostMethod postMethod = new PostMethod(getUrl(telegram, requestParams));
			method = postMethod;
			setQueryString(method, telegram, requestParams);
			String consume = requestParams.get("consume");
			postMethod.setRequestHeader("Content-Type", consume);
			if (consume.equalsIgnoreCase("application/x-www-form-urlencoded")) {
				setFormParam(postMethod, telegram, requestParams);
			} else if (consume.equalsIgnoreCase("application/json")) {
				setJson(postMethod, telegram, requestParams);
			} else if (consume.equalsIgnoreCase("application/xml")) {
				setXml(postMethod, telegram, requestParams);
			} else {
				throw new UnsupportedOperationException();
			}
		} else {
			throw new UnsupportedOperationException();
		}
		method.setRequestHeader("Accept", requestParams.get("produce"));
		return method;
	}

	private void setXml(PostMethod postMethod, ApiTelegram telegram, Map<String, String> requestParams) {
		throw new UnsupportedOperationException();
	}

	private String getUrl(ApiTelegram telegram, Map<String, String> requestParams) {
		return getUrl(requestParams.get("scheme"), telegram.getHost(), telegram.getBasePath(), telegram.getRequestUri(),
				telegram.getRequests(), requestParams);
	}

	@SuppressWarnings("deprecation")
	private void setJson(PostMethod postMethod, ApiTelegram telegram, Map<String, String> requestParams) {
		JsonNode rootNode = mapper.createObjectNode();
		telegram.getRequests().stream().filter((r) -> r.getIn().equalsIgnoreCase("body")).forEach((p) -> {
			if (requestParams.containsKey(p.getName())) {
				if (p.getName().indexOf("::") >= 0) {
					String[] names = p.getName().split("::");
					String objectName = names[0];
					String childName = names[1];
					String[] types = p.getType().split("::", 2);
					String objectType = types[0];
					String childType = types[1];
					JsonNode childNode = rootNode.get(objectName);
					if (childNode == null) {
						if (objectType.equalsIgnoreCase("array")) {
							ArrayNode an = mapper.createArrayNode();
							childNode = mapper.createObjectNode();
							setNode(childNode, childName, getParamValue(p.getName(), childType, requestParams));
							an.add(childNode);
							ObjectNode on = (ObjectNode) rootNode;
							on.set(objectName, an);
						} else {
							childNode = mapper.createObjectNode();
							setNode(childNode, childName, getParamValue(p.getName(), childType, requestParams));
							ObjectNode on = (ObjectNode) rootNode;
							on.set(objectName, childNode);
						}
					} else {
						if (objectType.equalsIgnoreCase("array")) {
							JsonNode node = ((ArrayNode) childNode).get(0);
							setNode(node, childName, getParamValue(p.getName(), childType, requestParams));
						} else {
							setNode(childNode, childName, getParamValue(p.getName(), childType, requestParams));
						}
					}
				} else if (p.getType().indexOf("array") >= 0) {
					ArrayNode an = mapper.createArrayNode();
					String[] types = p.getType().split("::");
					String childType = types[1];
					addNode(an, getParamValue(p.getName(), childType, requestParams));
					ObjectNode on = (ObjectNode) rootNode;
					on.set(p.getName(), an);
				} else {
					ObjectNode on = (ObjectNode) rootNode;
					setNode(on, p.getName(), getParamValue(p.getName(), p.getType(), requestParams));
				}
			}
		});
		try {
			postMethod.setRequestBody(mapper.writeValueAsString(rootNode));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private void addNode(ArrayNode an, Object paramValue) {
		if (paramValue instanceof String) {
			an.add((String) paramValue);
		} else if (paramValue instanceof Long) {
			an.add((Long) paramValue);
		} else if (paramValue instanceof BigDecimal) {
			an.add((BigDecimal) paramValue);
		} else if (paramValue instanceof Boolean) {
			an.add((Boolean) paramValue);
		} else if (paramValue instanceof JsonNode) {
			an.add((JsonNode) paramValue);
		}
	}

	private void setNode(JsonNode childNode, String childName, Object paramValue) {
		if (paramValue instanceof String) {
			((ObjectNode) childNode).put(childName, (String) paramValue);
		} else if (paramValue instanceof Long) {
			((ObjectNode) childNode).put(childName, (Long) paramValue);
		} else if (paramValue instanceof BigDecimal) {
			((ObjectNode) childNode).put(childName, (BigDecimal) paramValue);
		} else if (paramValue instanceof Boolean) {
			((ObjectNode) childNode).put(childName, (Boolean) paramValue);
		}
	}

	private Object getParamValue(String name, String type, Map<String, String> requestParams) {
		Object paramValue;
		String value = requestParams.get(name);
		if (value == null || value.isEmpty()) {
			value = requestParams.get("c-" + name);
		}
		if (type.equalsIgnoreCase("string")) {
			paramValue = value;
		} else if (type.equalsIgnoreCase("boolean")) {
			paramValue = Boolean.valueOf(value);
		} else if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("long")) {
			paramValue = Long.valueOf(value);
		} else if (type.equalsIgnoreCase("number")) {
			paramValue = new BigDecimal(value).toPlainString();
		} else if (type.startsWith("object")) {
			String[] types = type.split("::");
			String childType = types[1];
			paramValue = getParamValue(name, childType, requestParams);
		} else {
			throw new UnsupportedOperationException();
		}
		return paramValue;
	}

	private void setQueryString(HttpMethod method, ApiTelegram telegram, Map<String, String> requestParams) {
		List<NameValuePair> queryStrings = new ArrayList<>();
		telegram.getRequests().stream().filter((r) -> r.getIn().equalsIgnoreCase("query")).forEach((p) -> {
			if (requestParams.containsKey(p.getName())) {
				queryStrings.add(
						new NameValuePair(p.getName(), (String) getParamValue(p.getName(), "string", requestParams)));
			}
		});
		method.setQueryString(queryStrings.toArray(new NameValuePair[0]));
	}

	private void setFormParam(PostMethod method, ApiTelegram telegram, Map<String, String> requestParams) {
		telegram.getRequests().stream().filter((r) -> r.getType().equalsIgnoreCase("formData")).forEach((p) -> {
			if (requestParams.containsKey(p.getName())) {
				method.addParameter(
						new NameValuePair(p.getName(), (String) getParamValue(p.getName(), "string", requestParams)));
			}
		});
	}

	private String getUrl(String scheme, String host, String basePath, String requestUri,
			List<ApiRequestParameter> requests, Map<String, String> requestParams) {
		StringBuilder sb = new StringBuilder(requestUri);
		requests.stream().filter((p) -> p.getIn().equalsIgnoreCase("path")).forEach((p) -> {
			String name = p.getName();
			sb.replace(sb.indexOf("{" + name), sb.indexOf("}", sb.indexOf("{" + name) + ("{" + name).length()) + 1,
					requestParams.get(p.getName()));
		});
		String resolvedRequestUri = sb.toString();
		sb.setLength(0);
		sb.append(scheme.toLowerCase()).append("://").append(host).append(basePath).append(resolvedRequestUri);
		return sb.toString();
	}
}
