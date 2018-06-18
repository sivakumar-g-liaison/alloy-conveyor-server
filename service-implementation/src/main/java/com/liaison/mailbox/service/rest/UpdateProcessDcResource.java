/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
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
import com.liaison.commons.audit.hipaa.HIPAAAdminSimplification201303;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.commons.exception.LiaisonRuntimeException;
import com.liaison.commons.jaxb.JAXBUtility;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.framework.AppConfigurationResource;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.rtdm.dao.StagedFileDAOBase;
import com.liaison.mailbox.service.core.ProcessorAffinityService;
import com.liaison.mailbox.service.dto.configuration.request.UpdateProcessDCRequestDTO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

/**
 * This is the gateway for update process dc.
 * 
 */
@AppConfigurationResource
@Path("process/updateprocessdc")
@Api(value = "process/updateprocessdc", description = "Gateway for the update the process_dc.")
public class UpdateProcessDcResource extends AuditedResource {

	/**
	 * REST method to update the process_dc column of PROCESSOR table
	 * 
	 * @param request
	 * @return
	 */
	@POST
	@ApiOperation(value = "update the process_dc", notes = "update the process_dc", position = 1)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
	public Response updateProcessDc(@Context final HttpServletRequest request) {
	    
	    // create the worker delegate to perform the business logic
	    AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
	        @Override
	        public Object call() {
	            
                // updates datacenter of processors
                new ProcessorAffinityService().updateProcessDc();
                return marshalResponse(200, MediaType.TEXT_PLAIN, "Success");
	        }
	    };
	    worker.actionLabel = "UpdateProcessDcResource.updateProcessDc()";
	    
	    // hand the delegate to the framework for calling
	    return process(request, worker);
	}

    /**
     * REST method to update the process_dc column of PROCESSOR(REMOTEDOWNLOADER) and StagedFile table
     * 
     * @param request
     * @return
     */
    @PUT
    @ApiOperation(value = "update the process_dc", notes = "update the process_dc", position = 1)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses({@ApiResponse(code = 500, message = "Unexpected Service failure.")})
    public Response updateDownloaderProcessDc(@Context final HttpServletRequest request) {
        
        // create the worker delegate to perform the business logic
        AbstractResourceDelegate<Object> worker = new AbstractResourceDelegate<Object>() {
            @Override
            public Object call() {
                
                // updates process_dc of downloder processor and staged file
                try {
                    String requestString = getRequestBody(request);
                    List<Object> processDcList = LiaisonArchaiusConfiguration.getInstance().getList(MailBoxConstants.PROCESS_DC_LIST);
                    UpdateProcessDCRequestDTO updateProcessorDCRequestDTO = JAXBUtility.unmarshalFromJSON(requestString, UpdateProcessDCRequestDTO.class);
                    if (processDcList.contains(updateProcessorDCRequestDTO.getExistingProcessDC())
                    		&& processDcList.contains(updateProcessorDCRequestDTO.getNewProcessDC())) {
                    	new ProcessorAffinityService().updateDownloaderProcessDc(updateProcessorDCRequestDTO.getExistingProcessDC(),updateProcessorDCRequestDTO.getNewProcessDC());
                    	new StagedFileDAOBase().updateStagedFileProcessDC(updateProcessorDCRequestDTO.getExistingProcessDC(),updateProcessorDCRequestDTO.getNewProcessDC());
                    	return marshalResponse(Response.Status.OK.getStatusCode(), MediaType.TEXT_PLAIN, "Success");
                    } else {
                    	return marshalResponse(Response.Status.BAD_REQUEST.getStatusCode(), MediaType.TEXT_PLAIN, "Invalid Process Dc value");
                    }
                } catch (IOException | JAXBException e) {
                    throw new LiaisonRuntimeException("Unable to Update the process_DC " + e.getMessage(), e);
                }
            }
        };
        worker.actionLabel = "UpdateProcessDcResource.updateDownloaderProcessDc()";
        
        // hand the delegate to the framework for calling
        return process(request, worker);
    }

	@Override
	protected AuditStatement getInitialAuditStatement(String actionLabel) {
	    return new DefaultAuditStatement(Status.ATTEMPT, actionLabel, PCIV20Requirement.PCI10_2_5,
	            PCIV20Requirement.PCI10_2_2, HIPAAAdminSimplification201303.HIPAA_AS_C_164_308_5iiD,
	            HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_a2iv,
	            HIPAAAdminSimplification201303.HIPAA_AS_C_164_312_c2d);
	}
}
