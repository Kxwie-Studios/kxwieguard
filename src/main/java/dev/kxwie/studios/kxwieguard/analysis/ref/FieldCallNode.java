package dev.kxwie.studios.kxwieguard.analysis.ref;

import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JField;
import dev.kxwie.studios.kxwieguard.tree.impl.JMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

public record FieldCallNode(JClass callerClass, JMethod caller, JField field, AbstractInsnNode insn) {
    public boolean isGetter() {
        var op = insn.getOpcode();
        return op == Opcodes.GETFIELD || op == Opcodes.GETSTATIC;
    }

    public boolean isSetter() {
        var op = insn.getOpcode();
        return op == Opcodes.PUTFIELD || op == Opcodes.PUTSTATIC;
    }

    @Override
    public String toString() {
        return "{ " + caller + " -> " + field + " }";
    }
}
