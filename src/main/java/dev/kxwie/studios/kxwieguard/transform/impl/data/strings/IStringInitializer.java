package dev.kxwie.studios.kxwieguard.transform.impl.data.strings;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import org.objectweb.asm.Opcodes;

import java.security.SecureRandom;
import java.util.List;

public interface IStringInitializer extends Opcodes {
    SecureRandom random = new SecureRandom();

    
    void generate(Context context, JClass clazz, String fieldName, String cacheName, List<String> strings);
}
