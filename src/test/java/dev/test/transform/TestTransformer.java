package dev.test.transform;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import org.objectweb.asm.tree.*;

public class TestTransformer extends Transformer {
    public TestTransformer() {
        super("Test", "test");
    }

    @Override
    public void transform(Context context) {
        for(var clazz : context.classes()) {
            for(var method : clazz.methods()) {
                System.out.println(method.fullOriginalName() + "(%s)".formatted(clazz.isLibMethod(method)));

                for(var parent : method.parents()) {
                    System.out.println("\t- " + parent.fullOriginalName());
                }
            }
        }
    }
}
