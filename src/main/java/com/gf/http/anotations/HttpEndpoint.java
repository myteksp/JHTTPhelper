package com.gf.http.anotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) 
public @interface HttpEndpoint {
	
	public static enum Method{
		GET, POST
	}
	
	public String path();
	
	public Method method();
}
