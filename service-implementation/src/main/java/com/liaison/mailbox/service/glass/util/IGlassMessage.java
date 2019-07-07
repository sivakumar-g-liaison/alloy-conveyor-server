/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.glass.util;

import com.google.inject.ImplementedBy;
import com.liaison.commons.util.StatusLogger;

@ImplementedBy(GlassMessage.class)
interface IGlassMessage extends StatusLogger {
}
