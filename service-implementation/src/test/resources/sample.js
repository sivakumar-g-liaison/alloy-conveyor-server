/*
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
function testHappyPath() {
	var msg = "Be happy!";
    print(msg + "\n");
    return msg;
}

function testHappyPathWithArgs(msg) {
    print(msg + "\n");
    return msg;
}

function testSystemExit() {
    java.lang.System.exit(0);
}

function testThreadSleep() {
    java.lang.Thread.sleep(86400000); // one day in millis
}

function testThrowsException() {
	throw new java.lang.Exception("This is an Exception!");
}

function testThrowsRuntimeException() {
	throw new java.lang.RuntimeException("This is a RuntimeException!");
}

function testUsingProxyClass() {
	return com.liaison.commons.scripting.proxy.System.currentTimeMillis();
}