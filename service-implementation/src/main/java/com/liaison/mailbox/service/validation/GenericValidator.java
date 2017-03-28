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

import javax.ws.rs.core.Response;

import com.liaison.dto.enums.ProcessMode;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.CredentialType;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.FolderType;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Common validator for all mailbox operations.
 *
 * @author veerasamyn
 */
public class GenericValidator {

	private final static Logger LOGGER = LogManager.getLogger(GenericValidator.class);

	private StringBuilder errorMessage;

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

		LOGGER.debug("Entering into validate method.");

		try {

			errorMessage = new StringBuilder();
			if (!doValidate(dto)) {
				throw new MailBoxConfigurationServicesException(Messages.ERROR_MSG.value() + errorMessage.toString(), Response.Status.BAD_REQUEST);
			}
		} catch (Exception e) {
			throw new MailBoxConfigurationServicesException(e.getMessage(), Response.Status.BAD_REQUEST);
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
			throw new MailBoxConfigurationServicesException(Messages.VALIDATOR_ERROR_MSG, Response.Status.BAD_REQUEST);
		}

		try {

			// Getting the list of methods from object
			for (Method m : dto.getClass().getMethods()) {

				// Checks the mandatory data
				if (m.isAnnotationPresent(Mandatory.class)
				    &&	!isMandatoryDataAvailable(m, dto)) {
						isValidationPassed = false;
						isMandatoryCheckPassed = false;
				}

				// Checks the available data is valid or not
				if (isMandatoryCheckPassed && m.isAnnotationPresent(DataValidation.class)) {
					isValidationPassed = isValidationPassed && isThisValidData(m, dto);
				}
				
				// Checks whether the provided value is valid port for type "PROPERTY_PORT"
				// or value is valid for type "PROPERTY_RETRY_ATTEMPT"
				//or value is valid for type "PROPERTY_CONNECTION_TIMEOUT" and type "PROPERTY_SOCKET_TIME_OUT"
				if (isValidationPassed && m.isAnnotationPresent(PatternValidation.class)) {
					isValidationPassed = isThisValidPattern(m, dto);
				}
			}

		} catch (IllegalAccessException e) {
			throw new MailBoxConfigurationServicesException(e.getMessage(), Response.Status.BAD_REQUEST);
		} catch (IllegalArgumentException e) {
			throw new MailBoxConfigurationServicesException(e.getMessage(), Response.Status.BAD_REQUEST);
		} catch (InvocationTargetException e) {
			throw new MailBoxConfigurationServicesException(e.getMessage(), Response.Status.BAD_REQUEST);
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
		boolean isValidationPassed = true;

		Object value = method.invoke(dto);
		if (value != null && !value.toString().isEmpty()) {
			isValidationPassed = enumValidation(annotationDetails, value);
		}

		return isValidationPassed;
	}
	
	/**
	 * Method to validate whether the given value matches with the pattern
	 * in case of retryAttempts, port, connection timeout and socket timeout
	 *
	 * @param method
	 * @param dto
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private boolean isThisValidPattern (Method method, Object dto) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		PatternValidation annotationDetails = method.getAnnotation(PatternValidation.class);
		boolean isValidPattern = true;

		Object value = method.invoke(dto);

		if (value != null && !value.toString().isEmpty()) {

			if ((annotationDetails.type().equals(MailBoxConstants.PROPERTY_CONNECTION_TIMEOUT) && !isBetweenRange(value))) {
				isValidPattern = false;
				errorMessage.append(annotationDetails.errorMessage());
			}
			
			if ((annotationDetails.type().equals(MailBoxConstants.PROPERTY_SOCKET_TIMEOUT) && !isBetweenRange(value))) {
				isValidPattern = false;
				errorMessage.append(annotationDetails.errorMessage());
			}
			
			if ((annotationDetails.type().equals(MailBoxConstants.PROPERTY_SCRIPT_EXECUTION_TIMEOUT) && !isScriptExecutionBetweenRange(value))) {
				isValidPattern = false;
				errorMessage.append(annotationDetails.errorMessage());
			}

            if ((annotationDetails.type().equals(MailBoxConstants.PROPERTY_STALE_FILE_TTL) && !isStaleTTLBetweenRange(value))) {
                isValidPattern = false;
                errorMessage.append(annotationDetails.errorMessage());
            }

			if ((annotationDetails.type().equals(MailBoxConstants.PROPERTY_RETRY_ATTEMPTS) && !isValidRetryAttemptValue(value))) {
				isValidPattern = false;
				errorMessage.append(annotationDetails.errorMessage());
			}
			
			if (MailBoxConstants.PROPERTY_URL.equals(annotationDetails.type()) && !isValidURL(value)) {
			    isValidPattern = false;
			    errorMessage.append(annotationDetails.errorMessage());
			}
			
			if (MailBoxConstants.PROPERTY_NO_OF_FILES_THRESHOLD.equals(annotationDetails.type())
	                &&  !isValidNumberOfFilesThreshold(String.valueOf(value))) {
				isValidPattern = false;
	            errorMessage.append(annotationDetails.errorMessage());
			}

            if (MailBoxConstants.PROPERTY_PROCESS_MODE.equals(annotationDetails.type())) {
                if ((!ProcessMode.SYNC.name().equalsIgnoreCase(String.valueOf(value))) &&
                        (!ProcessMode.ASYNC.name().equalsIgnoreCase(String.valueOf(value)))) {
                    isValidPattern = false;
                    errorMessage.append(annotationDetails.errorMessage());
                }
            }
		}
		return isValidPattern;

	}
	
	/**
	 * Method to validate whether given string is valid retryAttempt value
	 *
	 * @param value retry attempt value
	 * @return boolean
	 */
	private boolean isValidRetryAttemptValue (Object value) {
		 return value.toString().matches(MailBoxConstants.retryAttemptsRegex);
	}

