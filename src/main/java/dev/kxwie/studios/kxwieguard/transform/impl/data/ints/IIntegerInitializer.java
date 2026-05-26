package dev.kxwie.studios.kxwieguard.transform.impl.data.ints;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import org.objectweb.asm.Opcodes;

import java.security.SecureRandom;
import java.util.List;

public interface IIntegerInitializer extends Opcodes {
    SecureRandom random = new SecureRandom();
    void generate(Context context, JClass clazz, String fieldName, List<Integer> numbers);

    default byte[] intToBytes(int i) {
        return new byte[] {
                (byte) (i >> 24), (byte) (i >> 16),
                (byte) (i >> 8), (byte) (i)
        };
    }
}
