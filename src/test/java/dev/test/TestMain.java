package dev.test;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.transform.impl.data.ConstantsFixTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.data.ints.IntegerEncryptTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.data.strings.StringEncryptTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.dynamic.ReferenceObfuscationTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.flow.ControlFlowFlatteningTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.flow.ControlFlowShufflingTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.optimize.DeadCodeCleanTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.optimize.TrimTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.rename.ClassRenameTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.rename.FieldRenameTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.rename.MethodRenameTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.salt.ClassSaltTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.salt.MethodSaltTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.strip.LineNumberTransformer;
import dev.kxwie.studios.kxwieguard.transform.impl.strip.LocalVariableNameTransformer;
import dev.test.transform.MethodParameterObfuscationTransformer;
import dev.test.transform.TestTransformer;

public class TestMain {
    public static void main(String[] args) {
        var context = Context.newInstance()
                .computeFrames()
                .in("in.jar")
                .libs("libs")
                .out("out.jar")
                .setAggressiveOverload(true)
                .initialize();

        
        
        

        context.transform(
                new TrimTransformer(),

                new FieldRenameTransformer(),
                new MethodRenameTransformer(),
                new ClassRenameTransformer(),

                new LocalVariableNameTransformer(),
                new LineNumberTransformer(),
                new MethodSaltTransformer(),
                new ClassSaltTransformer(),

                new ConstantsFixTransformer(),
                new IntegerEncryptTransformer(),
                new StringEncryptTransformer(),

                new ControlFlowFlatteningTransformer(),
                new ControlFlowShufflingTransformer(),
                new DeadCodeCleanTransformer(),
                new ReferenceObfuscationTransformer()
        ).exportJar();
    }
}
