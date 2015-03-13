/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.entity.ContentType;

/**
 * @author OFS
 *
 */
public class Test {

	public static void main(String[] args) throws URISyntaxException, IOException {

		System.out.println(ContentType.TEXT_PLAIN.getMimeType());
	}
}
