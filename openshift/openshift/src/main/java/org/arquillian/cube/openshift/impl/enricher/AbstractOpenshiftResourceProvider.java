package org.arquillian.cube.openshift.impl.enricher;

import org.arquillian.cube.kubernetes.impl.enricher.AbstractKubernetesResourceProvider;
import org.arquillian.cube.openshift.impl.client.OpenShiftClient;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;

public abstract class AbstractOpenshiftResourceProvider extends AbstractKubernetesResourceProvider {

    @Inject
    private Instance<OpenShiftClient> openshiftClient;

    protected OpenShiftClient getOpenshiftClient() {
        return openshiftClient.get();
    }
}
