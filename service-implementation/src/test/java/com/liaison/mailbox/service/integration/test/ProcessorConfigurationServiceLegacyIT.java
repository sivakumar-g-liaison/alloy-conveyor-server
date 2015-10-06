/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.integration.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.MailBoxConfigurationService;
import com.liaison.mailbox.service.core.ProcessorConfigurationService;
import com.liaison.mailbox.service.dto.configuration.MailBoxDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.request.AddProcessorToMailboxRequestDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddMailBoxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.AddProcessorToMailboxResponseDTO;
import com.liaison.mailbox.service.dto.configuration.response.GetProcessorResponseDTO;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 *
 * @author OFS
 *
 */
public class ProcessorConfigurationServiceLegacyIT extends BaseServiceTest {

	private String serviceInstanceId = "5D9C3B487184426E9F9629EFEE7C5913";
    private String aclManifest = "H4sIAAAAAAAAAO1YbW/aMBD+K5U/TqRNokACn5aW0EUroaLRJrWqIpMckVfHjpwQlVb973NeKKBWXcdWEVV8QbLvfPfwnO8ewyMCVgDlKaDBIwoF4ByiofxAA6SrWldR+4qq+7o6MPoD1Tg2e8Y16qCY8hmmboQGbEFpB6VYAMs31oKHkGXrjUUGolyhguEYC/wLs6+UYJJxdhxBgWqPERFZ7uEENo9d4O29BuTp0k5TSkKcE85q25NMTHE+5yJBg5vH50V9Gp3rMo3gFE5xBpEdlgjPOMvlVuUe8QQT9uwcDJ0fgev5wWR6Lg/WVn9ZMoXklu2517bvTrxnm8tyEAzTlxHGE8/97kyb9DKZNNrDseuh25IrUhAKMVQgBGR8IcIywJfSv1k2eaeQ5dOVx9pafgu4z6WD5KsgISgzwe9ASJcUREKyrOJIhi8wXaxir01N9J/fXN+5cK989HT71PlnLGXxEizrDYm8HPvFEuuyQnTG7xuC9ovmDpZKW5iJcI4lmDTd93WpZwqUTSRbIoOaoD2DSihNlZCSvZepAlJe3v/JyEnI2ZzEJ7Hgi3QHUCAEFwrjOZmvBvHHYGNypGZ7B1hB3FKJIST8aCJizMhDFV7bRSlGPVMdmYap2n1dNcyRZmjGmTOybN1ynJ7R3dCNAkDgoFq9JR3bo7ehciEqiLvw+N5RXvJxuTa9TWjn/dxvuF4600A3Jmd+oKraDiUb1zoQlDWIxepO/HXNXg+zKtMr1sCOEsI+teC3SdbaJfhtegp9ZsF/2e6nE8f1dnq/1ydfebs3ho95wLfr3h6my6Gf29XPrh/4Ekgof8LrvaMrYBGIrRfYTmJOGpEec0bkRVdCs6tapmYq4XxmKIY615SZZYZKV7Ow1Y90sGawMRH+COog/gfxbwUzn3tYyP5iMjFEV3Xdh5CWvcjCZfU/n2x8mfqm9PwNJYKk5vgUAAA=";
    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
    	System.setProperty("com.liaison.secure.properties.path", "invalid");
        System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
        System.setProperty("archaius.deployment.environment", "test");
        InitInitialDualDBContext.init();
    }

    /**
     * Method constructs Processor with valid data.
     * @throws MailBoxConfigurationServicesException
     *
     * @throws Exception
     */
    @Test
	public void testCreateandReadProcessorUsingPguid() throws Exception {

        // Adding the mailbox
        AddMailboxRequestDTO requestDTO = new AddMailboxRequestDTO();
        MailBoxDTO mbxDTO = constructDummyMailBoxDTO(System.currentTimeMillis(), true);
        requestDTO.setMailBox(mbxDTO);

        MailBoxConfigurationService service = new MailBoxConfigurationService();
        AddMailBoxResponseDTO response = service.createMailBox(requestDTO, serviceInstanceId, aclManifest);

        Assert.assertEquals(SUCCESS, response.getResponse().getStatus());

        // Adding the processor
        AddProcessorToMailboxRequestDTO procRequestDTO = MailBoxUtil.unmarshalFromJSON(ServiceUtils.readFileFromClassPath("requests/processor/create_processor_legacy.json"), AddProcessorToMailboxRequestDTO.class);
        procRequestDTO.getProcessorLegacy().setLinkedMailboxId(response.getMailBox().getGuid());
        ProcessorConfigurationService procService = new ProcessorConfigurationService();
        AddProcessorToMailboxResponseDTO procResponseDTO = procService.createProcessor(response.getMailBox().getGuid(), procRequestDTO, serviceInstanceId);

        // Get the processor
        GetProcessorResponseDTO procGetResponseDTO = procService.getProcessor(response.getMailBox().getGuid(), procResponseDTO.getProcessor().getGuId());

        // Assertion
        Assert.assertEquals(SUCCESS, procGetResponseDTO.getResponse().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getName(), procGetResponseDTO.getProcessor().getName());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getStatus(), procGetResponseDTO.getProcessor().getStatus());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getType(), procGetResponseDTO.getProcessor().getType());
        Assert.assertEquals(procRequestDTO.getProcessorLegacy().getProtocol(), procGetResponseDTO.getProcessor().getProtocol());

    }

}
