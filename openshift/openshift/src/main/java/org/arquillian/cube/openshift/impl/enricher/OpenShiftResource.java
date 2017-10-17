package org.arquillian.cube.openshift.impl.enricher;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface OpenShiftResource {
    /**
     * The value can either be
     * link (https://www.github.com/alesj/template-testing/some.json)
     * test classpath resource (classpath:some.json)
     * deployment archive resource (archive:some.json)
     * or plain content ({"kind" : "Secret", ...})
     *
     * W/o any prefix (or http schema) it's treated as plain content.
     *
     * @return link, classpath resource, archive resource or content
     */
    String value();
}
