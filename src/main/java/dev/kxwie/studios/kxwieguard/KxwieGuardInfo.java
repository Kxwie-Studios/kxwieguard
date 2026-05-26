package dev.kxwie.studios.kxwieguard;

public class KxwieGuardInfo {
    private static final int major = 1, minor = 0, patch = 0;

    public static String build() {
        return String.format("v%s.%s.%s", major, minor, patch);
    }

    public static String type() {
        return "KxwieGuard Free";
    }

    public static String versionText() {
        return String.format("%s %s", type(), build());
    }
}
