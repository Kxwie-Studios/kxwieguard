package dev.kxwie.studios.kxwieguard.salt;

import dev.kxwie.studios.kxwieguard.analysis.flow.graph.Block;
import dev.kxwie.studios.kxwieguard.analysis.interpreter.SimpleValue;
import org.objectweb.asm.tree.analysis.Frame;

public interface ISaltable<T extends ISalt> {
    boolean hasSalt();

    T salt();

    default boolean canSalt(Frame<SimpleValue> frame) {
        return true;
    }

    default boolean canSalt(Block block) {
        return true;
    }
}
