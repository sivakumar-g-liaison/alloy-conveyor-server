package com.liaison.mailbox.enums;

public enum Protocol {
	
	FTP("ftp"),
	HTTP("http"),
	HTTPS("https");
	
	 private final String code;

    private Protocol(String code) { this.code = code; }

    @Override
    public String toString() { return code; }
	
}
