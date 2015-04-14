package com.liaison.mailbox.service.dto;

public class GenericSearchFilterDTO {
	
	private String mbxName;
	private String serviceInstanceId;
	private String profileName;
	private String page;
	private String pageSize;
	private String sortField;
	private String sortDirection;
	private String stagedFileName;
	private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
