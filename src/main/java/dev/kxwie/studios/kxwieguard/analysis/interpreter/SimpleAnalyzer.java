package dev.kxwie.studios.kxwieguard.analysis.interpreter;

import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;


public class SimpleAnalyzer extends Analyzer<SimpleValue> {
    public SimpleAnalyzer(SimpleInterpreter interpreter) {
        super(interpreter);
    }

    @Override
    protected Frame<SimpleValue> newFrame(int numLocals, int numStack) {
        return SimpleFrame.of(numLocals, numStack);
    }

    @Override
    protected Frame<SimpleValue> newFrame(Frame<? extends SimpleValue> frame) {
        return SimpleFrame.of(frame);
    }
}
