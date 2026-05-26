package dev.kxwie.studios.kxwieguard.transform;

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

import java.util.Comparator;
import java.util.List;


public final class TransformerOrder {
    
    private static final List<Transformer> transformers = List.of(
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
    );

    @SuppressWarnings("unchecked")
    public <T> T get(Class<Transformer> clazz) {
        return (T) transformers.stream()
                .filter(clazz::isInstance)
                .findFirst().orElse(null);
    }

    public List<Transformer> sorted(List<Transformer> transformers) {
        transformers.sort(Comparator.comparingInt(TransformerOrder.transformers::indexOf));
        return transformers;
    }

    public static List<Transformer> transformers() {
        return transformers;
    }
}
