package dev.kxwie.studios.kxwieguard.transform.impl.data.strings.initializers;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.property.Property;
import dev.kxwie.studios.kxwieguard.transform.impl.data.strings.IStringInitializer;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.utils.ASMUtils;
import dev.kxwie.studios.kxwieguard.utils.InsnBuilder;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;


public class DefaultStringInitializer implements IStringInitializer {
    @Override
    public void generate(Context context, JClass clazz, String fieldName, String cacheName, List<String> strings) {
        
        int key = random.nextInt(Short.MAX_VALUE);
        var strBuilder = new StringBuilder();
        var lengthStr = new StringBuilder();

        for(var str : strings) {
            strBuilder.append(str);
            lengthStr.append((char) (str.length() ^ key));
        }

        var theStr = strBuilder.toString();
        var lenStr = lengthStr.toString();

        var method = clazz.findOrCreateClinit();

        
        var keyVar = method.allocVar(Type.INT_TYPE);
        var strVar = method.allocVar();
        var lenArrVar = method.allocVar();
        var strArrVar = method.allocVar();
        var iVar = method.allocVar(Type.INT_TYPE);
        var offsetVar = method.allocVar(Type.INT_TYPE);
        var lenVar = method.allocVar(Type.INT_TYPE);
        var statusVar = method.allocVar(Type.INT_TYPE);

        
        var loop = new LabelNode();
        var varLbl = new LabelNode();

        var builder = new InsnBuilder()
                .label(new LabelNode());
        if(clazz.hasSalt()) {
            builder.add(context.properties().add(ASMUtils.pushInt(key ^ clazz.salt().value()), Property.SENSITIVE_CONSTANT, Property.IGNORE_INTEGER))
                    .add(clazz.salt().load())
                    .ixor();
        } else {
            builder.add(context.properties().add(ASMUtils.pushInt(key), Property.SENSITIVE_CONSTANT, Property.IGNORE_INTEGER));
        }

        builder._var(ISTORE, keyVar)

                .label(new LabelNode())
                ._const(theStr)
                ._var(ASTORE, strVar)

                .label(new LabelNode())
                ._const(lenStr)
                .method(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C")
                ._var(ASTORE, lenArrVar)

                .label(new LabelNode())
                ._var(ALOAD, lenArrVar)
                .arraylength()
                .anewarray("java/lang/String")
                ._var(ASTORE, strArrVar)

                .label(new LabelNode())
                ._int(-1)
                ._var(ISTORE, statusVar)
                ._goto(varLbl)

                
                .label(loop)
                ._var(ALOAD, lenArrVar)
                ._var(ILOAD, iVar)
                .caload()
                ._var(ILOAD, keyVar)
                .ixor()
                ._var(ISTORE, lenVar)

                .label(new LabelNode())
                ._var(ALOAD, strArrVar)
                ._var(ILOAD, iVar)

                ._var(ALOAD, strVar) 
                ._var(ILOAD, offsetVar)
                ._var(ILOAD, offsetVar)
                ._var(ILOAD, lenVar)
                .iadd()
                .method(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;") 
                .aastore()

                .label(new LabelNode())
                ._var(ILOAD, offsetVar)
                ._var(ILOAD, lenVar)
                .iadd()
                ._var(ISTORE, offsetVar)

                .label(new LabelNode())
                .iinc(iVar, 1)

                .label(new LabelNode())
                ._var(ILOAD, iVar)
                ._var(ALOAD, lenArrVar)
                .arraylength()
                .jump(IF_ICMPLT, loop)

                
                .label(new LabelNode())
                ._int(0)
                ._var(ISTORE, statusVar)

                .label(varLbl)
                ._int(0)
                ._var(ISTORE, iVar)

                .label(new LabelNode())
                ._int(0)
                ._var(ISTORE, offsetVar)

                .label(new LabelNode())
                ._int(0)
                ._var(ISTORE, lenVar)

                .label(new LabelNode())
                ._var(ILOAD, statusVar)
                .jump(IFNE, loop)

                .label(new LabelNode())
                ._var(ALOAD, strArrVar)
                .field(PUTSTATIC, clazz.name(), fieldName, "[Ljava/lang/String;")

                .label(new LabelNode())
                ._var(ALOAD, strArrVar)
                .arraylength()
                .anewarray("java/lang/Object")
                .field(PUTSTATIC, clazz.name(), cacheName, "[Ljava/lang/Object;");

        method.insertSafe(builder.result());
    }
}
