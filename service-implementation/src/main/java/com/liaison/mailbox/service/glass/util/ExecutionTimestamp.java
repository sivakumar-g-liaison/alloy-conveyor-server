/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.glass.util;

import java.io.Serializable;
import java.util.Date;

import com.liaison.commons.message.glass.dom.TimeStamp;
import com.liaison.commons.message.glass.dom.TimeStampType;
import com.liaison.commons.message.glass.util.GlassMessageUtil;
import com.liaison.commons.util.UUIDGen;

/**
 * used for recording timestamps eventually to glass
 * 
 * @author jeremyfranklin-ross
 * 
 */
public class ExecutionTimestamp
		implements Serializable {

	private static final long serialVersionUID = -7781501685922032343L;
	protected String name;
	protected Date timestamp;
	protected String ID = UUIDGen.getCustomUUID();
	protected String sessionID;
	protected TimestampType type;

	/**
	 * Consider using the static beginTimestamp and endTimestamp/endTimestampFor methods
	 * 
	 * @param name Name of the timestamp instance
	 * @param sessionID Identifier. Use GlobalProcessId
	 * @param timestamp Current Date
	 * @param type Type of the timestamp. Start/End
	 * @see #beginTimestamp(String)
	 * @see #endTimestampFor(ExecutionTimestamp)
	 */
	public ExecutionTimestamp(String name, String sessionID, Date timestamp, TimestampType type) {
		this.name = name;
		this.sessionID = sessionID;
		this.timestamp = timestamp;
		this.type = type;
	}

	/**
	 * Handy means to begin a timestamp
	 * 
	 * @param name Name for the timestamp instance
	 * @return Execution
	 * @see #endTimestampFor(ExecutionTimestamp)
	 */
	public static ExecutionTimestamp beginTimestamp(String name) {
		return new ExecutionTimestamp(name, UUIDGen.getCustomUUID(), new Date(), TimestampType.Start);
	}

	public static ExecutionTimestamp endTimestamp(String name) {
		return new ExecutionTimestamp(name, null, new Date(), TimestampType.End);
	}

	public static ExecutionTimestamp endTimestamp(String name, String sessionID) {
		return new ExecutionTimestamp(name, sessionID, new Date(), TimestampType.End);
	}

	public enum TimestampType {
		Start,
		End
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public TimestampType getType() {
		return type;
	}

	public void setType(TimestampType type) {
		this.type = type;
	}

	public String getID() {
		return ID;
	}

	public TimeStamp buildGlassTimeStamp() {
		return ExecutionTimestamp.buildGlassTimeStamp(this);
	}

	public static TimeStamp buildGlassTimeStamp(ExecutionTimestamp timestamp) {
		TimeStamp glassTimeStamp = new TimeStamp();
		glassTimeStamp.setName(timestamp.getName());
		glassTimeStamp.setTime(GlassMessageUtil.convertToXMLGregorianCalendar(timestamp.getTimestamp()));
		glassTimeStamp.setTimeStampId(timestamp.getID());
		glassTimeStamp.setTimeStampSessionId(timestamp.getSessionID());
		String timestampname = timestamp.getType().name().toUpperCase();
		TimeStampType t = TimeStampType.valueOf(timestampname);
		glassTimeStamp.setType(t);
		return glassTimeStamp;
	}
}