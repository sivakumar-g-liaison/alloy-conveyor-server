package com.liaison.mailbox.enums;

public enum ProcessorComponentType {
	
	REMOTEDOWNLOADER("remotedownloader"),
	REMOTEUPLOADER("remoteuploader");
	
	 private final String code;

    private ProcessorComponentType(String code) { this.code = code; }

    @Override
    public String toString() { return code; }
	
}
