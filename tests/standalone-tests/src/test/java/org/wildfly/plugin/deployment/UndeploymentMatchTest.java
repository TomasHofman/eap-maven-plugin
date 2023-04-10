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

package org.wildfly.plugin.deployment;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wildfly.plugin.core.DeploymentDescription;
import org.wildfly.plugin.core.DeploymentManager;
import org.wildfly.plugin.tests.AbstractWildFlyServerMojoTest;

/**
 * Matcher Undeployment test case.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class UndeploymentMatchTest extends AbstractWildFlyServerMojoTest {

    @Inject
    private DeploymentManager deploymentManager;

    @Before
    public void before() throws Exception {

        deploymentManager.deploy(getDeployment().setName("test-undeploy-1.war"));
        deploymentManager.deploy(getDeployment().setName("test-undeploy-2.war"));
    }

    @Test
    public void undeployAll() throws Exception {

        undeploy(MatchPatternStrategy.ALL);

        final Set<DeploymentDescription> deployments = deploymentManager.getDeployments();
        assertEquals(0, deployments.size());
    }

    @Test
    public void undeployFirst() throws Exception {

        undeploy(MatchPatternStrategy.FIRST);

        final Set<DeploymentDescription> deployments = deploymentManager.getDeployments();
        assertEquals(1, deployments.size());
    }

    @Test(expected = MojoDeploymentException.class)
    public void undeployFail() throws Exception {
        undeploy(MatchPatternStrategy.FAIL);
    }

    @After
    public void after() throws Exception {
        undeploy(MatchPatternStrategy.ALL);
    }

    private void undeploy(MatchPatternStrategy matchPatternStrategy) throws Exception {

        final UndeployMojo undeployMojo = lookupMojoAndVerify("undeploy", "undeploy-webarchive-match-pom.xml");

        setValue(undeployMojo, "matchPatternStrategy", matchPatternStrategy.toString());
        undeployMojo.execute();
    }

}
