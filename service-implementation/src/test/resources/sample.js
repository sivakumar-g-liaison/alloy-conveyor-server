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