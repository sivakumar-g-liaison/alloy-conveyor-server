package com.liaison.mailbox;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.entity.ContentType;

public class Test {

	public static void main(String[] args) throws URISyntaxException, IOException {

		System.out.println(ContentType.TEXT_PLAIN.getMimeType());
	}
}
