package dev.kxwie.studios.kxwieguard.classgen.impl;

import dev.kxwie.studios.kxwieguard.classgen.IClassGen;
import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JField;
import dev.kxwie.studios.kxwieguard.tree.impl.JMethod;
import dev.kxwie.studios.kxwieguard.utils.InsnBuilder;
import dev.kxwie.studios.kxwieguard.utils.SwitchUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;

import java.lang.invoke.*;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class ReferenceObfuscationClassGenerator implements IClassGen {
    private static final String invokerDesc = MethodType.methodType(
            CallSite.class,
            MethodHandles.Lookup.class,
            String.class,
            MethodType.class
    ).toMethodDescriptorString();

    private static final String middleInvokerDesc = MethodType.methodType(
            Object.class,
            MethodHandles.Lookup.class,
            MutableCallSite.class,
            String.class,
            MethodType.class,
            Object[].class
    ).toMethodDescriptorString();

    private static final String mainInvokerDesc = MethodType.methodType(
            MethodHandle.class, 
            MethodHandles.Lookup.class, 
            MutableCallSite.class, 
            String.class, 
            MethodType.class, 
            int.class, 
            int.class 
    ).toMethodDescriptorString();

    private final char[] chars; 
    private final int indexKey;

    public JMethod outerInvoker;
    public JField refField;

    public ReferenceObfuscationClassGenerator(char[] chars, int indexKey) {
        this.chars = chars;
        this.indexKey = indexKey;
    }

    @Override
    public JClass create(Context context) {
        var clazz = context.createClass("java/lang/Object", ACC_PUBLIC | ACC_SUPER);
        var midInvokerName = context.dictionary().newMethodName(clazz, middleInvokerDesc);
        var mainInvokerName = context.dictionary().newMethodName(clazz, mainInvokerDesc);
        var decryptorName = context.dictionary().newMethodName(clazz, "(II)Ljava/lang/String;");
        var refFieldName = context.dictionary().newFieldName(clazz, "[Ljava/lang/String;");
        refField = clazz.createField(ACC_STATIC, refFieldName, "[Ljava/lang/String;");

        createOuterInvoker(context, clazz, midInvokerName);
        createMiddleInvoker(context, clazz, midInvokerName, mainInvokerName);
        createMainInvoker(context, clazz, mainInvokerName, decryptorName);
        createDecryptor(clazz, decryptorName);
        context.addArtificial(clazz);
        return clazz;
    }

    public void generateClinit(Context context, JClass clazz, List<String> references) {
        var maxReferencesPer = 2000;
        var count = (int) Math.ceil((double) references.size() / maxReferencesPer);

        var clinit = clazz.findOrCreateClinit();
        var clinitBuilder = new InsnBuilder()
                .label(new LabelNode())
                ._int(references.size())
                .anewarray("java/lang/String")
                .field(PUTSTATIC, clazz.name(), refField.name(), refField.desc());

        int j = 0;
        for(var i = 0; i < count; i++) {
            var name = context.dictionary().newMethodName(clazz, "()V");
            var method = clazz.createMethod(ACC_STATIC, name, "()V");
            var methodBuilder = new InsnBuilder(method.insns())
                    .field(GETSTATIC, clazz.name(), refField.name(), refField.desc());

            for(int n = Math.min(j + maxReferencesPer, references.size()); j < n; j++) {
                var ref = references.get(j);
                methodBuilder
                        .dup()
                        ._int(j)
                        ._const(ref)
                        .aastore();
            }

            methodBuilder.pop()._return();
            clinitBuilder.method(INVOKESTATIC, clazz.name(), name, method.desc());
        }

        clinit.insns().insert(clinitBuilder.result());
    }

    private void createDecryptor(JClass clazz, String name) {
        var method = clazz.createMethod(ACC_STATIC, name, "(II)Ljava/lang/String;");
        
        var idxVar = method.allocVar(Type.INT_TYPE);
        var keyVar = method.allocVar(Type.INT_TYPE);

        var sb = method.allocVar();

        
        var loop = new LabelNode();
        var builder = new InsnBuilder(method.insns())
                .label(new LabelNode())
                .type(NEW, "java/lang/StringBuilder")
                .dup()
                .field(GETSTATIC, clazz.name(), refField.name(), refField.desc())
                ._var(ILOAD, idxVar)
                .aaload()
                .method(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V")
                ._var(ASTORE, sb)

                ._int(0)

                .label(loop)
                ._var(ALOAD, sb)
                .swap()
                .dup_x1()
                .method(INVOKEVIRTUAL, "java/lang/StringBuilder", "charAt", "(I)C")
                ._var(ILOAD, keyVar)
                ._int(16)
                .iushr()
                .ixor()

                ._var(ALOAD, sb) 
                .dup2_x1()
                .pop2() 
                .dup_x2().pop() 
                .dup_x2().pop() 
                .swap() 
                .dup_x2()
                .swap()
                .method(INVOKEVIRTUAL, "java/lang/StringBuilder", "setCharAt", "(IC)V")

                .label(new LabelNode())
                ._int(1)
                .iadd()
                .dup() 

                ._var(ALOAD, sb)
                .method(INVOKEVIRTUAL, "java/lang/StringBuilder", "length", "()I")
                .jump(IF_ICMPLT, loop)

                .pop()
                ._var(ALOAD, sb)
                .method(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;")
                ._areturn();


    }

    private void createOuterInvoker(Context context, JClass clazz, String midInvokeName) {
        var name = context.dictionary().newMethodName(clazz, invokerDesc);
        var method = clazz.createMethod(ACC_PUBLIC | ACC_STATIC, name, invokerDesc);
        var builder = new InsnBuilder(method.insns())
                .label(new LabelNode()) 
                .type(NEW, "java/lang/invoke/MutableCallSite")
                .dup()
                ._var(ALOAD, 2)
                .method(INVOKESPECIAL, "java/lang/invoke/MutableCallSite", "<init>", "(Ljava/lang/invoke/MethodType;)V")
                ._var(ASTORE, 3)

                .label(new LabelNode())
                ._var(ALOAD, 3)
                .method(INVOKESTATIC, "java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;")
                ._const(clazz.type())
                ._const(midInvokeName)
                ._const(Type.getObjectType("java/lang/Object"))
                ._const(Type.getObjectType("java/lang/invoke/MethodHandles$Lookup"))
                ._int(4)
                .anewarray("java/lang/Class")

                .dup()
                ._int(0)
                ._const(Type.getObjectType("java/lang/invoke/MutableCallSite"))
                .aastore()

                .dup()
                ._int(1)
                ._const(Type.getObjectType("java/lang/String"))
                .aastore()

                .dup()
                ._int(2)
                ._const(Type.getObjectType("java/lang/invoke/MethodType"))
                .aastore()

                .dup()
                ._int(3)
                ._const(Type.getObjectType("[Ljava/lang/Object;"))
                .aastore()

                .method(INVOKESTATIC, "java/lang/invoke/MethodType", "methodType", "(Ljava/lang/Class;Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;")
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
                ._const(Type.getObjectType("[Ljava/lang/Object;"))
                ._var(ALOAD, 2)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodType", "parameterCount", "()I")
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asCollector", "(Ljava/lang/Class;I)Ljava/lang/invoke/MethodHandle;")
                ._int(0)
                ._int(4)
                .anewarray("java/lang/Object")

                .dup()
                ._int(0)
                ._var(ALOAD, 0)
                .aastore()

                .dup()
                ._int(1)
                ._var(ALOAD, 3)
                .aastore()

                .dup()
                ._int(2)
                ._var(ALOAD, 1)
                .aastore()

                .dup()
                ._int(3)
                ._var(ALOAD, 2)
                .aastore()

                .method(INVOKESTATIC, "java/lang/invoke/MethodHandles", "insertArguments", "(Ljava/lang/invoke/MethodHandle;I[Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;")
                ._var(ALOAD, 2)
                .method(INVOKESTATIC, "java/lang/invoke/MethodHandles", "explicitCastArguments", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
                .method(INVOKEVIRTUAL, "java/lang/invoke/MutableCallSite", "setTarget", "(Ljava/lang/invoke/MethodHandle;)V")
                ._var(ALOAD, 3)
                ._areturn();

        outerInvoker = method;
    }

    private void createMiddleInvoker(Context context, JClass clazz, String name, String mainInvokerName) {
        var method = clazz.createMethod(ACC_PRIVATE | ACC_STATIC, name, middleInvokerDesc);
        var builder = new InsnBuilder(method.insns())
                .label(new LabelNode())
                ._var(ALOAD, 4)
                .dup()
                .arraylength()
                ._int(2)
                .isub()
                .aaload()
                .type(CHECKCAST, "java/lang/Integer")
                .method(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I")
                ._var(ISTORE, 5) 

                .label(new LabelNode())
                ._var(ALOAD, 4)
                .dup()
                .arraylength()
                ._int(1)
                .isub()
                .aaload()
                .type(CHECKCAST, "java/lang/Integer")
                .method(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I")
                ._var(ISTORE, 6) 

                .label(new LabelNode())
                ._var(ALOAD, 1)
                ._var(ALOAD, 0)
                ._var(ALOAD, 1)
                ._var(ALOAD, 2)
                ._var(ALOAD, 3)
                ._var(ILOAD, 5)
                ._var(ILOAD, 6)
                .method(INVOKESTATIC, clazz.name(), mainInvokerName, "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/invoke/MutableCallSite;Ljava/lang/String;Ljava/lang/invoke/MethodType;II)Ljava/lang/invoke/MethodHandle;")
                ._var(ASTORE, 7)

                .label(new LabelNode())
                ._var(ALOAD, 7)
                ._var(ALOAD, 3)
                .method(INVOKESTATIC, "java/lang/invoke/MethodHandles", "explicitCastArguments", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
                .method(INVOKEVIRTUAL, "java/lang/invoke/MutableCallSite", "setTarget", "(Ljava/lang/invoke/MethodHandle;)V")

                .label(new LabelNode())
                ._var(ALOAD, 7)
                ._const(Type.getObjectType("[Ljava/lang/Object;"))
                ._var(ALOAD, 4)
                .arraylength()
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asSpreader", "(Ljava/lang/Class;I)Ljava/lang/invoke/MethodHandle;")
                ._var(ALOAD, 4)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;")
                ._areturn();

    }

    private void createMainInvoker(Context context, JClass clazz, String mainInvokerName, String decryptorName) {
        var method = clazz.createMethod(ACC_PRIVATE | ACC_STATIC, mainInvokerName, mainInvokerDesc);
        
        
        var lookup = method.allocVar();
        var mutableCallSite = method.allocVar();
        var name = method.allocVar();
        var methodType = method.allocVar();
        var indexXorKey = method.allocVar(Type.INT_TYPE);
        var decryptionKey = method.allocVar(Type.INT_TYPE);

        
        var indexVar = method.allocVar(Type.INT_TYPE);
        var typeVar = method.allocVar(Type.INT_TYPE);
        var stringsVar = method.allocVar();
        var classVar = method.allocVar();
        var nameVar = method.allocVar();
        var targetType = method.allocVar();
        var handle = method.allocVar();

        var builder = new InsnBuilder(method.insns())
                .label(new LabelNode())
                ._var(ILOAD, indexXorKey)
                ._int(indexKey)
                .ixor()
                ._var(ISTORE, indexVar)

                .label(new LabelNode())
                ._var(ALOAD, name)
                ._int(0)
                .method(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C")
                ._var(ISTORE, typeVar)

                .label(new LabelNode()) 
                ._var(ILOAD, indexVar)
                ._var(ILOAD, decryptionKey)
                .method(INVOKESTATIC, clazz.name(), decryptorName, "(II)Ljava/lang/String;")
                ._const(":")
                .method(INVOKEVIRTUAL, "java/lang/String", "split", "(Ljava/lang/String;)[Ljava/lang/String;")
                ._var(ASTORE, stringsVar)

                .label(new LabelNode()) 
                ._var(ALOAD, stringsVar)
                ._int(0)
                .aaload()
                .method(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;")
                ._var(ASTORE, classVar)

                .label(new LabelNode()) 
                ._var(ALOAD, stringsVar)
                ._int(1)
                .aaload()
                ._var(ASTORE, nameVar)

                .label(new LabelNode()) 
                ._var(ALOAD, stringsVar)
                ._int(2)
                .aaload()
                ._const(clazz.type())
                .method(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;")
                .method(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")
                ._var(ASTORE, targetType)

                .label(new LabelNode());

        
        var virtualLabel = new LabelNode();
        var staticLabel = new LabelNode();
        var virtualFieldLabel = new LabelNode();
        var virtualGetter = new LabelNode();
        var staticGetter = new LabelNode();
        var staticFieldLabel = new LabelNode();
        var dflt = new LabelNode();
        var exit = new LabelNode();

        builder
                ._var(ILOAD, typeVar)
                .lookupswitch(SwitchUtils.createLookup(dflt, Map.of(
                        (int) chars[0], virtualLabel,
                        (int) chars[1], staticLabel,
                        (int) chars[2], virtualFieldLabel,
                        (int) chars[3], staticFieldLabel,
                        (int) chars[4], virtualFieldLabel,
                        (int) chars[5], staticFieldLabel
                )))
                .label(virtualLabel)
                ._var(ALOAD, lookup)
                ._var(ALOAD, classVar)
                ._var(ALOAD, nameVar)
                ._var(ALOAD, targetType)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
                ._var(ASTORE, handle)
                ._goto(exit)

                .label(staticLabel)
                ._var(ALOAD, lookup)
                ._var(ALOAD, classVar)
                ._var(ALOAD, nameVar)
                ._var(ALOAD, targetType)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;")
                ._var(ASTORE, handle)
                ._goto(exit)

                .label(virtualFieldLabel)
                ._var(ALOAD, lookup)
                ._var(ALOAD, classVar)
                ._var(ALOAD, nameVar)
                ._var(ALOAD, targetType)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodType", "returnType", "()Ljava/lang/Class;")
                ._var(ILOAD, typeVar)
                ._const(chars[2])
                .jump(IF_ICMPEQ, virtualGetter)

                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findSetter", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
                ._var(ASTORE, handle)
                ._goto(exit)

                .label(virtualGetter)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findGetter", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
                ._var(ASTORE, handle)
                ._goto(exit)

                .label(staticFieldLabel)
                ._var(ALOAD, lookup)
                ._var(ALOAD, classVar)
                ._var(ALOAD, nameVar)
                ._var(ALOAD, targetType)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodType", "returnType", "()Ljava/lang/Class;")
                ._var(ILOAD, typeVar)
                ._const(chars[3])
                .jump(IF_ICMPEQ, staticGetter)

                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStaticSetter", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
                ._var(ASTORE, handle)
                ._goto(exit)

                .label(staticGetter)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStaticGetter", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
                ._var(ASTORE, handle)
                ._goto(exit)

                .label(dflt)
                ._null()
                .athrow()

                
                .label(exit)
                ._var(ALOAD, handle)

                ._var(ALOAD, methodType)
                .method(INVOKEVIRTUAL, "java/lang/invoke/MethodType", "parameterCount", "()I")
                ._int(2)
                .isub()

                ._int(2)
                .anewarray("java/lang/Class")

                .dup()
                ._int(0)
                .field(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;")
                .aastore()

                .dup()
                ._int(1)
                .field(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;")
                .aastore()
                .method(INVOKESTATIC, "java/lang/invoke/MethodHandles", "dropArguments", "(Ljava/lang/invoke/MethodHandle;I[Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")
                ._areturn();
    }
}
