/*******************************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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

package org.eclipse.hono.tests.jms;

import java.net.HttpURLConnection;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Optional;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.qpid.jms.JmsQueue;
import org.eclipse.hono.client.ConnectionLifecycle;
import org.eclipse.hono.client.DisconnectListener;
import org.eclipse.hono.client.ReconnectListener;
import org.eclipse.hono.client.ServerErrorException;
import org.eclipse.hono.config.ClientConfigProperties;
import org.eclipse.hono.util.MessageHelper;
import org.eclipse.hono.util.Strings;
import org.eclipse.hono.util.TelemetryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * A helper for implementing integration tests for Hono using Apache Qpid JMS Client.
 *
 */
public class JmsBasedHonoConnection implements ConnectionLifecycle<JmsBasedHonoConnection> {

    private static final String AMQP_URI_PATTERN = "amqp://%s:%d?%s";
    private static final Logger LOG = LoggerFactory.getLogger(JmsBasedHonoConnection.class);

    private final ClientConfigProperties clientConfig;

    private Context ctx;
    private Connection connection;
    private Session session;

    private JmsBasedHonoConnection(final ClientConfigProperties clientConfig) {
        this.clientConfig = clientConfig;
    }

    /**
     * Creates a new connection to a server.
     * 
     * @param clientConfig The configuration properties for connecting to the server.
     * @return The connected client.
     */
    public static JmsBasedHonoConnection newConnection(final ClientConfigProperties clientConfig) {

        Objects.requireNonNull(clientConfig);
        return new JmsBasedHonoConnection(clientConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<JmsBasedHonoConnection> connect() {

        final Future<JmsBasedHonoConnection> result = Future.future();
        try {
            createContext();
            createSession();
            result.complete(this);
        } catch (JMSException | NamingException e) {
            result.fail(e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDisconnectListener(final DisconnectListener<JmsBasedHonoConnection> listener) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addReconnectListener(final ReconnectListener<JmsBasedHonoConnection> listener) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Void> isConnected() {

        return Optional.ofNullable(connection)
                .map(c -> Future.succeededFuture((Void) null))
                .orElse(Future.failedFuture(new ServerErrorException(HttpURLConnection.HTTP_UNAVAILABLE)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        disconnect(Future.future());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect(final Handler<AsyncResult<Void>> completionHandler) {
        try {
            connection.close();
            completionHandler.handle(Future.succeededFuture());
        } catch (JMSException e) {
            completionHandler.handle(Future.failedFuture(e));
        }
    }

    private JmsBasedHonoConnection createSession() throws JMSException {

        try {
            final ConnectionFactory cf = (ConnectionFactory) ctx.lookup("server");
            connection = cf.createConnection(clientConfig.getUsername(), clientConfig.getPassword());
            connection.setExceptionListener(new MyExceptionListener());
            if (!Strings.isNullOrEmpty(clientConfig.getName())) {
                connection.setClientID(clientConfig.getName());
            }
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            return this;

        } catch (NamingException e) {
            throw new IllegalStateException("initial context does not contain entry for server");
        }
    }

    /**
     * Creates an anonymous message producer.
     * 
     * @return The producer.
     * @throws JMSException if the producer could not be created.
     */
    public MessageProducer createAnonymousProducer() throws JMSException {
        return createProducer((Destination) null);
    }

    /**
     * Creates a message producer for a destination.
     * 
     * @param destination The destination. 
     * @return The producer.
     * @throws JMSException if the producer could not be created.
     */
    public MessageProducer createProducer(final String destination) throws JMSException {
        return createProducer(getDestination(destination));
    }

    /**
     * Creates a message producer for a destination.
     * 
     * @param destination The destination. 
     * @return The producer.
     * @throws JMSException if the producer could not be created.
     */
    public MessageProducer createProducer(final Destination destination) throws JMSException {
        if (session == null) {
            throw new IllegalStateException("No JMS session");
        } else {
            LOG.info("creating producer [{}]", destination);
            return session.createProducer(destination);
        }
    }

    /**
     * Creates a consumer for telemetry messages.
     * 
     * @param tenantId The tenant to create the consumer for.
     * @return The consumer.
     * @throws JMSException if the consumer could not be created.
     */
    public MessageConsumer createTelemetryConsumer(final String tenantId) throws JMSException {
        return createConsumer(getDestination(TelemetryConstants.TELEMETRY_ENDPOINT, tenantId));
    }

    /**
     * Creates a message consumer for a destination.
     * 
     * @param destination The destination. 
     * @return The consumer.
     * @throws JMSException if the consumer could not be created.
     */
    public MessageConsumer createConsumer(final String destination) throws JMSException {
        return createConsumer(getDestination(destination));
    }

    /**
     * Creates a message consumer for a destination.
     * 
     * @param destination The destination. 
     * @return The consumer.
     * @throws JMSException if the consumer could not be created.
     */
    public MessageConsumer createConsumer(final Destination destination) throws JMSException {
        if (session == null) {
            throw new IllegalStateException("No JMS session");
        } else {
            LOG.info("creating consumer [{}]", destination);
            return session.createConsumer(destination);
        }
    }

    private void createContext() throws NamingException {

        final String params = String.format("jms.prefetchPolicy.queuePrefetch=%d&amqp.maxFrameSize=16384", clientConfig.getInitialCredits());
        final String serverUri = String.format(
                AMQP_URI_PATTERN,
                clientConfig.getHost(),
                clientConfig.getPort(),
                params);

        final Hashtable<Object, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        env.put("connectionfactory.server", serverUri);

        ctx = new InitialContext(env);
    }

    /**
     * Creates a new message for a payload and device ID.
     * <p>
     * The returned message contains the UTF-8 encoding of the given string in its payload.
     * The device ID is put into property {@link MessageHelper#APP_PROPERTY_DEVICE_ID}.
     * 
     * @param payload The payload.
     * @param deviceId The identifier of the device that the data is reported for.
     * @return The message.
     * @throws JMSException if the message could not be created.
     * @throws IllegalStateException if the connection is not established.
     */
    public BytesMessage newMessage(final String payload, final String deviceId) throws JMSException {
        if (session == null) {
            throw new IllegalStateException("connection not established");
        }
        final BytesMessage message = session.createBytesMessage();
        message.setStringProperty(MessageHelper.APP_PROPERTY_DEVICE_ID, deviceId);
        message.writeUTF(payload);
        return message;
    }

    /**
     * Creates a new destination for an address.
     * 
     * @param address The address to create the destination for.
     * @return The destination.
     */
    public static Destination getDestination(final String address) {
        return new JmsQueue(address);
    }

    /**
     * Creates a new destination for an endpoint and tenant.
     * 
     * @param endpoint The endpoint.
     * @param tenantId The tenant identifier.
     * @return The destination.
     */
    public static Destination getDestination(final String endpoint, final String tenantId) {
        return getDestination(String.format("%s/%s", endpoint, tenantId));
    }

    /**
     * An exception listener that simply logs all exceptions.
     *
     */
    static class MyExceptionListener implements ExceptionListener {

        private static final Logger LOGGER = LoggerFactory.getLogger(MyExceptionListener.class);
        @Override
        public void onException(final JMSException exception) {
            LOGGER.error("Connection ExceptionListener fired.", exception);
        }
    }
}