package dev.test.transform;

import dev.kxwie.studios.kxwieguard.analysis.ref.export.ReferenceDotGraphExport;
import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.transform.Transformer;

public class RefGraphExportTest extends Transformer {
    public RefGraphExportTest() {
        super("Export Ref Graph", "refGraphExport");
    }

    @Override
    public void transform(Context context) {
        System.out.println(new ReferenceDotGraphExport(context).export(true));
    }
}
