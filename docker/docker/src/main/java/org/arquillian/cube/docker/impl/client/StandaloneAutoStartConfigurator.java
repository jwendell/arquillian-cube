package org.arquillian.cube.docker.impl.client;

import org.arquillian.cube.docker.impl.client.config.CubeContainers;
import org.jboss.arquillian.core.api.annotation.Observes;

public class StandaloneAutoStartConfigurator {

    public void configure(@Observes CubeDockerConfiguration event) {
        if (event.getAutoStartContainers() == null) {
            AutoStartParser autoStartParser = resolveNotSetAutoStart(event.getDockerContainersContent());
            event.setAutoStartContainers(autoStartParser);
        }
    }

    private AutoStartParser resolveNotSetAutoStart(CubeContainers cubeContainers) {
        return new RegularExpressionAutoStartParser(RegularExpressionAutoStartParser.REGULAR_EXPRESSION_PREFIX + ".*", cubeContainers);
    }

}
