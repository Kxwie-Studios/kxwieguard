package dev.kxwie.studios.kxwieguard.transform.impl.flow;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.property.Property;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import dev.kxwie.studios.kxwieguard.utils.ASMUtils;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class ExceptionFlowTransformer extends Transformer {

    private static final String[] BOGUS_EXCEPTIONS = {
        "java/lang/AssertionError",
        "java/lang/StackOverflowError",
        "java/lang/ClassCircularityError"
    };

    public ExceptionFlowTransformer() {
        super("Exception Flow Obfuscation", "exceptionFlow");
    }

    @Override
    public void transform(Context context) {
        for (var clazz : context.classes()) {
            if (Exclusions.EXCEPTION_FLOW.excluded(clazz)) continue;

            for (var method : clazz.methods()) {
                if (cantEditMethod(clazz, method, true)) continue;
                if (Exclusions.EXCEPTION_FLOW.excluded(method)) continue;
                if (method.properties().has(Property.STRING_DECRYPTOR, Property.INTEGER_DECRYPTOR)) continue;

                var insns = method.insns();
                if (insns.size() < 6) continue;

                var candidates = new ArrayList<AbstractInsnNode>();
                for (var insn : insns) {
                    if (insn instanceof LabelNode || insn instanceof LineNumberNode || insn instanceof FrameNode) continue;
                    if (ASMUtils.isReturn(insn)) continue;
                    if (insn instanceof JumpInsnNode) continue;
                    candidates.add(insn);
                }

                if (candidates.size() < 4) continue;

                int step = Math.max(4, candidates.size() / 4);
                for (int i = 0; i + step < candidates.size(); i += step) {
                    var start = candidates.get(i);
                    var end   = candidates.get(i + step - 1);

                    var startLabel   = new LabelNode();
                    var endLabel     = new LabelNode();
                    var handlerLabel = new LabelNode();
                    var afterLabel   = new LabelNode();

                    insns.insertBefore(start, startLabel);

                    var after = end.getNext();
                    if (after == null) continue;

                    insns.insertBefore(after, endLabel);

                    var handlerList = new InsnList();
                    handlerList.add(new JumpInsnNode(GOTO, afterLabel));
                    handlerList.add(handlerLabel);
                    handlerList.add(new FrameNode(F_SAME1, 0, null, 1,
                            new Object[]{"java/lang/Throwable"}));
                    handlerList.add(new InsnNode(POP));
                    handlerList.add(afterLabel);
                    handlerList.add(new FrameNode(F_SAME, 0, null, 0, null));

                    insns.insertBefore(after, handlerList);

                    var exType = BOGUS_EXCEPTIONS[random.nextInt(BOGUS_EXCEPTIONS.length)];
                    method.traps().add(new TryCatchBlockNode(startLabel, endLabel, handlerLabel, exType));
                }

                markChange();
            }
        }
    }
}
