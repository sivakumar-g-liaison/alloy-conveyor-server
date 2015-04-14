/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import com.google.gson.JsonParseException;

/**
 * Utilities for JSON.
 * 
 * @author OFS
 */
public class JSONUtil {

	/**
	 * 
	 * @param serializedJson
	 * @param clazz
	 * 
	 * @return json
	 * 
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T unmarshalFromKeyManagerJSON(String serializedJson, Class<T> clazz)
			throws JAXBException, JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector introspector = new AnnotationIntrospector.Pair(primary, secondary);
		// make deserializer use JAXB annotations (only)
		mapper.setAnnotationIntrospector(introspector);
		T ummarshaledObject = (T) mapper.readValue(serializedJson, clazz);
		return ummarshaledObject;
	}

	/**
	 * Method is used to unmarshal the string to Object
	 * 
	 * @param serializedJson
	 * @param clazz
	 * 
	 * @return json
	 * 
	 * @throws JAXBException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T unmarshalFromJSON(String serializedJson, Class<T> clazz)
			throws JAXBException, JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector introspector = new AnnotationIntrospector.Pair(primary, secondary);
		// make deserializer use JAXB annotations (only)
		mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
		T ummarshaledObject = (T) mapper.readValue(serializedJson, clazz);
		return ummarshaledObject;
	}

	/**
	 * Method is used to marshal the Object to JSON.
	 * 
	 * @param object
	 * @return String
	 * @throws JAXBException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static String marshalToJSON(Object object)
			throws JAXBException, JsonGenerationException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector introspector = new AnnotationIntrospector.Pair(primary, secondary);
		// make deserializer use JAXB annotations (only)
		mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
		String jsonBuilt = mapper.writeValueAsString(object);
		return jsonBuilt;
	}

}
