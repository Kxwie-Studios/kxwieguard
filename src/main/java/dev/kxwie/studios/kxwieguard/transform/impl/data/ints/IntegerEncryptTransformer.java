package dev.kxwie.studios.kxwieguard.transform.impl.data.ints;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.property.Property;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import dev.kxwie.studios.kxwieguard.transform.impl.data.ints.decryptors.DefaultIntegerDecryptor;
import dev.kxwie.studios.kxwieguard.transform.impl.data.ints.initializers.DefaultIntegerInitializer;
import dev.kxwie.studios.kxwieguard.utils.ASMUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class IntegerEncryptTransformer extends Transformer {
    private final List<Supplier<IIntegerInitializer>> initializers = List.of(
            DefaultIntegerInitializer::new
    );

    private final List<Supplier<IIntegerDecryptor>> decryptors = List.of(
            DefaultIntegerDecryptor::new
    );

    public IntegerEncryptTransformer() {
        super("Encrypt Integer Constants", "integerEncrypt");
    }

    @Override
    public void transform(Context context) {
        for(var clazz : context.classes()) {
            if(clazz.isInterface() && clazz.version() < V1_8)
                continue;

            if(Exclusions.INTEGER_ENCRYPTION.excluded(clazz))
                continue;

            var initializer = initializers.get(random.nextInt(initializers.size())).get();
            var decryptor = decryptors.get(random.nextInt(decryptors.size())).get();

            var numbers = new ArrayList<Integer>();
            var decryptorName = context.dictionary().newMethodName(clazz, decryptor.getDescriptor());
            var fieldName = context.dictionary().newFieldName(clazz, "[I");

            decryptor.setName(decryptorName);
            for(var method : clazz.methods()) {
                if(Exclusions.INTEGER_ENCRYPTION.excluded(method))
                    continue;

                var frames = method.frames(context);
                for(var insn : method.insns()) {
                    if(!ASMUtils.isIntPush(insn))
                        continue;

                    if(ASMUtils.isIconst(insn))
                        continue;

                    if(context.properties().get(insn).has(Property.IGNORE_INTEGER))
                        continue;

                    var num = ASMUtils.getInt(insn);
                    method.insns().insertBefore(insn, decryptor.addAndCall(context, method, insn, frames, numbers, num));
                    method.insns().remove(insn);
                    markChange();
                }
            }

            if(numbers.isEmpty()) {
                context.dictionary().revertMethod();
                context.dictionary().revertField();
                continue;
            }

            int access = (clazz.isInterface() ? ACC_PUBLIC : ACC_PRIVATE) | ACC_STATIC | ACC_FINAL;
            clazz.createField(access, fieldName, "[I");

            decryptor.generate(clazz, fieldName);
            initializer.generate(context, clazz, fieldName, numbers);
        }
    }
}
