package dev.kxwie.studios.kxwieguard.salt;

import org.objectweb.asm.tree.AbstractInsnNode;

public interface ISalt {
    int value();

    AbstractInsnNode load();
    AbstractInsnNode store();
}
