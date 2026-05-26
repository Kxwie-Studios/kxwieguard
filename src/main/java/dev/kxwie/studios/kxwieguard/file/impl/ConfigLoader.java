package dev.kxwie.studios.kxwieguard.file.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.kxwie.studios.kxwieguard.context.Context;
import dev.kxwie.studios.kxwieguard.file.Loader;
import dev.kxwie.studios.kxwieguard.log.Logger;
import dev.kxwie.studios.kxwieguard.transform.Setting;
import dev.kxwie.studios.kxwieguard.transform.TransformerOrder;

public class ConfigLoader implements Loader {
    private final String configPath;
    private final Context context;

    public ConfigLoader(String configPath) {
        this.configPath = configPath;
        this.context = Context.newInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load() {
        var file = Context.getFromWorkspace(configPath);
        if(!file.exists()) {
            Logger.warn("No config file found. Running with no obfuscation.");
            throw new RuntimeException();
        }
        Logger.info("Loading config (%s)...", configPath);

        
        var configObject = new Gson().fromJson(Context.readWorkspaceString(configPath), JsonObject.class);
        context.in(configObject.get("in").getAsString())
                .out(configObject.get("out").getAsString())
                .libs(configObject.get("libs").getAsString())
                .setDictionary(configObject.get("dictionary").getAsString())
                .setWatermark(configObject.get("watermark").getAsString());

        if(configObject.has("aggressiveOverload"))
            context.setAggressiveOverload(configObject.get("aggressiveOverload").getAsBoolean());

        if(configObject.get("computeFrames").getAsBoolean())
            context.computeFrames();

        
        var transformers = configObject.get("transformers").getAsJsonObject();
        for(var transformer : TransformerOrder.transformers()) {
            var e = transformers.get(transformer.key());
            if(e == null)
                continue;

            var obj = e.getAsJsonObject();
            if(obj == null)
                continue;

            var enabled = obj.get("enabled").getAsBoolean();
            if(enabled)
                context.transformers().add(transformer);

            
            for(var setting : transformer.settings()) {
                var settingObj = obj.get(setting.key());
                if(settingObj == null)
                    continue;

                switch (setting.value()) {
                    case Boolean _ -> ((Setting<Boolean>) setting).setValue(settingObj.getAsBoolean());
                    case String _ -> ((Setting<String>) setting).setValue(settingObj.getAsString());
                    case Integer _ -> ((Setting<Integer>) setting).setValue(settingObj.getAsInt());
                    default -> {}
                }
            }
        }
    }

    public Context result() {
        return context;
    }
}
