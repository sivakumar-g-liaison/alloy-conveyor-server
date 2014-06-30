/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.directorysweeper;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * FileGroupDTO
 * 
 * <P>
 * Class describes the meta data about the file.
 * 
 * @author veerasamyn
 */

@JsonRootName("fileGroups")
public class FileGroupDTO {

	private List<FileAttributesDTO> fileAttributes = null;

	public FileGroupDTO() {

	}

	public List<FileAttributesDTO> getFileAttributes() {

		if (null == fileAttributes) {
			fileAttributes = new ArrayList<>();
		}

		return fileAttributes;
	}

	public void setFileAttributes(List<FileAttributesDTO> fileAttributes) {
		this.fileAttributes = fileAttributes;
	}

}
