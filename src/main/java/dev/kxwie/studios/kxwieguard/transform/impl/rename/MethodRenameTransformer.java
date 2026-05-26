package dev.kxwie.studios.kxwieguard.transform.impl.rename;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.Exclusions;
import dev.kxwie.studios.kxwieguard.naming.Mapping;
import dev.kxwie.studios.kxwieguard.naming.Mappings;
import dev.kxwie.studios.kxwieguard.transform.Setting;
import dev.kxwie.studios.kxwieguard.transform.Transformer;
import dev.kxwie.studios.kxwieguard.tree.impl.JClass;
import dev.kxwie.studios.kxwieguard.tree.impl.JMethod;
import dev.kxwie.studios.kxwieguard.utils.MemberUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MethodRenameTransformer extends Transformer {
    private final Setting<String> prefix = setting("prefix", "");
    private final Setting<Boolean> shuffle = setting("shuffle", false);

    public MethodRenameTransformer() {
        super("Rename Methods", "renameMethods");
    }

    @Override
    public void transform(Context context) {
        for(var clazz : context.classes()) {
            mapMethods(context, clazz);
        }

        remap(context);
    }

    private void mapMethods(Context context, JClass clazz) {
        if(shuffle.value()) {
            var seed = random.nextLong();

            Collections.shuffle(clazz.core().methods, new Random(seed));
            Collections.shuffle(clazz.methods(), new Random(seed));
        }

        for(var method : clazz.methods()) {
            var impactedClasses = impactedClasses(context, clazz, method);
            if(skipHierarchy(method, impactedClasses))
                continue;

            var name = findOrGenerateName(context, clazz, impactedClasses, method);
            for(var member : impactedClasses) {
                var oldId = MemberUtils.fullMethod(member, method);
                if(Mappings.METHOD.containsOld(oldId))
                    continue;

                var newId = MemberUtils.fullMethod(member.name(), name, method.desc());
                Mappings.METHOD.register(oldId, new Mapping(newId, name));

                markChange();
            }
        }
    }

    private String findOrGenerateName(Context context, JClass clazz, Set<JClass> impactedClasses, JMethod method) {
        for(var member : impactedClasses) {
            var id = MemberUtils.fullMethod(member, method);

            if(Mappings.METHOD.containsOld(id))
                return Mappings.METHOD.retrieve(id).value();
        }

        return context.dictionary().newMethodName(prefix.value(), clazz, method.desc());
    }

    
    private boolean skipHierarchy(JMethod method, Set<JClass> impactedClasses) {
        if(method.owner().isLibMethod(method))
            return true;

        
        for(var member : impactedClasses) {
            var opt = member.findMethod(method.name(), method.desc());
            if(opt.isPresent())
                method = opt.get();

            if(Exclusions.RENAME_METHOD.excluded(member))
                return true;

            if(Exclusions.RENAME_METHOD.excluded(member, method))
                return true;

            if(cantEditMethod(member, method, false, true))
                return true;
        }

        return false;
    }

    private Set<JClass> impactedClasses(Context context, JClass clazz, JMethod method) {
        var classes = new HashSet<>(clazz.children()); 
        classes.add(clazz);

        for(var parent : clazz.parents()) {
            if(!parent.hasMethodInTree(context, method))
                continue;

            classes.add(parent);
        }

        return classes;
    }
}
