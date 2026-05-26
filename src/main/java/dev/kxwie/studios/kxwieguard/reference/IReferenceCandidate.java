package dev.kxwie.studios.kxwieguard.reference;

public interface IReferenceCandidate {
    boolean test(String owner, String name, String desc);

    String getFilterString();
}
