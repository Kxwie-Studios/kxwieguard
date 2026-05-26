package dev.kxwie.studios.kxwieguard.property;

import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JMethod;

import java.util.HashMap;
import java.util.Map;


public class GlobalPropertyContainer {
    private final Map<Object, PropertyContainer> properties;

    public GlobalPropertyContainer() {
        this.properties = new HashMap<>();
    }

    public <T> T add(T obj, Property... properties) {
        this.properties.computeIfAbsent(obj, _ -> new PropertyContainer()).add(properties);
        return obj;
    }

    public PropertyContainer get(Object obj) {
        
        return properties.getOrDefault(obj, new PropertyContainer());
    }

    public Map<Object, PropertyContainer> properties() {
        return properties;
    }
}
