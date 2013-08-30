package com.liaison.mailbox.service.core.component;

import java.io.IOException;
import java.io.OutputStream;

public class HTTPStringOutputStream extends OutputStream {

	private StringBuilder content = new StringBuilder();
    @Override
    public void write(int b) throws IOException {
        this.content.append((char) b );
    }
    
    public String getContent() {
    	return this.content.toString();
    }
    
    public String toString(){
        return this.content.toString();
    }

}
