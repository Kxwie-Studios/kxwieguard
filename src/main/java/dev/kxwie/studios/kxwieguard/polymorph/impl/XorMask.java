package dev.kxwie.studios.kxwieguard.polymorph.impl;

import dev.kxwie.studios.kxwieguard.polymorph.IntMask;
import dev.kxwie.studios.kxwieguard.utils.InsnBuilder;
import org.objectweb.asm.tree.InsnList;

public class XorMask extends IntMask<XorMask> {
    @Override
    public int apply(int num) {
        return num ^ value;
    }

    @Override
    public int applyInverse(int num) {
        return apply(num);
    }

    @Override
    public InsnList insns() {
        return new InsnBuilder()._int(value).ixor().result();
    }
}