	/**
	 * Method to validate whether given string is valid timeout value
	 *
	 * @param value connection timeout
	 * @return boolean
	 */
	public boolean isBetweenRange (Object value) {
	    int range = Integer.valueOf(value.toString()).intValue();
		return range <= MailBoxConstants.TIMEOUT_RANGE_MAX  && range >= MailBoxConstants.TIMEOUT_RANGE_MIN;
	}

	/**
	 * Method to validate whether given string is valid Http connection timeout
	 * value
	 *
	 * @param value connectionTimeout
	 * @return boolean
	 */
	public boolean isHttpBetweenRange (Object value) {
		int range = Integer.valueOf(value.toString()).intValue();
		return range <= MailBoxConstants.HTTP_CONNECITON_TIMEOUT_RANGE_MAX && range >= MailBoxConstants.HTTP_CONNECITON_TIMEOUT_RANGE_MIN;
	}

	/**
	 * Method to validate whether given string is valid timeout value
	 *
	 * @param value script execution timeout
	 * @return boolean
	 */
	private boolean isScriptExecutionBetweenRange (Object value) {
	    int range = Integer.valueOf(value.toString()).intValue();
		return (0 == range) || (range <= MailBoxConstants.SCRIPT_EXC_TIMEOUT_RANGE_MAX  && range >= MailBoxConstants.SCRIPT_EXC_TIMEOUT_RANGE_MIN);
	}
	
    /**
     *  Method to validate whether given string is valid TTL value
     *
     * @param value ttl value
     * @return
     */
    private boolean isStaleTTLBetweenRange(Object value) {
        int range = Integer.valueOf(value.toString()).intValue();
        return (0 == range) || (range <= MailBoxConstants.STALE_FILE_TTL_RANGE_MAX  && range >= MailBoxConstants.STALE_FILE_TTL_RANGE_MIN);
    }
	
	/**
	 * Method to validate whether given string is valid number of files
	 * 
	 * @param value number of file threshold
	 * @return boolean
	 */
	private boolean isValidNumberOfFilesThreshold(String value) {
        return value.matches(MailBoxConstants.NUMBER_OF_FILES_THRESHOLD_REGX);

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

		if (MailBoxConstants.MBX_STATUS.equals(annotationDetails.type())
				&&	EntityStatus.findByName(String.valueOf(value)) == null) {
					errorMessage.append(annotationDetails.errorMessage());
					return false;
		} else if (MailBoxConstants.PROCESSOR_TYPE.equals(annotationDetails.type())
				&&  ProcessorType.findByName(String.valueOf(value)) == null) {
				    errorMessage.append(annotationDetails.errorMessage());
					return false;
		} else if (MailBoxConstants.FOLDER_TYPE.equals(annotationDetails.type())
				&&	FolderType.findByName(String.valueOf(value)) == null) {
					errorMessage.append(annotationDetails.errorMessage());
					return false;
		} else if (MailBoxConstants.PROCESSOR_PROTOCOL.equals(annotationDetails.type())
				&&	Protocol.findByName(String.valueOf(value)) == null) {
					errorMessage.append(annotationDetails.errorMessage());
					return false;
		} else if (MailBoxConstants.CREDENTIAL_TYPE.equals(annotationDetails.type())
                &&  CredentialType.findByName(String.valueOf(value)) == null) {
            errorMessage.append(annotationDetails.errorMessage());
            return false;
        } else if (MailBoxConstants.CLUSTER_TYPE.equals(annotationDetails.type())
                && MailBoxConstants.LOWSECURE.equals(MailBoxUtil.CLUSTER_TYPE)
                && !MailBoxConstants.LOWSECURE.equals(value)) {
            errorMessage.append(annotationDetails.errorMessage());
            return false;
        }

		return true;
	}

    /**
     * Method to validate the url
     * 
     * @param value
     * @return boolean
     */
    private boolean isValidURL(Object value) {
        
        String[] customSchemes = { "sftp", "ftp", "ftps", "http", "https" };
        UrlValidator validator = new UrlValidator(customSchemes, UrlValidator.ALLOW_LOCAL_URLS);
        
        if (validator.isValid(value.toString())) {
            return true;
        }
        return false;
    }
}
