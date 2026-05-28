package dev.kxwie.studios.kxwieguard.transform.impl.rename;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.property.Property;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import dev.kxwie.studios.kxwieguard.utils.ASMUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashMap;

public class MethodParameterObfuscationTransformer extends Transformer {

    public MethodParameterObfuscationTransformer() {
        super("Method Parameter Obfuscation", "methodParameterObfuscate");
    }

    @Override
    public void transform(Context context) {
        var remapped = new HashMap<String, String>();

        for (var clazz : context.classes()) {
            if (Exclusions.PARAMETER_OBFUSCATE.excluded(clazz)) continue;

            for (var method : clazz.methods()) {
                if (cantEditMethod(clazz, method, true)) continue;
                if (Exclusions.PARAMETER_OBFUSCATE.excluded(method)) continue;
                if (method.properties().has(Property.STRING_DECRYPTOR, Property.INTEGER_DECRYPTOR)) continue;

                if (!method.isNonHierarchical()) continue;

                var oldDesc = method.desc();
                var key = clazz.name() + "." + method.name() + oldDesc;

                method.allocParameter(Type.INT_TYPE);

                remapped.put(key, method.desc());
                markChange();
            }
        }

        if (remapped.isEmpty()) return;

        for (var clazz : context.jarClasses()) {
            for (var method : clazz.methods()) {
                for (var insn : method.insns()) {
                    if (!(insn instanceof MethodInsnNode call)) continue;

                    var key = call.owner + "." + call.name + call.desc;
                    var newDesc = remapped.get(key);
                    if (newDesc == null) continue;

                    call.desc = newDesc;

                    var push = new org.objectweb.asm.tree.InsnList();
                    push.add(ASMUtils.pushInt(0));
                    method.insns().insertBefore(call, push);
                }
            }
        }
    }
}
