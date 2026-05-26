package dev.kxwie.studios.kxwieguard.transform.impl.flow;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.Collections;

public class ControlFlowShufflingTransformer extends Transformer {
    public ControlFlowShufflingTransformer() {
        super("Control Flow Shuffling", "controlFlowShuffle");
    }

    @Override
    public void transform(Context context) {
        for(var clazz : context.classes()) {
            if(Exclusions.FLOW_SHUFFLE.excluded(clazz))
                continue;

            for(var method : clazz.methods()) {
                if(Exclusions.FLOW_SHUFFLE.excluded(method))
                    continue;

                if(!method.traps().isEmpty())
                    continue;

                var graph = method.createFlowGraph(context);
                if(graph.blocks().size() <= 3)
                    continue;

                
                method.localVariables().clear();
                var firstBlock = graph.firstBlock();
                var blocks = graph.blocks();

                Collections.shuffle(blocks, random);
                blocks.remove(firstBlock);
                blocks.addFirst(firstBlock);

                
                var rebuilt = new InsnList();
                for(var block : blocks) {
                    for(var insn : block.insns()) {
                        method.insns().remove(insn); 
                        if(insn instanceof FrameNode)
                            continue;

                        rebuilt.add(insn);
                    }

                    if(block.deadEnd() || block.ends())
                        continue;

                    var dfltIdx = blocks.indexOf(block.defaultBlock());
                    var currIdx = blocks.indexOf(block);
                    if((dfltIdx - currIdx) == 1) 
                        continue;

                    rebuilt.add(new JumpInsnNode(GOTO, block.defaultBlock().label()));
                }

                method.insns().clear();
                method.insns().add(rebuilt);
                markChange();
            }
        }
    }
}
