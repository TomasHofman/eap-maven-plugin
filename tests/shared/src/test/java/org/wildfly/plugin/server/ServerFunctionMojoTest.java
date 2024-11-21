/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.plugin.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.plugin.tests.AbstractWildFlyMojoTest;
import org.wildfly.plugin.tests.TestEnvironment;
import org.wildfly.plugin.tools.server.ServerManager;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Ignore
public class ServerFunctionMojoTest extends AbstractWildFlyMojoTest {

    @After
    public void shutdown() throws Exception {
        // Ensure the server is shutdown
        try (ModelControllerClient client = createClient()) {
            ServerManager serverManager = ServerManager.builder()
                    .client(client)
                    .standalone();
            if (serverManager.isRunning()) {
                serverManager.shutdown();
            }
            serverManager = ServerManager.builder()
                    .client(client)
                    .domain();
            if (serverManager.isRunning()) {
                serverManager.shutdown();
            }
        }
    }

    @Test
    public void testStartStandalone() throws Exception {
        final StartMojo mojo = getStartMojo();
        mojo.execute();
        try (ModelControllerClient client = createClient()) {
            final ServerManager serverManager = ServerManager.builder().client(client).standalone();
            // Verify the server is running
            Assert.assertTrue("The start goal did not start the server.", serverManager.isRunning());
            Assert.assertFalse("This should be a standalone server, but found a domain server.",
                    serverManager.containerDescription().isDomain());
        }
    }

    @Test
    public void testShutdownStandalone() throws Exception {
        // Start up the server and ensure it's running
        final StartMojo startMojo = getStartMojo();
        startMojo.execute();
        try (ModelControllerClient client = createClient()) {
            final ServerManager serverManager = ServerManager.builder().client(client).standalone();
            // Verify the server is running
            Assert.assertTrue("The start goal did not start the server.", serverManager.isRunning());
        }

        // Look up the stop mojo and attempt to stop
        final ShutdownMojo stopMojo = lookupMojoAndVerify("shutdown", "shutdown-pom.xml");
        stopMojo.execute();
        try (ModelControllerClient client = createClient()) {
            final ServerManager serverManager = ServerManager.builder().client(client).standalone();
            // Verify the server is running
            Assert.assertFalse("The start goal did not start the server.", serverManager.isRunning());
        }
    }

    @Test
    public void testStartAndAddUserStandalone() throws Exception {
        final StartMojo mojo = getStartMojo();
        // The MOJO lookup replaces a configured add-users configuration with a default value so we need to manually
        // create and insert the field for testing
        setValue(mojo, "addUser", createAddUsers("admin:admin.1234:admin", "user:user.1234:user,mgmt::true"));
        mojo.execute();
        try (ModelControllerClient client = createClient()) {
            final ServerManager serverManager = ServerManager.builder().client(client).standalone();
            // Verify the server is running
            Assert.assertTrue("The start goal did not start the server.", serverManager.isRunning());
        }

        final Path standaloneConfigDir = TestEnvironment.WILDFLY_HOME.resolve("standalone").resolve("configuration");

        // Check the management users
        final Path mgmtUsers = standaloneConfigDir.resolve("mgmt-users.properties");
        Assert.assertTrue("File " + mgmtUsers + " does not exist", Files.exists(mgmtUsers));
        Assert.assertTrue("User admin was not added to the mgmt-user.properties file", fileContains(mgmtUsers, "admin="));

        // Check the management users
        final Path mgmtGroups = standaloneConfigDir.resolve("mgmt-groups.properties");
        Assert.assertTrue("File " + mgmtGroups + " does not exist", Files.exists(mgmtGroups));
        Assert.assertTrue("User admin was not added to the mgmt-groups.properties file",
                fileContains(mgmtGroups, "admin=admin"));

        // Check the application users
        final Path appUsers = standaloneConfigDir.resolve("application-users.properties");
        Assert.assertTrue("File " + appUsers + " does not exist", Files.exists(appUsers));
        Assert.assertTrue("User user was not added to the application-user.properties file", fileContains(appUsers, "user="));

        // Check the management users
        final Path appGroups = standaloneConfigDir.resolve("application-roles.properties");
        Assert.assertTrue("File " + appGroups + " does not exist", Files.exists(appGroups));
        Assert.assertTrue("User user was not added to the application-roles.properties file",
                fileContains(appGroups, "user=user,mgmt"));
    }

    @Test
    public void testStartDomain() throws Exception {
        final StartMojo mojo = getStartMojo("start-domain-pom.xml");
        mojo.execute();
        try (DomainClient client = DomainClient.Factory.create(createClient())) {
            final ServerManager serverManager = ServerManager.builder().client(client).domain();
            // Verify the server is running
            Assert.assertTrue("The start goal did not start the server.", serverManager.isRunning());
            Assert.assertTrue("This should be a domain server server, but found a standalone server.",
                    serverManager.containerDescription().isDomain());
        }
    }

    @Test
    public void testShutdownDomain() throws Exception {
        // Start up the server and ensure it's running
        final StartMojo startMojo = getStartMojo("start-domain-pom.xml");
        startMojo.execute();
        try (DomainClient client = DomainClient.Factory.create(createClient())) {
            final ServerManager serverManager = ServerManager.builder().client(client).domain();
            // Verify the server is running
            Assert.assertTrue("The start goal did not start the server.", serverManager.isRunning());
        }

        // Look up the stop mojo and attempt to stop
        final ShutdownMojo stopMojo = lookupMojoAndVerify("shutdown", "shutdown-pom.xml");
        stopMojo.execute();
        try (DomainClient client = DomainClient.Factory.create(createClient())) {
            final ServerManager serverManager = ServerManager.builder().client(client).domain();
            // Verify the server is running
            Assert.assertFalse("The start goal did not start the server.", serverManager.isRunning());
        }
    }

    private StartMojo getStartMojo() throws Exception {
        return getStartMojo("start-pom.xml");
    }

    private StartMojo getStartMojo(final String pomFile) throws Exception {
        // Start up the server and ensure it's running
        final StartMojo startMojo = lookupMojoAndVerify("start", pomFile);
        setValue(startMojo, "jbossHome", TestEnvironment.WILDFLY_HOME.toString());
        setValue(startMojo, "serverArgs",
                new String[] { "-Djboss.management.http.port=" + Integer.toString(TestEnvironment.PORT) });
        return startMojo;
    }

    private static ModelControllerClient createClient() throws UnknownHostException {
        return ModelControllerClient.Factory.create(TestEnvironment.HOSTNAME, TestEnvironment.PORT);
    }

    private static boolean fileContains(final Path path, final String text) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(text)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static AddUser createAddUsers(final String... userStrings) throws NoSuchFieldException, IllegalAccessException {
        final AddUser result = new AddUser();
        final List<User> users = new ArrayList<>(userStrings.length);
        for (String userString : userStrings) {
            users.add(createUser(userString));
        }
        setValue(result, "users", users);
        return result;
    }

    private static User createUser(final String userString) {
        final User user = new User();
        user.set(userString);
        return user;
    }
}
