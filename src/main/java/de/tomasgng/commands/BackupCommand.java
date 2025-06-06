package de.tomasgng.commands;

import de.tomasgng.BackupMasterPlugin;
import de.tomasgng.config.pathproviders.ConfigPathProvider;
import de.tomasgng.orm.DatabaseManager;
import de.tomasgng.orm.UploadHistory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class BackupCommand extends Command {

    public BackupCommand() {
        super(BackupMasterPlugin.getPlugin().getConfigDataProvider().getValue(ConfigPathProvider.SETTINGS_COMMAND_NAME, String.class),
              "Create easy backups with an upload function.",
              "",
              (List<String>) BackupMasterPlugin.getPlugin().getConfigDataProvider().getValue(ConfigPathProvider.SETTINGS_COMMAND_ALIASES, List.class));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(args.length == 0)
            BackupMasterPlugin.getPlugin().getBackupManager().backupWorlds(sender);

        if(args.length == 2 && args[0].equalsIgnoreCase("history")) {
            int limit;

            try {
                limit = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Keine Zahl ya salame");
                return false;
            }

            List<UploadHistory> historyList = DatabaseManager.instance.getUploadHistoryList(limit);

            if(historyList.isEmpty()) {
                sender.sendMessage("Keine Einträge ya salame");
            }

            for (int i = 0; i < historyList.size(); i++) {
                UploadHistory history = historyList.get(i);

                sender.sendMessage((i+1) + " - " + history.backupCreator + " - " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.GERMAN).format(history.creationDate) + " - " + history.link);
            }
        }
        return false;
    }
}
