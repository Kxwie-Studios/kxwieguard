package dev.kxwie.studios.kxwieguard.naming.dictionary;


import dev.kxwie.studios.kxwieguard.tree.impl.JClass;

public interface IDictionary {
    String newClassName(String prefix);
    String newMethodName(String prefix, JClass owner, String desc);
    String newFieldName(String prefix, JClass owner, String desc);

    String newClassName();
    String newMethodName(JClass owner, String desc);
    String newFieldName(JClass owner, String desc);

    String newName(int count);

    void revertClass();
    void revertMethod();
    void revertField();
}
