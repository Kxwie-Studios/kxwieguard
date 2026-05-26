package dev.kxwie.studios.kxwieguard.transform.impl.data.strings;

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

public interface IStringDecryptor extends Opcodes {
    SecureRandom random = new SecureRandom();

    
    void generate(Context context, JClass clazz, String fieldName, String cacheName);

    
    InsnList addAndCall(Context context, JMethod method, AbstractInsnNode callSite, Map<AbstractInsnNode, SimpleFrame> frames, List<String> strings, String str);

    
    void setName(String name);

    
    String getDescriptor();
}
