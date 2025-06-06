package de.tomasgng.config.pathproviders;

import de.tomasgng.config.utils.ConfigExclude;
import de.tomasgng.config.utils.ConfigPair;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.UUID;

public final class ConfigPathProvider {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private static final String SETTINGS_BASE = "settings.";
    public static ConfigPair SETTINGS_PIXELDRAIN_API_KEY = pair(SETTINGS_BASE + "pixeldrainApiKey", "Insert your API KEY here", "The API Key for Pixeldrain uploads.", "Obtain one here: https://pixeldrain.com/user/api_keys");
    public static ConfigPair SETTINGS_BACKUP_FOLDER = pair(SETTINGS_BASE + "backupFolder", "backups", "The folder where the ZIP files will be stored.");
    public static ConfigPair SETTINGS_COMMAND_NAME = pair(SETTINGS_BASE + "command.name", "backupmaster", "The name of the main command.");
    public static ConfigPair SETTINGS_COMMAND_PERMISSION = pair(SETTINGS_BASE + "command.permission", "backupmaster.use");
    public static ConfigPair SETTINGS_COMMAND_ALIASES = pair(SETTINGS_BASE + "command.aliases", List.of("backups", "backup"));
    public static ConfigPair SETTINGS_ENCRYPTION_ENABLED = pair(SETTINGS_BASE + "encryption.enabled", false);
    public static ConfigPair SETTINGS_ENCRYPTION_ZIP_PASSWORD = pair(SETTINGS_BASE + "encryption.zipPassword", UUID.randomUUID().toString(), "The password for the encrypted ZIP file");

    @ConfigExclude(excludeComments = false)
    public static ConfigPair MESSAGES_BASE = pair("messages", null, "All messages are in MiniMessage format.", "Check: https://docs.advntr.dev/minimessage/format");
    public static ConfigPair MESSAGES_PREFIX = pair(MESSAGES_BASE.getPath() + ".prefix", "<gradient:green:dark_green>BackupMaster</gradient> <dark_gray>●<gray>");
    public static ConfigPair MESSAGES_BACKUP_STARTED = pair(MESSAGES_BASE.getPath() + ".backupStarted", "%prefix% Backup process has been <green>started</green>.");
    public static ConfigPair MESSAGES_BACKUP_ALREADY_STARTED = pair(MESSAGES_BASE.getPath() + ".backupAlreadyStarted", "%prefix% A backup is currently in progress.");
    public static ConfigPair MESSAGES_BACKUP_CREATED = pair(MESSAGES_BASE.getPath() + ".backupCreated", "%prefix% Backup <green>successfully</green> created for <green>%world%</green>.");
    public static ConfigPair MESSAGES_BACKUP_PROCESS_FAILED = pair(MESSAGES_BASE.getPath() + ".backupProcessFailed", "%prefix% Backup <red>failed</red> for <red>%world%</red>. Reason: <red>%reason%");
    public static ConfigPair MESSAGES_ERROR_WHILE_CREATING_TEMP_COPY = pair(MESSAGES_BASE.getPath() + ".backupTempFolderFailed", "%prefix% Error while creating a temp folder for <red>%world%<gray>. Reason: <red>%reason%");
    public static ConfigPair MESSAGES_BACKUP_UPLOAD_PROGRESS = pair(MESSAGES_BASE.getPath() + ".backupUploadProgress", "%prefix% Uploading <green>%world%<gray>... <green>%progress%%");
    public static ConfigPair MESSAGES_BACKUP_UPLOADED = pair(MESSAGES_BASE.getPath() + ".backupUploaded", "%prefix% Backup <green>successfully</green> uploaded for <green>%world%<gray>. <br><gray>Download link: <green>%link%");
    public static ConfigPair MESSAGES_ERROR_WHILE_UPLOADING = pair(MESSAGES_BASE.getPath() + ".backupUploadFailed", "%prefix% Error while uploading the backup for <red>%world%<gray>. Reason: <red>%reason%");
    public static ConfigPair MESSAGES_PIXELDRAIN_API_KEY_MISSING = pair(MESSAGES_BASE.getPath() + ".pixeldrainApiKeyMissing", "%prefix% Please specify a ");

    private static ConfigPair pair(String path, Object value) {
        return new ConfigPair(path, value);
    }

    private static ConfigPair pair(String path, Object value, String... comments) {
        return new ConfigPair(path, value, comments);
    }
}
