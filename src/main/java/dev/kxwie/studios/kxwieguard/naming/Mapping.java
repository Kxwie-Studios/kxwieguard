package dev.kxwie.studios.kxwieguard.naming;


public record Mapping(String key, String value) {
    @Override
    public String toString() {
        return key;
    }
}
