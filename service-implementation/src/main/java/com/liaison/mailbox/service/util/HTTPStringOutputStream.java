/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

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
