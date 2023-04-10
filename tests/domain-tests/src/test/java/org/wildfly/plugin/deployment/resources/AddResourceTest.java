/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.plugin.deployment.resources;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;
import org.wildfly.plugin.deployment.resource.AddResourceMojo;
import org.wildfly.plugin.tests.AbstractWildFlyServerMojoTest;

/**
 * AddResource test case
 *
 * @author <a href="mailto:dave.himself@gmail.com">Dave Heath</a>
 */
// @Ignore("Composite operations don't seem to be working with datasources")
public class AddResourceTest extends AbstractWildFlyServerMojoTest {

    @Test
    public void testCanAddCompositeResource() throws Exception {

        final AddResourceMojo addResourceMojo = find("add-resource-with-composite-pom.xml");
        try {
            addResourceMojo.execute();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

    }

    @Test
    public void testCanAddResource() throws Exception {

        AddResourceMojo addResourceMojo = find("add-resource-pom.xml");
        try {
            addResourceMojo.execute();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

    }

    @Test
    public void testCanAddXaDataSource() throws Exception {

        final AddResourceMojo addResourceMojo = find("add-resource-xa-datasource.xml");
        try {
            addResourceMojo.execute();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

    }

    private AddResourceMojo find(final String pom) throws Exception {
        final AddResourceMojo addResourceMojo = lookupMojoAndVerify("add-resource", pom);
        // Profiles are required to be set and when there is a property defined on an attribute parameter the test
        // harness does not set the fields
        setValue(addResourceMojo, "profiles", Collections.singletonList("full"));
        return addResourceMojo;
    }

}
