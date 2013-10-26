/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.directorysweeper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MetaDataDTO
 * 
 * <P>
 * Class describes the meta data about the file.
 * 
 * @author veerasamyn
 */

@JsonRootName("metadata")
public class MetaDataDTO {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataDTO.class);
	List<FileAttributesDTO> fileAttributesList = null;
	List<List<FileAttributesDTO>> metadata = new ArrayList<>();

	public MetaDataDTO() {

	}

	/**
	 * Parameterized constructor.
	 * 
	 * @param filename
	 * @param size
	 * @param folderdername
	 * @param timestamp
	 * @throws IOException
	 */
	public MetaDataDTO(List<List<Path>> fileGroups) throws IOException {

		for (List<Path> fileList : fileGroups) {
			fileAttributesList = new ArrayList<>();
			for (Path file : fileList) {

				FileAttributesDTO fileAttributes = new FileAttributesDTO();
				fileAttributes.setFilename(file.getFileName().toString());
				fileAttributes.setSize(Files.size(file));
				fileAttributes.setFolderdername(file.getParent().toString());
				fileAttributes.setTimestamp(Files.getLastModifiedTime(file).toString());
				fileAttributesList.add(fileAttributes);
			}
			metadata.add(fileAttributesList);
		}

	}

	public List<List<FileAttributesDTO>> getMetaData() {
		return metadata;
	}

	public void setMetaData(List<List<FileAttributesDTO>> metaData) {
		this.metadata = metaData;
	}

}
