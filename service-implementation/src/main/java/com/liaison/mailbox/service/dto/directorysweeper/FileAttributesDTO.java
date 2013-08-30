package com.liaison.mailbox.service.dto.directorysweeper;

public class FileAttributesDTO {
    
    private String filename;
    private Long size;
    private String folderdername;
    private String timestamp;
    
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }
    public String getFolderdername() {
        return folderdername;
    }
    public void setFolderdername(String folderdername) {
        this.folderdername = folderdername;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
