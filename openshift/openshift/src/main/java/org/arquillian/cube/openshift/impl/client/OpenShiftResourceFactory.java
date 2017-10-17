package org.arquillian.cube.openshift.impl.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.arquillian.cube.impl.util.StringResolver;
import org.arquillian.cube.impl.util.Strings;
import org.arquillian.cube.openshift.impl.enricher.OpenShiftResource;

/*
import org.jboss.arquillian.ce.adapter.OpenShiftAdapter;
import org.jboss.arquillian.ce.api.AddRoleToServiceAccount;
import org.jboss.arquillian.ce.api.AddRoleToServiceAccountWrapper;
import org.jboss.arquillian.ce.api.OpenShiftResource;
import org.jboss.arquillian.ce.api.OpenShiftResources;
import org.jboss.arquillian.ce.api.RoleBinding;
import org.jboss.arquillian.ce.api.RoleBindings;
import org.jboss.arquillian.ce.api.Template;
import org.jboss.arquillian.ce.api.TemplateResources;
*/

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;

public class OpenShiftResourceFactory {
    private static final Logger log = Logger.getLogger(OpenShiftResourceFactory.class.getName());

    public static final String CLASSPATH_PREFIX = "classpath:";
    public static final String ARCHIVE_PREFIX = "archive:";
    public static final String URL_PREFIX = "http";

    private static final OSRFinder OSR_FINDER = new OSRFinder();
    private static final RBFinder RB_FINDER = new RBFinder();
    private static final ARSAFinder ARSA_FINDER = new ARSAFinder();
    private static final TEMPFinder TEMP_FINDER = new TEMPFinder();

    public static void createResources(String resourcesKey, Archive<?> archive, Class<?> testClass, Properties properties) {
        try {
            final StringResolver resolver = Strings.createStringResolver(properties);

            List<OpenShiftResource> openShiftResources = new ArrayList<>();
            OSR_FINDER.findAnnotations(openShiftResources, testClass);
            for (OpenShiftResource osr : openShiftResources) {
                String file = resolver.resolve(osr.value());

                InputStream stream;
                if (file.startsWith(URL_PREFIX)) {
                    stream = new URL(file).openStream();
                } else if (file.startsWith(CLASSPATH_PREFIX)) {
                    String resourceName = file.substring(CLASSPATH_PREFIX.length());
                    stream = testClass.getClassLoader().getResourceAsStream(resourceName);
                    if (stream == null) {
                        throw new IllegalArgumentException("Could not find resource on classpath: " + resourceName);
                    }
                } else if (file.startsWith(ARCHIVE_PREFIX)) {
                    String resourceName = file.substring(ARCHIVE_PREFIX.length());
                    Node node = archive.get(resourceName);
                    if (node == null) {
                        throw new IllegalArgumentException("Could not find resource in Arquillian archive: " + resourceName);
                    }
                    stream = node.getAsset().openStream();
                } else {
                    stream = new ByteArrayInputStream(file.getBytes());
                }

                log.info(String.format("Creating new OpenShift resource: %s", file));
                adapter.createResource(resourcesKey, stream);
            }

            List<RoleBinding> roleBindings = new ArrayList<>();
            RB_FINDER.findAnnotations(roleBindings, testClass);
            for (RoleBinding rb : roleBindings) {
                String roleRefName = resolver.resolve(rb.roleRefName());
                String userName = resolver.resolve(rb.userName());
                log.info(String.format("Adding new role binding: %s / %s", roleRefName, userName));
                adapter.addRoleBinding(resourcesKey, roleRefName, userName);
            }

            List<AddRoleToServiceAccount> arsaBindings = new ArrayList<>();
            ARSA_FINDER.findAnnotations(arsaBindings, testClass);
            for (AddRoleToServiceAccount arsa : arsaBindings) {
                String role = resolver.resolve(arsa.role());
                String saPattern = String.format("system:serviceaccount:${kubernetes.namespace}:%s", arsa.serviceAccount());
                String serviceAccount = resolver.resolve(saPattern);
                log.info(String.format("Adding role %s to service account %s", role, serviceAccount));
                adapter.addRoleBinding(resourcesKey, role, serviceAccount);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Aggregates a list of templates specified by @Template
     */
    public static List<Template> getTemplates(Class<?> testClass) {
        try {
            List<Template> templates = new ArrayList<>();
            TEMP_FINDER.findAnnotations(templates, testClass);
            return templates;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns true if templates are to be instantiated synchronously and false if
     * asynchronously.
     */
    public static boolean syncInstantiation(Class<?> testClass) {
    	List<Template> templates = new ArrayList<>();
        TemplateResources tr = TEMP_FINDER.findAnnotations(templates, testClass);
        if (tr == null) {
        	/* Default to synchronous instantiation */
        	return true;
        } else {
        	return tr.syncInstantiation();
        }
    }

    public static void deleteResources(String resourcesKey, OpenShiftAdapter adapter) {
        try {
            adapter.deleteResources(resourcesKey);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static abstract class Finder<U extends Annotation, V extends Annotation> {

        protected abstract Class<U> getWrapperType();

        protected abstract Class<V> getSingleType();

        protected abstract V[] toSingles(U u);

        U findAnnotations(List<V> annotations, Class<?> testClass) {
            if (testClass == Object.class) {
                return null;
            }

            U anns = testClass.getAnnotation(getWrapperType());
            if (anns != null) {
                V[] ann = toSingles(anns);
                for (int i = ann.length - 1; i >= 0; i--) {
                    annotations.add(0, ann[i]);
                }
            }

            V ann = testClass.getAnnotation(getSingleType());
            if (ann != null) {
                annotations.add(0, ann);
            }

            findAnnotations(annotations, testClass.getSuperclass());
	    return anns;
        }

    }

    private static class OSRFinder extends Finder<OpenShiftResources, OpenShiftResource> {
        protected Class<OpenShiftResources> getWrapperType() {
            return OpenShiftResources.class;
        }

        protected Class<OpenShiftResource> getSingleType() {
            return OpenShiftResource.class;
        }

        protected OpenShiftResource[] toSingles(OpenShiftResources openShiftResources) {
            return openShiftResources.value();
        }
    }

    private static class RBFinder extends Finder<RoleBindings, RoleBinding> {
        protected Class<RoleBindings> getWrapperType() {
            return RoleBindings.class;
        }

        protected Class<RoleBinding> getSingleType() {
            return RoleBinding.class;
        }

        protected RoleBinding[] toSingles(RoleBindings roleBindings) {
            return roleBindings.value();
        }
    }

    private static class ARSAFinder extends Finder<AddRoleToServiceAccountWrapper, AddRoleToServiceAccount> {
        protected Class<AddRoleToServiceAccountWrapper> getWrapperType() {
            return AddRoleToServiceAccountWrapper.class;
        }

        protected Class<AddRoleToServiceAccount> getSingleType() {
            return AddRoleToServiceAccount.class;
        }

        protected AddRoleToServiceAccount[] toSingles(AddRoleToServiceAccountWrapper roleBindings) {
            return roleBindings.value();
        }
    }

    private static class TEMPFinder extends Finder<TemplateResources, Template> {
        protected Class<TemplateResources> getWrapperType() {
            return TemplateResources.class;
        }

        protected Class<Template> getSingleType() {
            return Template.class;
        }

        protected Template[] toSingles(TemplateResources templateResources) {
            return templateResources.templates();
        }

        protected boolean syncInstantiation(TemplateResources templateResources) {
            return templateResources.syncInstantiation();
        }
    }
}
