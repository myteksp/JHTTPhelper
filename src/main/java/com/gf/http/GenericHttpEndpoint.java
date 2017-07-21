package com.gf.http;

import java.util.Map;

public interface GenericHttpEndpoint {
	Map<String, String> getDefaultHeaders();
	void setDefaultHeaders(final Map<String, String> headers);
	<T> T get(final String path, final Class<T> clz);
	Map<String, String> head(final String path);
	<T> T post(final String path, final Object body, final Class<T> clz);
}
