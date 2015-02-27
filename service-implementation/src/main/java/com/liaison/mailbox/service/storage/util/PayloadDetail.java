/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.storage.util;

import com.liaison.fs2.api.FS2MetaSnapshot;

/**
 * @author VNagarajan
 *
 * Class contains meta about the payload and size.
 * Wrapper for Payload Metadata
 */
public class PayloadDetail {

	private FS2MetaSnapshot metaSnapshot;
	private long payloadSize;

	public FS2MetaSnapshot getMetaSnapshot() {
		return metaSnapshot;
	}
	public void setMetaSnapshot(FS2MetaSnapshot metaSnapshot) {
		this.metaSnapshot = metaSnapshot;
	}
	public long getPayloadSize() {
		return payloadSize;
	}
	public void setPayloadSize(long payloadSize) {
		this.payloadSize = payloadSize;
	}

}