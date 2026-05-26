package dev.kxwie.studios.kxwieguard.context.hierarchy;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JField;
import dev.kxwie.studios.kxwieguard.tree.impl.JMethod;

import java.util.List;

public interface IHierarchy {
    String object = "java/lang/Object";

    void build();

    void build(JClass clazz);
    void build(JMethod method);
    void build(JField field);

    void buildIfEmpty(JClass clazz);
    void buildIfEmpty(JMethod method);
    void buildIfEmpty(JField field);

    String commonSuperClass(String type1, String type2);

    default void trace(Context context, JClass clazz, List<String> used) {
        if(clazz == null)
            return;

        if(used.contains(clazz.name()))
            return;

        used.add(clazz.name());
        if(clazz.superName() != null && !clazz.isInterface())
            trace(context, context.forName(clazz.superName()), used);

        for(var itf : clazz.interfaces()) {
            var itfClass = context.forName(itf);
            trace(context, itfClass, used);
        }
    }
}
