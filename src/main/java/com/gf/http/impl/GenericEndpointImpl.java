package com.gf.http.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.gf.http.GenericHttpEndpoint;

public final class GenericEndpointImpl implements GenericHttpEndpoint{
	private final Map<String, String> defaultHeaders;
	private final RestTemplate rest;
	private final String baseUrl;

	public GenericEndpointImpl(final String baseUrl, final RestTemplate rest, final Map<String, String> defaultHeaders) {
		this.rest = rest;
		this.defaultHeaders = new HashMap<String, String>(defaultHeaders.size() + 5);
		this.baseUrl = baseUrl;
		this.setDefaultHeaders(defaultHeaders);
	}
	
	@Override
	public Map<String, String> getDefaultHeaders() {
		return defaultHeaders;
	}
	@Override
	public void setDefaultHeaders(final Map<String, String> headers) {
		defaultHeaders.clear();
		for (final Entry<String, String> e : headers.entrySet())
			defaultHeaders.putIfAbsent(e.getKey(), e.getValue());
	}
	
	private final HttpHeaders buildeaders(){
		final HttpHeaders headers = new HttpHeaders();
		for (final Entry<String, String> e : defaultHeaders.entrySet())
			headers.set(e.getKey(), e.getValue());
		return headers;
	}
	
	private final HttpHeaders buildeaders(final String contentType){
		final HttpHeaders headers = buildeaders();
		headers.set("Content-Type", contentType);
		return headers;
	}
	
	private final String getUrl(final String path){
		return this.baseUrl + path;
	}
	
	private final String getContentType(final Object obj){
		if (obj instanceof InputStream){
			return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}else if (obj instanceof byte[]){
			return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		return MediaType.APPLICATION_JSON_UTF8_VALUE;
	}

	@Override
	public <T> T get(String path, Class<T> clz) {
		final String url = getUrl(path);
		final HttpEntity<?> requestEntity = new HttpEntity<Object>(buildeaders());
		final ResponseEntity<T> result = rest.exchange(url, HttpMethod.GET, requestEntity, clz);
		return result.getBody();
	}

	@Override
	public Map<String, String> head(String path) {
		final String url = getUrl(path);
		final HttpHeaders res = rest.headForHeaders(url);
		final HashMap<String, String> headers = new HashMap<String, String>(res.size());
		for(final Entry<String, List<String>> e : res.entrySet()){
			final String key = e.getKey();
			final List<String> val = e.getValue();
			final StringBuilder output = new StringBuilder(100);
			final int lastIndex = val.size() - 1;
			for (int i = 0; i < lastIndex; i++) 
				output.append(val.get(i)).append(", ");
			
			output.append(val.get(lastIndex));
			headers.put(key, output.toString());
		}
		return headers;
	}

	@Override
	public <T> T post(String path, Object body, Class<T> clz) {
		final String url = getUrl(path);
		final HttpEntity<?> requestEntity = new HttpEntity<Object>(body, buildeaders(getContentType(body)));
		final ResponseEntity<T> result = rest.exchange(url, HttpMethod.POST, requestEntity, clz);
		return result.getBody();
	}
}
