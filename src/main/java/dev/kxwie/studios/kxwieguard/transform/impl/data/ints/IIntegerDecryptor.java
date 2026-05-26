package dev.kxwie.studios.kxwieguard.transform.impl.data.ints;

import dev.kxwie.studios.kxwieguard.analysis.interpreter.SimpleFrame;
import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

public interface IIntegerDecryptor extends Opcodes {
    SecureRandom random = new SecureRandom();

    void generate(JClass clazz, String fieldName);

    void setName(String name);

    String getDescriptor();

    InsnList addAndCall(Context context, JMethod method, AbstractInsnNode callSite, Map<AbstractInsnNode, SimpleFrame> frames, List<Integer> numbers, int num);

    default byte[] intToBytes(int i) {
        return new byte[] {
                (byte) (i >> 24), (byte) (i >> 16),
                (byte) (i >> 8), (byte) (i)
        };
    }
}
