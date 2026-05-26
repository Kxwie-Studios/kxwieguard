package dev.kxwie.studios.kxwieguard;

import dev.kxwie.studios.kxwieguard.file.impl.ConfigLoader;
import dev.kxwie.studios.kxwieguard.file.impl.ConfigWriter;
import dev.kxwie.studios.kxwieguard.file.impl.exclusions.ExclusionLoader;
import dev.kxwie.studios.kxwieguard.file.impl.exclusions.ExclusionWriter;
import dev.kxwie.studios.kxwieguard.file.impl.initOrder.ClassInitOrderLoader;
import dev.kxwie.studios.kxwieguard.file.impl.initOrder.ClassInitOrderWriter;
import dev.kxwie.studios.kxwieguard.file.impl.references.ReferenceLoader;
import dev.kxwie.studios.kxwieguard.file.impl.references.ReferenceWriter;
import dev.kxwie.studios.kxwieguard.file.mapping.MappingExport;
import dev.kxwie.studios.kxwieguard.log.Logger;

import java.io.IOException;

public class Main {
    private static final String configPrefix = "--config=";
    private static final String exclusionPrefix = "--exclusions=";
    private static final String initOrderPrefix = "--initOrder=";
    private static final String referencePrefix = "--references=";
    private static final String javaPathPrefix = "--javaPath=";

    private static final String BAD_ARGS = """
            Usage tutorial.
            You ran kxwieguard with no arguments (or bad arguments). KxwieGuard is a CLI tool, run the obfuscator using any of these args:
            These files have to be in the `workspace/` folder provided in the ZIP file. If the path contains spaces, add double quotes.
            \t`--config=`     (Required)
            \t`--exclusions=` (Optional)
            \t`--initOrder=`  (Optional)
            \t`--references=` (Optional)
            \t'--javaPath='   (Optional) (Specifies the Java Runtime class path to use to obfuscate the JAR file)
            """;

    public static void main(String[] args) {
        UpdateChecker.checkAndPrintUpdates();
        if(args.length == 0) {
            Logger.error(BAD_ARGS);
            return;
        }

        
        var configPath = "";
        var exclusionPath = "";
        var initOrderPath = "";
        var referencePath = "";
        var javaPath = "";
        for(var arg : args) {
            if(arg.startsWith(configPrefix))
                configPath = arg.substring(configPrefix.length());

            if(arg.startsWith(exclusionPrefix))
                exclusionPath = arg.substring(exclusionPrefix.length());

            if(arg.startsWith(initOrderPrefix))
                initOrderPath = arg.substring(initOrderPrefix.length());

            if(arg.startsWith(referencePrefix))
                referencePath = arg.substring(referencePrefix.length());

            if(arg.startsWith(javaPathPrefix))
                javaPath = arg.substring(javaPathPrefix.length());
        }

        if(configPath.isEmpty()) {
            Logger.error(BAD_ARGS);
            return;
        }

        
        var loader = new ConfigLoader(configPath);
        var context = loader.result();
        loader.load();

        if(!exclusionPath.isEmpty())
            new ExclusionLoader(exclusionPath).load();

        if(!referencePath.isEmpty())
            new ReferenceLoader(context, referencePath).load();

        if(!javaPath.isEmpty())
            context.javaPath(javaPath);

        
        context.initialize();
        if(!initOrderPath.isEmpty()) 
            new ClassInitOrderLoader(context, initOrderPath).load();

        context.transform()
                .exportJar();

        
        try {
            new ConfigWriter(context, configPath).write();
            new MappingExport(context).write();
        } catch (IOException e) {
            Logger.error("An exception was thrown when saving configs:");
            e.printStackTrace();
        }
    }
}
