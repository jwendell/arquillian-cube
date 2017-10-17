package org.arquillian.cube.impl.util;

import java.util.Properties;

import org.jboss.dmr.ValueExpressionResolver;

public class CustomValueExpressionResolver extends ValueExpressionResolver {
    private final Properties properties;

    public CustomValueExpressionResolver(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected String resolvePart(String name) {
        String value = (String) properties.get(name);
        if (value != null) {
            return value;
        }
        return super.resolvePart(name);
    }
}
