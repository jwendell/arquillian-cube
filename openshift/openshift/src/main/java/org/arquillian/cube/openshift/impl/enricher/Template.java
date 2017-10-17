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
@Target(ElementType.TYPE)
public @interface Template {
    String url() default "";

    String labels() default "";

    TemplateParameter[] parameters() default {};

    /**
     * Do we invoke K8s process template?
     */
    boolean process() default true;
}
