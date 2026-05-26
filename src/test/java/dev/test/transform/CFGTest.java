package dev.test.transform;

import dev.kxwie.studios.kxwieguard.analysis.flow.export.DotGraphExport;
import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.transform.Transformer;

public class CFGTest extends Transformer {
    public CFGTest() {
        super("CFGTest", "cfgTest");
    }

    @Override
    public void transform(Context context) {
        for(var clazz : context.classes()) {
            for(var method : clazz.methods()) {
                var graph = method.createFlowGraph(context);
                var export = new DotGraphExport(graph).export();

                System.out.println(method);
                System.out.println(export);
                System.out.println();
            }
        }
    }
}
