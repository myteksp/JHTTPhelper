package com.gf.http;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


import com.gf.http.anotations.HttpEndpoint;
import com.gf.http.anotations.HttpEndpoint.Method;



@RunWith(SpringRunner.class)
public class HttpApplicationTests {
	
	private final HttpEndpointCreator creator = new  HttpEndpointCreator();
	
	@After
	public void close() throws IOException{
		creator.close();
	}

	@Test
	public void contextLoads() {
		final TestRepo repo = creator.makeRepo("https://www.wikipedia.org", new TestRepo() {
			@Override
			public String root() {return null;}
		}, TestRepo.class);
		final String root = repo.root();
		assertTrue(root.contains("English") && root.contains("Wikipedia"));
	}
	
	
	private static interface TestRepo{
		@HttpEndpoint(path="/", method=Method.GET)
		String root();
	}

}
