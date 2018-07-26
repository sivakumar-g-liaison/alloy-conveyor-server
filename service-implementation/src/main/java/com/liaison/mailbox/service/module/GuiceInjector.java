/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.module;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceInjector {

    private static Injector injector;

    // Lazy initialization of the injector, only init when required.
    public static Injector getInjector() {
        if (injector == null)
        {
            injector = Guice.createInjector(new RelayModule());
        }

        return injector;
    }
}
