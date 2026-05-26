package dev.kxwie.studios.kxwieguard.transform.impl.rename;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.naming.Mapping;
import dev.kxwie.studios.kxwieguard.naming.Mappings;
import dev.kxwie.studios.kxwieguard.transform.Setting;
import dev.kxwie.studios.kxwieguard.transform.Transformer;

import java.util.ArrayList;
import java.util.Collections;

public class ClassRenameTransformer extends Transformer {
    private final Setting<String> prefix = setting("prefix", "");
    private final Setting<Boolean> randomize = setting("randomize", false);

    public ClassRenameTransformer() {
        super("Rename Classes", "renameClasses");
    }

    @Override
    public void transform(Context context) {
        
        var classes = new ArrayList<>(context.classes());
        if(randomize.value())
            Collections.shuffle(classes);

        
        for(var clazz : classes) {
            if(Exclusions.RENAME_CLASS.excluded(clazz))
                continue;

            var newClassName = context.dictionary().newClassName(prefix.value().replace('.', '/'));
            Mappings.CLASS.register(clazz.name(), new Mapping(newClassName, newClassName));
            clazz.setSourceFile(newClassName + ".java");
            markChange();
        }

        remap(context);
    }
}
