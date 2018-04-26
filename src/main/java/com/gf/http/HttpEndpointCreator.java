package com.gf.http;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.gf.http.anotations.HttpEndpoint;
import com.gf.http.entities.RequestBody;
import com.gf.http.entities.UrlParam;
import com.gf.http.impl.GenericEndpointImpl;

public final class HttpEndpointCreator implements Closeable{
	private final RestTemplate rest;
	private final CloseableHttpClient httpclient;
	private final ConcurrentHashMap<String, String> defaultHeaders;

	public HttpEndpointCreator(
			final int maxConnectionsPerRoute,
			final int maxConnectionsTotal,
			final long connectionTTL,
			final TimeUnit connectionTTLtimeUnits){
		this.httpclient = HttpClients
				.custom()
				.setConnectionTimeToLive(1, connectionTTLtimeUnits)
				.setMaxConnPerRoute(maxConnectionsPerRoute)
				.setMaxConnTotal(maxConnectionsTotal)
				.build();
		this.rest = new RestTemplateBuilder()
				.detectRequestFactory(true)
				.requestFactory(new Supplier<ClientHttpRequestFactory>() {
					@Override
					public final ClientHttpRequestFactory get() {
						return new HttpComponentsClientHttpRequestFactory(httpclient);
					}
				})
				.build();
		this.defaultHeaders = new ConcurrentHashMap<String, String>();
	}

	public HttpEndpointCreator(
			final int maxConnectionsPerRoute,
			final int maxConnectionsTotal,
			final long connectionTTL,
			final TimeUnit connectionTTLtimeUnits,
			final String username,
			final String password){
		final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		this.httpclient = HttpClients
				.custom()
				.setConnectionTimeToLive(1, connectionTTLtimeUnits)
				.setMaxConnPerRoute(maxConnectionsPerRoute)
				.setMaxConnTotal(maxConnectionsTotal)
				.setDefaultCredentialsProvider(credentialsProvider)
				.build();
		this.rest = new RestTemplateBuilder()
				.detectRequestFactory(true)
				.requestFactory(new Supplier<ClientHttpRequestFactory>() {
					@Override
					public final ClientHttpRequestFactory get() {
						return new HttpComponentsClientHttpRequestFactory(httpclient);
					}
				})
				.build();
		this.defaultHeaders = new ConcurrentHashMap<String, String>();
	}

	public HttpEndpointCreator(
			final int maxConnectionsPerRoute,
			final int maxConnectionsTotal){
		this(maxConnectionsPerRoute, maxConnectionsTotal, 1, TimeUnit.DAYS);
	}

	public HttpEndpointCreator(
			final int maxConnectionsPerRoute,
			final int maxConnectionsTotal,
			final String username, 
			final String password){
		this(maxConnectionsPerRoute, maxConnectionsTotal, 1, TimeUnit.DAYS, username, password);
	}

	public HttpEndpointCreator(
			final int maxConnectionsPerRoute,
			final String username, 
			final String password){
		this(maxConnectionsPerRoute, maxConnectionsPerRoute * 10, username, password);
	}

	public HttpEndpointCreator(final int maxConnectionsPerRoute){
		this(maxConnectionsPerRoute, maxConnectionsPerRoute * 10);
	}

	public HttpEndpointCreator(){
		this(1000);
	}

	public HttpEndpointCreator(
			final String username, 
			final String password){
		this(1000, username, password);
	}
	
	
	private final String serializeParams(final Object[] params){
		if (params == null)
			return "";
		final List<UrlParam> urlParams = new ArrayList<UrlParam>(params.length);
		int estimatedLength = 0;
		for(final Object o : params)
			if (o instanceof UrlParam){
				final UrlParam p = (UrlParam)o;
				estimatedLength += p.length();
				urlParams.add(p);
			}
		
		final int len = urlParams.size();
		if (len > 0){
			final int lastIndex = len - 1;
			final StringBuilder sb = new StringBuilder(estimatedLength + len);
			sb.append('?');
			for (int i = 0; i < lastIndex; i++) {
				final UrlParam p = urlParams.get(i);
				sb.append(p.name).append('=').append(p.value).append('&');
			}
			final UrlParam p = urlParams.get(lastIndex);
			sb.append(p.name).append('=').append(p.value);
			return sb.toString();
		}
		return "";
	}
	
	private final RequestBody getBody(final Object[] params){
		if (params == null)
			return new RequestBody("");
		
		for(final Object o : params)
			if (o instanceof RequestBody)
				return (RequestBody) o;
		
		return new RequestBody("");
	}

	@SuppressWarnings("unchecked")
	public final <T> T makeRepo(final String baseUrl, final Object ifs, final Class<T> clz){
		return (T) java.lang.reflect.Proxy.newProxyInstance(
				clz.getClassLoader(), 
				new Class[]{clz}, 
				new InvocationHandler() {
					private final GenericHttpEndpoint endPoint = getGenericEndPoint(baseUrl);
					@Override
					public final Object invoke(
							final Object proxy, 
							final Method method, 
							final Object[] args) throws Throwable {
						try {
							final HttpEndpoint anotation = method.getAnnotation(HttpEndpoint.class);
							final String path;
							final com.gf.http.anotations.HttpEndpoint.Method httpMethod;
							if (anotation == null){
								path = method.getName();
								httpMethod = com.gf.http.anotations.HttpEndpoint.Method.GET;
							}else{
								path = anotation.path();
								httpMethod = anotation.method();
							}
							switch(httpMethod){
							case GET:
								return endPoint.get(path + serializeParams(args), method.getReturnType());
							case POST:
								return endPoint.post(path + serializeParams(args), getBody(args).value, method.getReturnType());
							}
						} catch (final Exception e) {
							if (e instanceof InvocationTargetException)
								throw ((InvocationTargetException)e).getTargetException();
							throw e;
						}
						return null;
					}
				});
	}
	
	public final <T> T makeRepo(final Object ifs, final Class<T> clz){
		return this.makeRepo("", ifs, clz);
	}

	public final GenericHttpEndpoint getGenericEndPoint(){
		return getGenericEndPoint("");
	}

	public final GenericHttpEndpoint getGenericEndPoint(final String baseUrl){
		return new GenericEndpointImpl(baseUrl, rest, defaultHeaders);
	}

	@Override
	public final void close() throws IOException {
		httpclient.close();
	}

	public final RestTemplate getTemplete(){
		return rest;
	}
}
