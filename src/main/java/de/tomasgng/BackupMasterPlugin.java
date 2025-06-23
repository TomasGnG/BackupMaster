package de.tomasgng;

import de.tomasgng.commands.BackupCommand;
import de.tomasgng.config.ConfigManager;
import de.tomasgng.config.dataproviders.ConfigDataProvider;
import de.tomasgng.scheduling.QuartzScheduleManager;
import de.tomasgng.utils.BackupManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class BackupMasterPlugin extends JavaPlugin {

    private static BackupMasterPlugin plugin;
    private BukkitAudiences adventure;

    private ConfigManager configManager;
    private ConfigDataProvider configDataProvider;
    private BackupManager backupManager;
    private QuartzScheduleManager quartzScheduleManager;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(this);
        configManager = new ConfigManager();
        configDataProvider = new ConfigDataProvider();
        backupManager = new BackupManager();
        quartzScheduleManager = new QuartzScheduleManager();

        registerCommand();
    }

    private void registerCommand() {
        try {
            final Field bukkitCmdMap = getServer().getClass().getDeclaredField("commandMap");
            bukkitCmdMap.setAccessible(true);

            CommandMap cmdMap = (CommandMap) bukkitCmdMap.get(getServer());

            cmdMap.register("backupworlds", new BackupCommand());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().severe("Couldn't register BackupWorlds command!");
            getLogger().severe(e.getMessage());
        }
    }



    @Override
    public void onDisable() {
        if(adventure != null) {
            adventure.close();
            adventure = null;
        }

        quartzScheduleManager.onDisable();
    }

    public static BackupMasterPlugin getPlugin() {
        return plugin;
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ConfigDataProvider getConfigDataProvider() {
        return configDataProvider;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }
}
