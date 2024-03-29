/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto;

import javax.ws.rs.core.Response;

/**
 * Class that constructs the response.
 *
 * @author OFS
 */
public interface ResponseBuilder {

	public abstract Response constructResponse() throws Exception;
}
