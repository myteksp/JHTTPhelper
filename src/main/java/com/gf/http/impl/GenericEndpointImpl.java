package com.gf.http.impl;

import java.io.InputStream;
import java.util.ArrayList;
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
import com.gf.http.Tracer;
import com.gf.util.string.JSON;
import com.gf.util.string.MacroCompiler;

public final class GenericEndpointImpl implements GenericHttpEndpoint{
	private final List<Header> defaultHeaders;
	private final RestTemplate rest;
	private final String baseUrl;

	public GenericEndpointImpl(final String baseUrl, final RestTemplate rest, final Map<String, String> defaultHeaders) {
		this.rest = rest;
		this.defaultHeaders = new ArrayList<Header>(defaultHeaders.size());
		this.baseUrl = baseUrl;
		this.setDefaultHeaders(defaultHeaders);
	}

	@Override
	public Map<String, String> getDefaultHeaders() {
		final HashMap<String, String> res = new HashMap<String, String>(defaultHeaders.size() + 5);
		for(final Header h : defaultHeaders)
			res.put(h.name, h.value);
		return res;
	}
	@Override
	public void setDefaultHeaders(final Map<String, String> headers) {
		defaultHeaders.clear();
		for (final Entry<String, String> e : headers.entrySet()) 
			defaultHeaders.add(new Header(e.getKey(), e.getValue()));
	}

	private final HttpHeaders buildeaders(){
		final HttpHeaders headers = new HttpHeaders();
		for (final Header h : defaultHeaders)
			headers.set(h.name, h.value);
		return headers;
	}

	private final HttpHeaders buildHeaders(final String contentType){
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
	public <T> T get(final String path, final Class<T> clz, final Tracer tracer) {
		try {
			tracer.trace("->>GET:");
			final String url = getUrl(path);
			final HttpHeaders hdrs = buildeaders();
			final HashMap<String, Object> traceObject = new HashMap<String, Object>();
			traceObject.put("url", url);
			traceObject.put("requestHeaders", hdrs);

			final ResponseEntity<T> result = rest.exchange(url, HttpMethod.GET, new HttpEntity<Object>(hdrs), clz);
			final T res = result.getBody();

			traceObject.put("responseHeaders", result.getHeaders());
			traceObject.put("response", res);
			tracer.trace(JSON.toPrettyJson(traceObject));
			tracer.trace("<<-GET");

			return res;
		}catch(final Throwable t) {
			tracer.trace(t.getMessage() + '\n' + JSON.toPrettyJson(t.getStackTrace()));
			tracer.trace("<<-GET");
			throw new RuntimeException(t);
		}
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
		final HttpEntity<?> requestEntity = new HttpEntity<Object>(body, buildHeaders(getContentType(body)));
		final ResponseEntity<T> result = rest.exchange(url, HttpMethod.POST, requestEntity, clz);
		return result.getBody();
	}

	@Override
	public <T> T post(final String path, final Object body, final Class<T> clz, final Tracer tracer) {
		try {
			tracer.trace("->>POST:");
			final HashMap<String, Object> traceObject = new HashMap<String, Object>();
			
			final String url = getUrl(path);
			final HttpHeaders hdrs = buildHeaders(getContentType(body));
			final HttpEntity<?> requestEntity = new HttpEntity<Object>(body, hdrs);
			
			traceObject.put("url", url);
			traceObject.put("requestHeaders", hdrs);
			traceObject.put("request", body);
			
			final ResponseEntity<T> result = rest.exchange(url, HttpMethod.POST, requestEntity, clz);
			final T res = result.getBody();
			traceObject.put("responseHeaders", result.getHeaders());
			traceObject.put("response", res);
			tracer.trace(JSON.toPrettyJson(traceObject));
			tracer.trace("<<-POST");
			return res;
		}catch(final Throwable t) {
			tracer.trace(t.getMessage() + '\n' + JSON.toPrettyJson(t.getStackTrace()));
			tracer.trace("<<-POST");
			throw new RuntimeException(t);
		}
	}



	private static final class Header{
		public final String name;
		public final String value;
		public Header(final String name, final String value) {
			this.name = name;
			this.value = value;
		}
		@Override
		public final int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		@Override
		public final boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Header other = (Header) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
		@Override
		public final String toString() {
			return MacroCompiler.compileInline("${0} : ${1}", name, value);
		}
	}
}
