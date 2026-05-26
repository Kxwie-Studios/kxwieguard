package dev.kxwie.studios.kxwieguard.transform.impl.data;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JField;
import dev.kxwie.studios.kxwieguard.utils.InsnBuilder;


public class ConstantsFixTransformer extends Transformer {
  public ConstantsFixTransformer() {
    super("Fix Constants", "fixConstants");
  }

  @Override
  public void transform(Context context) {
    for (JClass clazz : context.classes()) {
      if (Exclusions.FIX_CONSTANTS.excluded(clazz)) {
        continue;
      }

      for (JField field : clazz.fields()) {
        if (field.value() == null) {
          continue;
        }

        if (field.isVirtual()) {
          continue;
        }

        if (Exclusions.FIX_CONSTANTS.excluded(field)) {
          continue;
        }

        
        InsnBuilder builder = new InsnBuilder()
            ._const(field.value())
            .field(PUTSTATIC, clazz.name(), field.name(), field.desc());
        clazz.findOrCreateClinit().insertSafe(builder.result());
        field.setValue(null);

        markChange();
      }
    }
  }
}
