package de.tomasgng.config.dataproviders;

import de.tomasgng.BackupMasterPlugin;
import de.tomasgng.config.ConfigManager;
import de.tomasgng.config.pathproviders.ConfigPathProvider;
import de.tomasgng.config.utils.ConfigPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigDataProvider {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final ConfigManager config;

    public ConfigDataProvider() {
        config = BackupMasterPlugin.getPlugin().getConfigManager();
    }

    public <T> T getValue(ConfigPair pair, Class<T> type) {
        return getValue(pair, type, null);
    }

    public <T> T getValue(ConfigPair pair, Class<T> type, @Nullable Map<String, String> placeholders) {
        if(type == Boolean.class)
            return type.cast(config.getBoolean(pair));

        if(type == String.class) {
            String str = config.getString(pair);
            str = str.replace("%prefix%", config.getString(ConfigPathProvider.MESSAGES_PREFIX));

            if(placeholders == null)
                return type.cast(str);

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                str = str.replace(entry.getKey(), entry.getValue());
            }

            return type.cast(str);
        }

        if(type == Component.class){
            Component msg = config.getMiniMessageComponent(pair);
            msg = msg.replaceText(TextReplacementConfig.builder()
                                                       .match(Pattern.compile("%prefix%", Pattern.CASE_INSENSITIVE))
                                                       .replacement(config.getMiniMessageComponent(ConfigPathProvider.MESSAGES_PREFIX))
                                                       .build());

            if(placeholders == null)
                return type.cast(msg);

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                TextReplacementConfig replaceConfig = TextReplacementConfig.builder()
                        .replacement(entry.getValue())
                        .match(Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE)).build();
                msg = msg.replaceText(replaceConfig);
            }

            return type.cast(msg);
        }

        if(type == Integer.class)
            return type.cast(config.getInteger(pair));

        if(type == Double.class)
            return type.cast(config.getDouble(pair));

        if(type == List.class)
            return type.cast(config.getStringList(pair));

        return null;
    }
}
