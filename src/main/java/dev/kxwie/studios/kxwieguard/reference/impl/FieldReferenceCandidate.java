package dev.kxwie.studios.kxwieguard.reference.impl;

import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.exclude.StringFilter;
import dev.kxwie.studios.kxwieguard.reference.IReferenceCandidate;
import dev.kxwie.studios.kxwieguard.utils.MemberUtils;

public class FieldReferenceCandidate implements IReferenceCandidate {
    private final Context context;
    private final StringFilter filter;
    private final String filterString;

    public FieldReferenceCandidate(Context context, String filter) {
        this.context = context;
        this.filter = new StringFilter(filter);
        this.filterString = filter;
    }

    @Override
    public boolean test(String owner, String name, String desc) {
        var clazz = context.forName(owner);
        var field = clazz.findFieldFull(context, name, desc);

        if(field == null) {
            return filter.test(MemberUtils.fullField(clazz.originalName(), name, desc));
        } else {
            return filter.test(field.fullOriginalName());
        }
    }

    @Override
    public String getFilterString() {
        return filterString;
    }
}
