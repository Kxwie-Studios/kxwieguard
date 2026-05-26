package dev.kxwie.studios.kxwieguard.context.exception;

public class MissingWorkspaceItemException extends RuntimeException {
    public MissingWorkspaceItemException(String item) {
        super("Missing `" + item + "` from workspace path.");
    }
}
