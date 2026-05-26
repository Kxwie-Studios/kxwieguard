package dev.kxwie.studios.kxwieguard.classgen;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;

public interface IClassGen {
    JClass create(Context context);
}
