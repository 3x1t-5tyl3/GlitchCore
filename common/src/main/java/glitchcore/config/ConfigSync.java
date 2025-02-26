/*******************************************************************************
 * Copyright 2023, the Glitchfiend Team.
 * All rights reserved.
 ******************************************************************************/
package glitchcore.config;

import glitchcore.core.GlitchCore;
import glitchcore.network.SyncConfigPacket;
import glitchcore.util.Environment;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ConfigSync
{
    private static Map<String, Config> configs = new HashMap<>();

    public static void register(Config config)
    {
        String relative = Environment.getConfigPath().relativize(config.getPath()).toString().replace('\\', '/');
        configs.put(relative, config);
        GlitchCore.LOGGER.info("Registered synced config with path " + relative);
    }

    public static Stream<SyncConfigPacket> createPackets()
    {
        return configs.entrySet().stream().map(e -> {
            var config = e.getValue();

            // Reload the config from the filesystem, but do not save it
            config.read();
            config.load();

            return new SyncConfigPacket(e.getKey(), e.getValue().encode().getBytes(StandardCharsets.UTF_8));
        });
    }

    public static void reload(String path, String toml)
    {
        if (configs.containsKey(path))
        {
            var config = configs.get(path);
            config.parse(toml);
            config.load();
        }
        else throw new IllegalArgumentException("Unknown synced config " + path);
    }
}
