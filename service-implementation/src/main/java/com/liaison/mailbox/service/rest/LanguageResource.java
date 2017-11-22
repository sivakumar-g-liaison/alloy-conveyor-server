/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.rest;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.exception.LiaisonAuditableRuntimeException;
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TreeMap;

/**
 * This class contains methods to determine the language settings of the user.
 */
@Api(value = "config/language/userLocale", description = "Language related methods")
@Path("config/language/userLocale")
public class LanguageResource extends AuditedResource {

    private final static Logger logger = LogManager.getLogger(LanguageResource.class);
    private final static String DEFAULT_PATTERN = "dd/MM/yyyy";
    private final static String DEFAULT_LOCALE = "en-US";
    private final static String PATTERN_KEY = "dateRangePattern";
    private final static String LOCALE_KEY = "locale";

    @GET
    @ApiOperation(value = "This method parses the users language locale code from the request and returns the pattern " +
            "with locale.")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserLocale(@Context final HttpServletRequest request) {
    	
        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {

            @Override
            public Object call() {

                TreeMap<String, String> formattingData = new TreeMap<>();

                try {
                	
                    Locale locale = request.getLocale();
                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                    SimpleDateFormat simpleFormat = (SimpleDateFormat) dateFormat;
                    String tempPattern = simpleFormat.toPattern();
                    String[] newPattern = null;
                    String separator = "";
                    String returnPattern = "";
                    boolean error = false;

                    if (tempPattern.contains(".")) {
                        newPattern = tempPattern.split("\\.");
                        separator = ".";
                    } else if (tempPattern.contains(":")) {
                        newPattern = tempPattern.split(":");
                        separator = ":";
                    } else if (tempPattern.contains("/")) {
                        newPattern = tempPattern.split("/");
                        separator = "/";
                    } else if (tempPattern.contains("-")) {
                        newPattern = tempPattern.split("-");
                        separator = "-";
                    }

                    for (int i = 0; i < newPattern.length; i++) {
                        if (newPattern[i].contains("d")) {
                            newPattern[i] = "dd";
                        } else if (newPattern[i].contains("M")) {
                            newPattern[i] = "MM";
                        } else if (newPattern[i].contains("y")) {
                            newPattern[i] = "yyyy";
                        }
                        else {
                            error = true;
                        }
                        returnPattern += newPattern[i];

                        if (i < newPattern.length - 1) {
                            returnPattern += separator;
                        }
                    }

                    if(error){
                        returnPattern = DEFAULT_PATTERN;
                    }

                    formattingData.put(PATTERN_KEY, returnPattern);
                    formattingData.put(LOCALE_KEY, locale.toLanguageTag());

                } catch (Exception e) {
                    logger.error(e);
                    formattingData.clear();
                    formattingData.put(PATTERN_KEY, DEFAULT_PATTERN);
                    formattingData.put(LOCALE_KEY, DEFAULT_LOCALE);
                }

                return marshalResponse(200, MediaType.APPLICATION_JSON, formattingData);
            }
        };
        worker.actionLabel = "LanguageResource.getUserLocale()";

        // hand the delegate to the framework for calling
        try {
            return handleAuditedServiceRequest(request, worker);
        } catch (LiaisonAuditableRuntimeException e) {
            if (!StringUtils.isEmpty(e.getResponseStatus().getStatusCode() + "")) {
                return marshalResponse(e.getResponseStatus().getStatusCode(), MediaType.TEXT_PLAIN, e.getMessage());
            }
            return marshalResponse(500, MediaType.TEXT_PLAIN, e.getMessage());
        }
    }

    // abstract method implementations
    // =====================================================================

    /**
     * Initial audit statement common to exports service requests.
     *
     * @param actionLabel
     * @return
     */
    @Override
    protected AuditStatement getInitialAuditStatement(String actionLabel) {
        return new DefaultAuditStatement(
                        Status.ATTEMPT,
                        actionLabel,
                        PCIV20Requirement.PCI10_2_5,
                        PCIV20Requirement.PCI10_2_2,
                        HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
                        HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
                        HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
    }

}
