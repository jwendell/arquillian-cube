package org.arquillian.cube.openshift.impl.client;

import org.arquillian.cube.kubernetes.api.Configuration;
import org.arquillian.cube.kubernetes.api.Logger;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

public class EnvironmentProcessor {

    @Inject
    protected Instance<Logger> logger;

    public void createEnvironment(@Observes(precedence = 10) BeforeClass event, OpenShiftClient client,
            Configuration conf) throws DeploymentException {

        final TestClass testClass = event.getTestClass();
        Logger log = logger.get();
        log.info(String.format("Creating environment for %s", testClass.getName()));

        //OpenShiftResourceFactory.createResources(testClass.getName(), client, null, testClass.getJavaClass(), configuration.getProperties());
        //processTemplateResources(testClass, client, configuration);
    }

}
