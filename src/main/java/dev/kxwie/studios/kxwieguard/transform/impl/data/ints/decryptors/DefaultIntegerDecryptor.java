package dev.kxwie.studios.kxwieguard.transform.impl.data.ints.decryptors;

import dev.kxwie.studios.kxwieguard.analysis.interpreter.SimpleFrame;
import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.property.Property;
import dev.kxwie.studios.kxwieguard.transform.impl.data.ints.IIntegerDecryptor;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JMethod;
import dev.kxwie.studios.kxwieguard.utils.ASMUtils;
import dev.kxwie.studios.kxwieguard.utils.InsnBuilder;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;
import java.util.Map;

public class DefaultIntegerDecryptor implements IIntegerDecryptor {
    private String name;
    private final int idxXor;

    public DefaultIntegerDecryptor() {
        this.idxXor = random.nextInt();
    }

    @Override
    public void generate(JClass clazz, String fieldName) {
        int access = (clazz.isInterface())
                ? ACC_PUBLIC | ACC_STATIC
                : ACC_PRIVATE | ACC_STATIC;
        var method = clazz.createMethod(access, name, getDescriptor());

        
        var idxVal = method.allocVar(Type.INT_TYPE);
        var key = method.allocVar(Type.INT_TYPE);

        new InsnBuilder(method.insns())
                .label(new LabelNode())

                .label(new LabelNode())
                .field(GETSTATIC, clazz.name(), fieldName, "[I")
                ._var(ILOAD, idxVal)
                ._int(idxXor)
                .ixor()
                .iaload()
                ._var(ILOAD, key)
                .ixor()
                ._var(ILOAD, idxVal)
                .ixor()
                ._ireturn()
        ;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescriptor() {
        return "(II)I";
    }

    @Override
    public InsnList addAndCall(Context context, JMethod method, AbstractInsnNode callSite, Map<AbstractInsnNode, SimpleFrame> frames, List<Integer> numbers, int num) {
        
        var key = random.nextInt();
        int idxValue = numbers.size() ^ idxXor;
        num = ASMUtils.getInt(callSite) ^ key ^ idxValue;
        numbers.add(num);

        
        var builder = new InsnBuilder()._int(idxValue);
        if(method.canSalt(frames.get(callSite))) {
            var mask = method.seed();
            var masked = method.salt().value() & mask;

            builder
                    .add(method.salt().load())
                    ._int(mask).addProps(context, Property.IGNORE_INTEGER)
                    .iand()
                    ._int(masked ^ key).addProps(context, Property.IGNORE_INTEGER)
                    .ixor()
            ;
        } else {
            builder._int(key);
        }
        builder.add(context.properties().add(
                new MethodInsnNode(INVOKESTATIC, method.owner().name(), name, getDescriptor(), method.owner().isInterface()), Property.IGNORE_REF_OBFUSCATION
        ));
        return builder.result();
    }
}
