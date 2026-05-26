package dev.kxwie.studios.kxwieguard.context.resource;

import dev.kxwie.studios.kxwieguard.context.Context;

import java.io.IOException;
import java.util.jar.JarOutputStream;


public interface HandledResource {
    void handle(Context context, JarOutputStream jos, String name, byte[] bytes) throws IOException;
}
