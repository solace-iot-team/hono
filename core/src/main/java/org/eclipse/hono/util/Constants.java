/*******************************************************************************
 * Copyright (c) 2016, 2019 Contributors to the Eclipse Foundation
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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.Record;
import org.eclipse.hono.auth.Activity;
import org.eclipse.hono.auth.Authorities;
import org.eclipse.hono.auth.HonoUser;
import org.eclipse.hono.auth.HonoUserAdapter;

import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonLink;

/**
 * Constants used throughout Hono.
 *
 */
public final class Constants {

    /**
     * Indicates that an AMQP request cannot be processed due to a perceived client error.
     */
    public static final Symbol AMQP_BAD_REQUEST= Symbol.valueOf("hono:bad-request");
    /**
     * Indicates that an AMQP connection is closed due to inactivity.
     */
    public static final Symbol AMQP_ERROR_INACTIVITY= Symbol.valueOf("hono:inactivity");

    /**
     * The default separator character for target addresses.
     */
    public static final String DEFAULT_PATH_SEPARATOR = "/";
    /**
     * The name of the default tenant.
     */
    public static final String DEFAULT_TENANT = "DEFAULT_TENANT";

    /**
     * The type of the AMQP protocol adapter.
     */
    public static final String PROTOCOL_ADAPTER_TYPE_AMQP= "hono-amqp";
    /**
     * The type of the CoAP protocol adapter.
     */
    public static final String PROTOCOL_ADAPTER_TYPE_COAP= "hono-coap";
    /**
     * The type of the HTTP protocol adapter.
     */
    public static final String PROTOCOL_ADAPTER_TYPE_HTTP= "hono-http";
    /**
     * The type of the Eclipse Kura protocol adapter.
     */
    public static final String PROTOCOL_ADAPTER_TYPE_KURA= "hono-kura-mqtt";
    /**
     * The type of the LoRaWAN protocol adapter.
     */
    public static final String PROTOCOL_ADAPTER_TYPE_LORA= "hono-lora";
    /**
     * The type of the MQTT protocol adapter.
     */
    public static final String PROTOCOL_ADAPTER_TYPE_MQTT= "hono-mqtt";
    /**
     * The type of the sigfox protocol adapter.
     */
    public static final String PROTOCOL_ADAPTER_TYPE_SIGFOX = "hono-sigfox";

    /**
     * The (short) name of the Auth Server component.
     */
    public static final String SERVICE_NAME_AUTH = "hono-auth";
    /**
     * The (short) name of the Device Connection service.
     */
    public static final String SERVICE_NAME_DEVICE_CONNECTION = "hono-device-connection";
    /**
     * The (short) name of the Device Registry component.
     */
    public static final String SERVICE_NAME_DEVICE_REGISTRY = "hono-registry";

    /**
     * The "QoS-Level" request header indicating the quality of service level supported by the HTTP Adapter.
     * The HTTP adapter supports QoS level AT_LEAST_ONCE when uploading telemetry messages.
     */
    public static final String HEADER_QOS_LEVEL = "QoS-Level";

    /**
     * The AMQP capability indicating support for validating registration assertions
     * issued by a Device Registration service.
     */
    public static final Symbol CAP_REG_ASSERTION_VALIDATION = Symbol.valueOf("hono-reg-assertion");

    /**
     * The key that an authenticated client's principal is stored under in a {@code ProtonConnection}'s
     * attachments.
     */
    public static final String KEY_CLIENT_PRINCIPAL = "CLIENT_PRINCIPAL";
    /**
     * The key that the (surrogate) ID of a connection is stored under in a {@code ProtonConnection}'s
     * and/or {@code ProtonLink}'s attachments.
     */
    public static final String KEY_CONNECTION_ID = "CONNECTION_ID";

    /**
     * The address that the ID of a connection that has been closed by a client is published to.
     */
    public static final String EVENT_BUS_ADDRESS_CONNECTION_CLOSED = "hono.connection.closed";

    /**
     * The vert.x event bus address that the ID of a tenant that timed out is published to.
     */
    public static final String EVENT_BUS_ADDRESS_TENANT_TIMED_OUT = "tenant.timeout";

    /**
     * The AMQP 1.0 port defined by IANA for unencrypted connections.
     */
    public static final int PORT_AMQP = 5672;
    /**
     * The AMQP 1.0 port defined by IANA for TLS encrypted connections.
     */
    public static final int PORT_AMQPS = 5671;
    /**
     * Default value for a port that is not explicitly configured.
     */
    public static final int PORT_UNCONFIGURED = -1;

    /**
     * The loopback device address.
     */
    public static final String LOOPBACK_DEVICE_ADDRESS = "127.0.0.1";

    /**
     * The qualifier to use for referring to AMQP based components.
     */
    public static final String QUALIFIER_AMQP = "amqp";
    /**
     * The qualifier to use for referring to components scoped to the AMQP 1.0 messaging network.
     */
    public static final String QUALIFIER_DOWNSTREAM = "downstream";
    /**
     * The qualifier to use for referring to the AMQP Messaging Network.
     */
    public static final String QUALIFIER_MESSAGING = "messaging";
    /**
     * The qualifier to use for referring to REST based components.
     */
    public static final String QUALIFIER_REST = "rest";

    /**
     * The subject name to use for anonymous clients.
     */
    public static final String SUBJECT_ANONYMOUS = "ANONYMOUS";

    /**
     * The field name of JSON payloads containing a device ID.
     */
    public static final String JSON_FIELD_DEVICE_ID = "device-id";
    /**
     * The field name of JSON payloads containing a tenant ID.
     */
    public static final String JSON_FIELD_TENANT_ID = "tenant-id";

