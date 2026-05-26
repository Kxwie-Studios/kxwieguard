package dev.kxwie.studios.kxwieguard.transform.impl.salt;

import dev.kxwie.studios.kxwieguard.analysis.interpreter.SimpleFrame;
import dev.kxwie.studios.kxwieguard.analysis.ref.MethodCallNode;
import dev.kxwie.studios.kxwieguard.analysis.ref.ReferenceGraph;
import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.property.Property;
import dev.kxwie.studios.kxwieguard.salt.ISalt;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JMethod;
import dev.kxwie.studios.kxwieguard.utils.ASMUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MethodSaltTransformer extends Transformer {
    public MethodSaltTransformer() {
        super("Method Salting", "methodSalting");
    }

    @Override
    public void transform(Context context) {
        var graph = context.referenceGraph().build();
        var saltedMethods = new HashSet<JMethod>();

        
        for(var clazz : context.classes()) {
            registerClass(context, graph, clazz, saltedMethods);
        }

        
        for(var method : saltedMethods) {
            method.salt().updateVar(method.allocParameter(Type.INT_TYPE));
            markChange();
        }

        var modified = new HashSet<AbstractInsnNode>();
        for(var method : saltedMethods) {
            var refs = graph.refs(method);
            var salt = method.salt();

            for(var ref : refs) {
                var call = (MethodInsnNode) ref.insn();
                if(!modified.add(call)) 
                    continue;

                var caller = ref.caller();
                var frames = caller.frames(context);

                salt(context, caller, salt, call, frames);
            }
        }
    }

    private void salt(Context context, JMethod caller, ISalt salt, MethodInsnNode call, Map<AbstractInsnNode, SimpleFrame> frames) {
        var list = new InsnList();

        if(!caller.canSalt(frames.get(call))) { 
            list.add(context.properties().add(ASMUtils.pushInt(salt.value()), Property.UNPROTECTED_SALT));
        } else {
            var mask = caller.seed();
            var masked = caller.salt().value() & mask;

            list.add(caller.salt().load());
            list.add(context.properties().add(ASMUtils.pushInt(mask), Property.IGNORE_INTEGER));
            list.add(new InsnNode(IAND));
            list.add(context.properties().add(ASMUtils.pushInt(masked ^ salt.value()), Property.IGNORE_INTEGER));
            list.add(new InsnNode(IXOR));
        }

        caller.insns().insertBefore(call, list);
        call.desc = call.desc.replace(")", "I)");
    }

    private void registerClass(Context context, ReferenceGraph graph, JClass clazz, Set<JMethod> saltedMethods) {
        var toCheck = new HashSet<JMethod>();

        
        for(var method : clazz.methods()) {
            var impactedClasses = impactedClasses(context, clazz, method);

            if(skipMethodAndTree(graph, method, impactedClasses)) {
                toCheck.addAll(method.tree());
                toCheck.add(method);
            }
        }

        
        for(var method : clazz.methods()) {
            if(toCheck.contains(method)) 
                continue;

            var duplicateOpt = toCheck.stream()
                    .filter(e -> e != method)                    
                    .filter(e -> e.name().equals(method.name())) 
                    .filter(e -> e.desc().equals(method.desc().replace(")", "I)"))) 
                    .findAny();

            if(duplicateOpt.isPresent()) 
                continue;

            registerMethodTree(context, clazz, method, saltedMethods);
        }
    }

    private void registerMethodTree(Context context, JClass clazz, JMethod method, Set<JMethod> saltedMethods) {
        var impacted = impactedClasses(context, clazz, method);
        var salt = findOrGenerateSalt(method, impacted);

        for(var member : method.tree()) {
            if(!saltedMethods.add(member))
                continue;

            member.removeAccessFlags(ACC_VARARGS);
            member.makeSalt(salt, -1);
        }

        if(!saltedMethods.add(method))
            return;

        method.removeAccessFlags(ACC_VARARGS);
        method.makeSalt(salt, -1);
    }

    private int findOrGenerateSalt(JMethod method, Set<JClass> impactedClasses) {
        for(var clazz : impactedClasses) {
            var foundOpt = clazz.findMethod(method.name(), method.desc());
            if(foundOpt.isEmpty())
                continue;

            var found = foundOpt.get();
            if(!found.hasSalt())
                continue;

            return found.salt().value();
        }

        return random.nextInt();
    }

    private boolean skipMethodAndTree(ReferenceGraph graph, JMethod method, Set<JClass> impactedClasses) {
        if(method.owner().isLibMethod(method))
            return true;

        for(var member : impactedClasses) {
            var opt = member.findMethod(method.name(), method.desc());
            if(opt.isPresent())
                method = opt.get();

            if(Exclusions.METHOD_SALTING.excluded(member))
                return true;

            if(Exclusions.METHOD_SALTING.excluded(member, method))
                return true;

            if(cantEditMethod(member, method, true))
                return true;

            var refs = graph.refs(method);
            if (refs.stream().anyMatch(MethodCallNode::cantEdit))
                return true;
        }

        return false;
    }

    private Set<JClass> impactedClasses(Context context, JClass clazz, JMethod method) {
        var classes = new HashSet<>(clazz.children());
        classes.add(clazz);

        for(var parent : clazz.tree()) {
            if(!parent.hasMethodInTree(context, method))
                continue;

            classes.add(parent);
            classes.addAll(parent.children());
        }

        return classes;
    }
}