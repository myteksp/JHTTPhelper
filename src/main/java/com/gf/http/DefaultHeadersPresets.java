package com.gf.http;

import java.util.HashMap;
import java.util.Map;

public final class DefaultHeadersPresets {
	public static final Map<String, String> keepAlive(final Map<String, String> headers){
		headers.put("Connection", "keep-alive");
		return headers;
	}
	
	public static final Map<String, String> acceptAll(final Map<String, String> headers){
		headers.put("Accept", "*/*");
		return headers;
	}
	
	public static final Map<String, String> userAgent(final Map<String, String> headers, final String value){
		headers.put("User-Agent", value);
		return headers;
	}
	
	public static final Map<String, String> userAgentChrome(final Map<String, String> headers){
		return userAgent(headers, "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 YaBrowser/17.6.0.1633 Yowser/2.5 Safari/537.36");
	}
	
	public static final Map<String, String> buildDefault(){
		return userAgentChrome(
				acceptAll(
						keepAlive(
								new HashMap<String, String>()
								)
						)
				);
	}
}
