/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto;

/**
 * Helper DTO that contains various search filter details.
 * 
 * @author OFS
 */
public class GenericSearchFilterDTO {

	public static final String MATCH_MODE_EQUALS_CHR = "=";
	public static final String MATCH_MODE_EQUALS_STR = "equals";
	public static final String MATCH_MODE_LIKE = "like";
	
	private String mbxGuid;
	private String mbxName;
	private String serviceInstanceId;
	private String profileName;
	private String page;
	private String pageSize;
	private String sortField;
	private String sortDirection;
	private String stagedFileName;
	private String uploadedFileName;
	private String status;
	private String pipelineId;
	private String folderPath;
	private String protocol;
	private String processorType;
	private String processorName;
	private String processorGuid;
	private String matchMode;
	private String scriptName;
	private String clusterType;
	private boolean isDisableFilters;
	private boolean isMinResponse;
    private String keyGroupId;	
	
	public String getMbxGuid() {
	    return mbxGuid;
	}

	public void setMbxGuid(String mbxGuid) {
	    this.mbxGuid = mbxGuid;
	}

	public String getMbxName() {
		return mbxName;
	}

	public void setMbxName(String mbxName) {
		this.mbxName = mbxName;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}
	
	public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getPageSize() {
		return pageSize;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public String getSortDirection() {
		return sortDirection;
	}

	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}

    public String getStagedFileName() {
        return stagedFileName;
    }

    public void setStagedFileName(String stagedFileName) {
        this.stagedFileName = stagedFileName;
    }
    
    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPipelineId() {
		return pipelineId;
	}

	public void setPipelineId(String pipelineId) {
		this.pipelineId = pipelineId;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getProcessorType() {
		return processorType;
	}

	public void setProcessorType(String processorType) {
		this.processorType = processorType;
	}

	public boolean isDisableFilters() {
		return isDisableFilters;
	}

	public void setDisableFilters(boolean isDisableFilters) {
		this.isDisableFilters = isDisableFilters;
	}

	public String getProcessorName() {
		return processorName;
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}

	public String getProcessorGuid() {
		return processorGuid;
	}

	public void setProcessorGuid(String processorGuid) {
		this.processorGuid = processorGuid;
	}
	
	public String getScriptName() {
	    return scriptName;
	}
	
	public void setScriptName(String scriptName) {
	    this.scriptName = scriptName;
	}

    public String getClusterType() {
        return clusterType; 
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

	public String getMatchMode() {
		return matchMode;
	}
	
	public boolean isMinResponse() {
	    return isMinResponse;
	}
	
	public void setMinResponse(boolean isMinResponse) {
	    this.isMinResponse = isMinResponse;
	}

	public void setMatchMode(String matchMode) {

	    if (null != matchMode) {
	        // Force matchMode to lowercase
	        matchMode = matchMode.toLowerCase();
	        switch (matchMode) {
	            case MATCH_MODE_EQUALS_STR:
	                    this.matchMode = MATCH_MODE_EQUALS_CHR;
	                break;
	            default:
	                    this.matchMode = MATCH_MODE_LIKE;
	                break;
	        }
	    } else {
	        this.matchMode = MATCH_MODE_LIKE;
	    }
    }

    public String getKeyGroupId() {
        return keyGroupId;
    }

    public void setKeyGroupId(String keyGroupId) {
        this.keyGroupId = keyGroupId;
    }

}
