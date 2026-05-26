package dev.kxwie.studios.kxwieguard.context.asm;

import dev.kxwie.studios.kxwieguard.context.Context;
import org.objectweb.asm.ClassWriter;


public class HierarchyClassWriter extends ClassWriter {
    private final Context context;

    public HierarchyClassWriter(Context context) {
        super(context.writerFlags());
        this.context = context;

        if (!context.watermark().isEmpty()) {
            this.newUTF8(context.watermark());
        }
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        return context.hierarchy().commonSuperClass(type1, type2);
    }
}
