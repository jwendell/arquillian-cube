package org.arquillian.cube.openshift.impl.enricher;

import java.lang.annotation.Annotation;

import org.arquillian.cube.openshift.impl.client.OpenShiftClient;
import org.jboss.arquillian.test.api.ArquillianResource;

public class OpenshiftClientResourceProvider extends AbstractOpenshiftResourceProvider {

    @Override
    public boolean canProvide(Class<?> type) {
        return OpenShiftClient.class.isAssignableFrom(type);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        OpenShiftClient client = getOpenshiftClient();

        if (client == null) {
            throw new IllegalStateException("Unable to inject Openshift client into test.");
        }

        return client;
    }

}