    /**
     * The principal to use for anonymous clients.
     */
    public static final HonoUser PRINCIPAL_ANONYMOUS = new HonoUserAdapter() {

        private final Authorities authorities = new Authorities() {

            @Override
            public Map<String, Object> asMap() {
                return Collections.emptyMap();
            }

            @Override
            public boolean isAuthorized(final ResourceIdentifier resourceId, final Activity intent) {
                return false;
            }

            @Override
            public boolean isAuthorized(final ResourceIdentifier resourceId, final String operation) {
                return false;
            }
        };

        @Override
        public String getName() {
            return SUBJECT_ANONYMOUS;
        }

        @Override
        public Authorities getAuthorities() {
            return authorities;
        }
    };

    /**
     * The header name defined for setting the <em>time till disconnect</em> for device command readiness notification
     * events.
     */
    public static final String HEADER_TIME_TILL_DISCONNECT = "hono-ttd";
    /**
     * The header name defined for setting the <em>time-to-live</em> for the event messages.
     */
    public static final String HEADER_TIME_TO_LIVE = "hono-ttl";
    /**
     * The header name defined for setting the <em>request id</em> for device responses to a command.
     * This id is sent to the device and has to be used in replies to the command to correlate the original command with
     * the response.
     */
    public static final String HEADER_COMMAND_REQUEST_ID = "hono-cmd-req-id";

    /**
     * The header name defined for setting the <em>command</em> that is sent to the device.
     */
    public static final String HEADER_COMMAND = "hono-command";

    /**
     * The header name defined for setting the <em>status code</em> of a device respond to a command that was previously received by the device.
     */
    public static final String HEADER_COMMAND_RESPONSE_STATUS = "hono-cmd-status";

    private Constants() {
    }

    /**
     * Gets the principal representing an authenticated peer.
     * 
     * @param record The attachments to retrieve the principal from.
     * @return The principal representing the authenticated client or {@link Constants#PRINCIPAL_ANONYMOUS}
     *         if the client has not been authenticated or record is {@code null}.
     */
    public static HonoUser getClientPrincipal(final Record record) {

        if (record != null) {
            final HonoUser client = record.get(KEY_CLIENT_PRINCIPAL, HonoUser.class);
            return client != null ? client : PRINCIPAL_ANONYMOUS;
        } else {
            return PRINCIPAL_ANONYMOUS;
        }
    }

    /**
     * Gets the principal representing a connection's client.
     * 
     * @param con The connection to get the principal for.
     * @return The principal representing the authenticated client or {@link Constants#PRINCIPAL_ANONYMOUS}
     *         if the client has not been authenticated.
     * @throws NullPointerException if the connection is {@code null}.
     */
    public static HonoUser getClientPrincipal(final ProtonConnection con) {
        final Record attachments = Objects.requireNonNull(con).attachments();
        return getClientPrincipal(attachments);
    }

    /**
     * Gets the principal representing a connection's client.
     * 
     * @param con The connection to get the principal for.
     * @param principal The principal representing the authenticated client.
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    public static void setClientPrincipal(final ProtonConnection con, final HonoUser principal) {
        Objects.requireNonNull(principal);
        final Record attachments = Objects.requireNonNull(con).attachments();
        attachments.set(KEY_CLIENT_PRINCIPAL, HonoUser.class, principal);
    }

    /**
     * Copies properties from a connection's attachments to a link's attachments.
     * <p>
     * The properties copied are
     * <ul>
     * <li>{@link #KEY_CONNECTION_ID}</li>
     * </ul>
     * 
     * @param source The connection.
     * @param target The link.
     */
    public static void copyProperties(final ProtonConnection source, final ProtonLink<?> target) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        target.attachments().set(Constants.KEY_CONNECTION_ID, String.class, getConnectionId(source));
    }

    /**
     * Gets the (surrogate) identifier of the AMQP connection that a link is part of.
     * 
     * @param link The link to determine the connection id for.
     * @return The identifier retrieved from the link's <em>attachment</em> using key {@link #KEY_CONNECTION_ID}
     *         or {@code null} if the attachments do not contain a value for that a key.
     */
    public static String getConnectionId(final ProtonLink<?> link) {
        return link.attachments().get(KEY_CONNECTION_ID, String.class);
    }

    /**
     * Gets the (surrogate) identifier of an AMQP connection.
     * 
     * @param connection The connection to determine the connection id for.
     * @return The identifier retrieved from the connection's <em>attachments</em> using key {@link #KEY_CONNECTION_ID}
     *         or {@code null} if the attachments do not contain a value for that a key.
     */
    public static String getConnectionId(final ProtonConnection connection) {
        return connection.attachments().get(KEY_CONNECTION_ID, String.class);
    }

    /**
     * Sets the (surrogate) identifier of an AMQP connection.
     * <p>
     * The identifier will be added to the connection's <em>attachments</em> under key
     * {@link #KEY_CONNECTION_ID}.
     * 
     * @param connection The connection to set id for.
     * @param id The identifier to set.
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    public static void setConnectionId(final ProtonConnection connection, final String id) {
        Objects.requireNonNull(connection).attachments().set(Constants.KEY_CONNECTION_ID, String.class, Objects.requireNonNull(id));

    }

    /**
     * Checks if a given tenant identifier is the {@code DEFAULT_TENANT}.
     * 
     * @param tenantId The identifier to check.
     * @return {@code true} if the given identifier is equal to {@link #DEFAULT_TENANT}.
     */
    public static boolean isDefaultTenant(final String tenantId) {
        return DEFAULT_TENANT.equals(tenantId);
    }
}
