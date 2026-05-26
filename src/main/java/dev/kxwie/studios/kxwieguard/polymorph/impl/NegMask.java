package dev.kxwie.studios.kxwieguard.polymorph.impl;

import dev.kxwie.studios.kxwieguard.polymorph.IntMask;
import dev.kxwie.studios.kxwieguard.utils.InsnBuilder;
import org.objectweb.asm.tree.InsnList;

public class NegMask extends IntMask<NegMask> {
    @Override
    public int apply(int num) {
        return -num;
    }

    @Override
    public InsnList insns() {
        return new InsnBuilder().ineg().result();
    }
}
