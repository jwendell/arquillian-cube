package org.arquillian.cube.openshift.impl.client;

import java.util.Map;

import org.arquillian.cube.kubernetes.api.LabelProvider;
import org.arquillian.cube.kubernetes.api.NamespaceService;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;

public class OpenShiftProjectService implements NamespaceService {
    private static final String PROJECT_LABEL = "project";
    private static final String FRAMEWORK_LABEL = "framework";
    private static final String COMPONENT_LABEL = "component";

    private static final String ARQUILLIAN_FRAMEWORK = "arquillian";
    private static final String ITEST_COMPONENT = "integrationTest";

    @Inject
    Instance<OpenShiftClient> client;

    @Inject
    Instance<LabelProvider> labelProvider;

    @Override
    public void create(String namespace) {
        client.get().getClientExt().projectrequests().createNew().withNewMetadata()
            .withName(namespace)
            .addToLabels(labelProvider.get().getLabels())
            .addToLabels(PROJECT_LABEL, client.get().getClientExt().getNamespace())
            .addToLabels(FRAMEWORK_LABEL, ARQUILLIAN_FRAMEWORK)
            .addToLabels(COMPONENT_LABEL, ITEST_COMPONENT)
            .endMetadata()
            .done();
    }

    @Override
    public void annotate(String namespace, Map<String, String> annotations) {
        client.get().getClientExt().projects().withName(namespace).edit()
                .editMetadata()
                .addToAnnotations(annotations)
                .endMetadata().done();
    }

    @Override
    public Boolean delete(String namespace) {
        return client.get().getClientExt().projects().withName(namespace).delete();
    }

    @Override
    public Boolean exists(String namespace) {
        return client.get().getClientExt().projects().withName(namespace).get() != null;
    }

    @Override
    public void clean(String namespace) {
        System.out.println("OpenShiftProjectService::CLEAN!!!!");
    }

    @Override
    public void destroy(String namespace) {
        System.out.println("OpenShiftProjectService::DESTROY!!!!");
        delete(namespace);
    }
}
