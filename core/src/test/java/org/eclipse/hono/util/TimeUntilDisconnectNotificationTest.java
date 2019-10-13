/*******************************************************************************
 * Copyright (c) 2016, 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/


package org.eclipse.hono.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.apache.qpid.proton.message.Message;
import org.junit.Test;

import io.vertx.proton.ProtonHelper;

/**
 * Tests TimeUntilDisconnectNotification.
 */
public class TimeUntilDisconnectNotificationTest {

    /**
     * Verifies that the notification is constructed if the ttd value is set to a positive number of seconds.
     */
    @Test
    public void testNotificationIsConstructedIfTtdIsSetToPositiveValue() {

        final Message msg = createTestMessage();
        MessageHelper.addProperty(msg, MessageHelper.APP_PROPERTY_DEVICE_TTD, Integer.valueOf(10));
        final Optional<TimeUntilDisconnectNotification> ttdNotificationOpt = TimeUntilDisconnectNotification.fromMessage(msg);
        assertTrue(ttdNotificationOpt.isPresent());

        final TimeUntilDisconnectNotification notification = ttdNotificationOpt.get();
        assertNotificationProperties(notification);
        assertEquals(Integer.valueOf(10), notification.getTtd());
    }

    /**
     * Verifies that the notification is constructed if the ttd value is set to a positive number of seconds.
     */
    @Test
    public void testNotificationIsConstructedIfTtdIsSetToUnlimited() {

        final Message msg = createTestMessage();
        MessageHelper.addProperty(msg, MessageHelper.APP_PROPERTY_DEVICE_TTD, MessageHelper.TTD_VALUE_UNLIMITED);
        final Optional<TimeUntilDisconnectNotification> ttdNotificationOpt = TimeUntilDisconnectNotification.fromMessage(msg);
        assertTrue(ttdNotificationOpt.isPresent());
        final TimeUntilDisconnectNotification notification = ttdNotificationOpt.get();

        assertNotificationProperties(notification);
        assertEquals(notification.getTtd(), Integer.valueOf(MessageHelper.TTD_VALUE_UNLIMITED));
    }

    private void assertNotificationProperties(final TimeUntilDisconnectNotification notification) {
        assertTrue(notification.getMillisecondsUntilExpiry() > 0);
        assertTrue(Constants.DEFAULT_TENANT.equals(notification.getTenantId()));
        assertTrue("4711".equals(notification.getDeviceId()));
    }

    private Message createTestMessage() {
        final Message msg = ProtonHelper.message();
        MessageHelper.setCreationTime(msg);
        MessageHelper.addDeviceId(msg, "4711");
        MessageHelper.addTenantId(msg, Constants.DEFAULT_TENANT);
        return msg;
    }

}
