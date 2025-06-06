package de.tomasgng.utils;

import de.tomasgng.BackupMasterPlugin;
import de.tomasgng.backupcreators.BackupCreator;
import de.tomasgng.backupcreators.FilebinBackupCreator;
import de.tomasgng.config.pathproviders.ConfigPathProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class BackupManager {

    private final BukkitAudiences adventure = BackupMasterPlugin.getPlugin().getAdventure();
    private BackupCreator backupCreator;

    public boolean backupStarted;

    public BackupManager() {
        backupCreator = new FilebinBackupCreator();
        backupCreator.msgConsumer = (sender, component) -> adventure.sender(sender).sendMessage(component);
    }

    public void backupWorlds(CommandSender sender) {
        if(backupStarted) {
            adventure.sender(sender).sendMessage(BackupMasterPlugin.getPlugin().getConfigDataProvider().getValue(ConfigPathProvider.MESSAGES_BACKUP_ALREADY_STARTED, Component.class));
            return;
        }

        backupStarted = true;
        backupCreator.backupWorlds(sender);
    }
}
