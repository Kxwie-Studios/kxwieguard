package dev.kxwie.studios.kxwieguard.transform.impl.strip;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.transform.Setting;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import org.objectweb.asm.tree.LineNumberNode;

public class LineNumberTransformer extends Transformer {
    private final Setting<Boolean> remove = setting("remove", true);

    public LineNumberTransformer() {
        super("Line Number Mutation", "lineNumbers");
    }

    @Override
    public void transform(Context context) {
        for(var clazz : context.classes()) {
            if(Exclusions.LINE_NUMBERS.excluded(clazz))
                continue;

            for(var method : clazz.methods()) {
                if(Exclusions.LINE_NUMBERS.excluded(method))
                    continue;

                for(var insn : method.insns()) {
                    if(!(insn instanceof LineNumberNode ln))
                        continue;

                    if(remove.value()) {
                        method.insns().remove(ln);
                    } else {
                        ln.line = random.nextInt(Short.MAX_VALUE);
                    }
                    markChange();
                }
            }
        }
    }
}
