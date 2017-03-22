/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.services.enumclass.unit.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.liaison.mailbox.enums.DeploymentType;

public class DeploymentTypeTest {

    String RELAY = "RELAY";
    String LOWSECURE_RELAY = "LOWSECURE-RELAY";
    String CONVEYOR = "CONVEYOR";

    @Test
    public void testDeploymentType() {

        Assert.assertEquals(RELAY, DeploymentType.RELAY.getValue());
        Assert.assertEquals(LOWSECURE_RELAY, DeploymentType.LOWSECURE_RELAY.getValue());
        Assert.assertEquals(CONVEYOR, DeploymentType.CONVEYOR.getValue());
    }

    @Test
    public void testDeploymentType_NotNull() {

        Assert.assertNotNull(DeploymentType.RELAY.getValue());
        Assert.assertNotNull(DeploymentType.LOWSECURE_RELAY.getValue());
        Assert.assertNotNull(DeploymentType.CONVEYOR.getValue());
    }
}
