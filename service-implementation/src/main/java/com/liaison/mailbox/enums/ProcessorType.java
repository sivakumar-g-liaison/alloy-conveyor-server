package com.liaison.mailbox.enums;

public enum ProcessorType {
	
	REMOTEDOWNLOADER("remotedownloader"),
	REMOTEUPLOADER("remoteuploader");
	
	 private final String code;

    private ProcessorType(String code) { this.code = code; }

    @Override
    public String toString() { return code; }
	
}
