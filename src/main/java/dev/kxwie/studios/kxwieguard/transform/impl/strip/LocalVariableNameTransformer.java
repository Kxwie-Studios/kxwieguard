package dev.kxwie.studios.kxwieguard.transform.impl.strip;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.transform.Transformer;

public class LocalVariableNameTransformer extends Transformer {
    public LocalVariableNameTransformer() {
        super("Remove Local Names", "localNames");
    }

    @Override
    public void transform(Context context) {
        for(var clazz : context.classes()) {
            if(Exclusions.LOCAL_NAMES.excluded(clazz))
                continue;

            clazz.methods().forEach(e -> {
                if(Exclusions.LOCAL_NAMES.excluded(e))
                    return;

                e.core().parameters = null;
                e.localVariables().clear();
                markChange();
            });
        }
    }
}
