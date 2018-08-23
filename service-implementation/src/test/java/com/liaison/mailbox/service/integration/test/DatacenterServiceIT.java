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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.liaison.mailbox.rtdm.dao.DatacenterDAO;
import com.liaison.mailbox.rtdm.dao.DatacenterDAOBase;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.core.DatacenterService;

/**
 * Test class to test data center service
 *
 */
public class DatacenterServiceIT extends BaseServiceTest {

    private static final String AT4 = "at4";
    private static final String PX1 = "px1";

    @Test(enabled = false)
    public void testCreateDatacenter() {

        DatacenterService service = new DatacenterService();
        Map<String, String> datacenterMap = new HashMap<String, String>();
        datacenterMap.put(AT4, AT4);
        Response response = service.createDatacenter(datacenterMap);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        
        DatacenterDAO dao = new DatacenterDAOBase();
        String processDc = dao.findProcessingDatacenterByName(AT4);

        Assert.assertNotNull(processDc);
        Assert.assertEquals(processDc, AT4);
    }

    @Test(enabled = false)
    public void testCreateDatacenterInvalid() {

        DatacenterService service = new DatacenterService();
        Map<String, String> datacenterMap = new HashMap<String, String>();
        datacenterMap.put("at2", AT4);
        Response response = service.createDatacenter(datacenterMap);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(enabled = false)
    public void testUpdateDatacenter() {

        DatacenterService service = new DatacenterService();
        Map<String, String> datacenterMap = new HashMap<String, String>();
        datacenterMap.put(AT4, PX1);
        Response response = service.updateDatacenter(datacenterMap);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        
        DatacenterDAO dao = new DatacenterDAOBase();
        String processDc = dao.findProcessingDatacenterByName(AT4);

        Assert.assertNotNull(processDc);
        Assert.assertEquals(processDc, PX1);
    }
}
