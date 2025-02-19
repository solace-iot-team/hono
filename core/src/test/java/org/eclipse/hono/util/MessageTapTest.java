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

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.junit.Test;

import io.vertx.proton.ProtonHelper;

/**
 * Tests MessageTap.
 */
public class MessageTapTest {

    /**
     * Verifies that the message tap calls message consumer.
     */
    @Test
    public void testTapCallsMessageConsumer() {

        final Message msg = createTestMessage("", "bum/lux");

        MessageHelper.addTimeUntilDisconnect(msg, 60);

        final AtomicBoolean consumerCalled = new AtomicBoolean(false);

        final Consumer<Message> messageConsumer = MessageTap.getConsumer(
                message -> consumerCalled.set(true),
                n -> {});
        messageConsumer.accept(msg);
        assertTrue(consumerCalled.get());
    }

    /**
     * Verifies that the message tap calls the message AND the notification consumer
     * if a non empty message indicates command readiness.
     */
    @Test
    public void testTapCallsNotificationDeviceReadyToDeliverConsumer() {

        final Message msg = createTestMessage("", "bum/lux");

        MessageHelper.addTimeUntilDisconnect(msg, 6000);

        final AtomicBoolean messageConsumerCalled = new AtomicBoolean(false);
        final AtomicBoolean notificationConsumerCalled = new AtomicBoolean(false);

        final Consumer<Message> messageConsumer = MessageTap.getConsumer(
                m -> messageConsumerCalled.set(true),
                message -> notificationConsumerCalled.set(true)
        );
        messageConsumer.accept(msg);

        assertTrue(notificationConsumerCalled.get());
        assertTrue(messageConsumerCalled.get());
    }

    /**
     * Verifies that the message tap calls the message AND the notification consumer
     * if an empty message indicates command readiness.
     */
    @Test
    public void testTapCallsNotificationDeviceReadyToDeliverConsumerForEmptyEvent() {

        final Message msg = createTestMessageWithoutBody("bum/lux");

        MessageHelper.addTimeUntilDisconnect(msg, 6000);

        final AtomicBoolean messageConsumerCalled = new AtomicBoolean(false);
        final AtomicBoolean notificationConsumerCalled = new AtomicBoolean(false);

        final Consumer<Message> messageConsumer = MessageTap.getConsumer(
                m -> messageConsumerCalled.set(true),
                message -> notificationConsumerCalled.set(true)
        );
        messageConsumer.accept(msg);

        assertTrue(notificationConsumerCalled.get());
        assertTrue(messageConsumerCalled.get());
    }

    private Message createTestMessage(final String contentType, final String payload) {
        final Message msg = createTestMessageWithoutBody(payload);
        if (payload != null) {
            msg.setBody(new Data(new Binary(payload.getBytes(StandardCharsets.UTF_8))));
        }
        msg.setContentType(contentType);
        MessageHelper.setJsonPayload(msg, payload);
        return msg;
    }

    private Message createTestMessageWithoutBody(final String payload) {
        final Message msg = ProtonHelper.message();
        MessageHelper.setCreationTime(msg);
        MessageHelper.addDeviceId(msg, "4711");
        MessageHelper.addTenantId(msg, Constants.DEFAULT_TENANT);
        return msg;
    }

}
