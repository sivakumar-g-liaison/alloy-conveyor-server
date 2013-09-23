/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.MailBoxStatus;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * Common validator for all mailbox operations.
 * 
 * @author veerasamyn
 */
public class GenericValidator {

	private final static Logger LOGGER = LoggerFactory.getLogger(GenericValidator.class);

	private StringBuffer errorMessage;

	public GenericValidator() {
		super();
	}

	/**
	 * Validate the given object.
	 * 
	 * @param dto
	 *            The DTO which has to be validated
	 * @return true if only validate is passed
	 * @throws MailBoxConfigurationServicesException
	 */
	public boolean validate(Object dto) throws MailBoxConfigurationServicesException {

		LOGGER.info("Entering into validate method.");

		try {

			errorMessage = new StringBuffer();
			if (!doValidate(dto)) {
				throw new MailBoxConfigurationServicesException(Messages.ERROR_MSG.value() + errorMessage.toString());
			}
		} catch (Exception e) {
			throw new MailBoxConfigurationServicesException(e.getMessage());
		}

		return true;
	}

	/**
	 * Validates the all fields using custom annotation.
	 * 
	 * @param dto
	 *            The DTO which has to be validated
	 * @return true if only validation is passed
	 * @throws MailBoxConfigurationServicesException
	 */
	private boolean doValidate(Object dto) throws MailBoxConfigurationServicesException {

		boolean isValidationPassed = true;
		boolean isMandatoryCheckPassed = true;

		if (null == dto) {
			throw new MailBoxConfigurationServicesException(Messages.VALIDATOR_ERROR_MSG);
		}

		try {

			// Getting the list of methods from object
			for (Method m : dto.getClass().getMethods()) {

				// Checks the mandatory data
				if (m.isAnnotationPresent(Mandatory.class)) {
					if (!isMandatoryDataAvailable(m, dto)) {
						isValidationPassed = false;
						isMandatoryCheckPassed = false;
					}
				}

				// Checks the available data is valid or not
				if (isMandatoryCheckPassed && m.isAnnotationPresent(DataValidation.class)) {
					isValidationPassed = isValidationPassed && isThisValidData(m, dto);
				}
			}

		} catch (IllegalAccessException e) {
			throw new MailBoxConfigurationServicesException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new MailBoxConfigurationServicesException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new MailBoxConfigurationServicesException(e.getMessage());
		}

		return isValidationPassed;
	}

	/**
	 * Validates mandatory fields.
	 * 
	 * @param method
	 *            The method which has Mandatory annotation
	 * @param dto
	 *            The DTO which has to be validated
	 * @return true if only validation is passed
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private boolean isMandatoryDataAvailable(Method method, Object dto) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		Mandatory annotationDetails = method.getAnnotation(Mandatory.class);
		boolean isValidationPassed = true;

		Object value = method.invoke(dto);
		if (value == null || value.toString().trim().equals("")) {
			isValidationPassed = false;
			errorMessage.append(annotationDetails.errorMessage());

		} else if (value instanceof Long && value.equals(new Long(0))) {
			isValidationPassed = false;
			errorMessage.append(annotationDetails.errorMessage());
		} else if (value instanceof List) {

			@SuppressWarnings("unchecked")
			List<Object> object = (List<Object>) value;
			if (object.isEmpty()) {
				isValidationPassed = false;
				errorMessage.append(annotationDetails.errorMessage());
			}
		}

		return isValidationPassed;
	}

	/**
	 * Validates the given is data is valid or not.
	 * 
	 * @param method
	 *            The method which has DataValidation annotation
	 * @param dto
	 *            The DTO which has to be validated
	 * @return true if only validation is passed
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private boolean isThisValidData(Method method, Object dto) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		DataValidation annotationDetails = method.getAnnotation(DataValidation.class);
		boolean isValidationPassed = false;

		Object value = method.invoke(dto);
		if (value != null && !value.toString().isEmpty()) {
			isValidationPassed = enumValidation(annotationDetails, value);
		}

		return isValidationPassed;
	}

	/**
	 * Validates the given values is avail in the Enum.
	 * 
	 * @param annotationDetails
	 *            DataValidation annotation
	 * @param value
	 *            The value of the property in the DTO.
	 * @return true if only validation is passed
	 */
	private boolean enumValidation(DataValidation annotationDetails, Object value) {

		if (MailBoxConstants.MBX_STATUS.equals(annotationDetails.type())) {
			if (MailBoxStatus.findByName(String.valueOf(value)) == null) {
				errorMessage.append(annotationDetails.errorMessage());
				return false;
			}
		}

		return true;
	}
}
