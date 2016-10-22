package org.arquillian.cube.openshift.impl;

import org.arquillian.cube.kubernetes.api.NamespaceService;
import org.arquillian.cube.kubernetes.impl.KubernetesExtension;
import org.arquillian.cube.kubernetes.impl.namespace.DefaultNamespaceService;
import org.arquillian.cube.openshift.impl.client.CubeOpenShiftConfigurator;
import org.arquillian.cube.openshift.impl.client.CubeOpenShiftRegistrar;
import org.arquillian.cube.openshift.impl.client.OpenShiftClientCreator;
import org.arquillian.cube.openshift.impl.client.OpenShiftProjectService;
import org.arquillian.cube.openshift.impl.client.OpenShiftSuiteLifecycleController;
import org.arquillian.cube.openshift.impl.enricher.OpenshiftClientResourceProvider;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class OpenshiftExtension extends KubernetesExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(OpenShiftClientCreator.class)
               .observer(CubeOpenShiftConfigurator.class)
               .observer(CubeOpenShiftRegistrar.class)
               .observer(OpenShiftSuiteLifecycleController.class);

        builder.service(ResourceProvider.class, OpenshiftClientResourceProvider.class)
               .override(NamespaceService.class, DefaultNamespaceService.class, OpenShiftProjectService.class);
    }

}
