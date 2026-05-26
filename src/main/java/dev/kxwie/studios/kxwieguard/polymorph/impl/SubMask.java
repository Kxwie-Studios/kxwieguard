package dev.kxwie.studios.kxwieguard.polymorph.impl;

import dev.kxwie.studios.kxwieguard.polymorph.IntMask;
import dev.kxwie.studios.kxwieguard.utils.InsnBuilder;
import org.objectweb.asm.tree.InsnList;

public class SubMask extends IntMask<SubMask> {
    @Override
    public int apply(int num) {
        return num - value;
    }

    @Override
    public int applyInverse(int num) {
        return num + value;
    }

    @Override
    public InsnList insns() {
        return new InsnBuilder()._int(value).isub().result();
    }
}
