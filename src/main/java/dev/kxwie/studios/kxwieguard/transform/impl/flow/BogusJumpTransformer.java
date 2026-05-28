package dev.kxwie.studios.kxwieguard.transform.impl.flow;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.property.Property;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import dev.kxwie.studios.kxwieguard.utils.ASMUtils;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class BogusJumpTransformer extends Transformer {

    public BogusJumpTransformer() {
        super("Bogus Jump Obfuscation", "bogusJumps");
    }

    @Override
    public void transform(Context context) {
        for (var clazz : context.classes()) {
            if (Exclusions.BOGUS_JUMP.excluded(clazz)) continue;

            for (var method : clazz.methods()) {
                if (cantEditMethod(clazz, method, true)) continue;
                if (Exclusions.BOGUS_JUMP.excluded(method)) continue;
                if (method.properties().has(Property.STRING_DECRYPTOR, Property.INTEGER_DECRYPTOR)) continue;

                var insns = method.insns();
                if (insns.size() < 5) continue;

                var candidates = new ArrayList<AbstractInsnNode>();
                for (var insn : insns) {
                    int op = insn.getOpcode();
                    if (op < 0) continue;
                    if (ASMUtils.isReturn(insn)) continue;
                    if (insn instanceof JumpInsnNode) continue;
                    candidates.add(insn);
                }

                int inserted = 0;
                for (int i = 0; i < candidates.size(); i++) {
                    if (i % 8 != 0) continue;
                    var insn = candidates.get(i);

                    var realLabel = new LabelNode();
                    var deadLabel = new LabelNode();

                    int r = random.nextInt();

                    var list = new InsnList();
                    list.add(ASMUtils.pushInt(r));
                    list.add(ASMUtils.pushInt(r));
                    list.add(new InsnNode(IXOR));
                    list.add(new JumpInsnNode(IFNE, deadLabel));
                    list.add(new JumpInsnNode(GOTO, realLabel));
                    list.add(deadLabel);
                    list.add(new FrameNode(F_SAME, 0, null, 0, null));
                    list.add(ASMUtils.pushInt(random.nextInt()));
                    list.add(new InsnNode(POP));
                    list.add(new JumpInsnNode(GOTO, realLabel));
                    list.add(realLabel);
                    list.add(new FrameNode(F_SAME, 0, null, 0, null));

                    insns.insertBefore(insn, list);
                    inserted++;
                }

                if (inserted > 0) markChange();
            }
        }
    }
}
