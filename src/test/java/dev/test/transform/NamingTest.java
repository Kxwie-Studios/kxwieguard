package dev.test.transform;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.log.Logger;
import dev.kxwie.studios.kxwieguard.naming.Mappings;
import dev.kxwie.studios.kxwieguard.transform.Transformer;

public class NamingTest extends Transformer {
    public NamingTest() {
        super("Naming", "namingTest");
    }

    @Override
    public void transform(Context context) {
        for(var entry : Mappings.METHOD.getMappings().entrySet()) {
            var key = entry.getKey();
            var mapping = entry.getValue();
            Logger.info("%s -> [%s -> %s]", key, mapping.key(), mapping.value());
        }
    }
}
