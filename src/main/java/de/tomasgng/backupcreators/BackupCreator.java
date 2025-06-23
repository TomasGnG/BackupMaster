package de.tomasgng.backupcreators;

import de.tomasgng.BackupMasterPlugin;
import de.tomasgng.backupcreators.utils.CountingRequestBody;
import de.tomasgng.config.dataproviders.ConfigDataProvider;
import de.tomasgng.config.pathproviders.ConfigPathProvider;
import net.kyori.adventure.text.Component;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public abstract class BackupCreator {
    private final ConfigDataProvider config = BackupMasterPlugin.getPlugin().getConfigDataProvider();

    private final String backupFolder = config.getValue(ConfigPathProvider.SETTINGS_BACKUP_FOLDER, String.class);
    private final String tempBackupFolder = backupFolder + "/temp/";

    public BiConsumer<CommandSender, Component> msgConsumer;
    public int lastSentProgress = -1;

    public void backupWorlds(CommandSender sender) {
        msgConsumer.accept(sender, config.getValue(ConfigPathProvider.MESSAGES_BACKUP_STARTED, Component.class));

        new File(backupFolder).mkdir();

        Bukkit.getScheduler().runTask(BackupMasterPlugin.getPlugin(), () -> Bukkit.getWorlds().forEach(World::save));

        Bukkit.getScheduler().runTaskAsynchronously(BackupMasterPlugin.getPlugin(), () -> {
            for (World world : Bukkit.getWorlds()) {
                backupWorld(sender, world);
                String downloadLink = uploadBackup(sender, backupFolder + "/" + world.getName() + ".zip");

                if(downloadLink == null)
                    continue;

                msgConsumer.accept(sender, config.getValue(ConfigPathProvider.MESSAGES_BACKUP_UPLOADED,
                                                           Component.class,
                                                           Map.of("%world%", world.getName(), "%link%", downloadLink)));
            }
            BackupMasterPlugin.getPlugin().getBackupManager().backupStarted = false;
        });
    }

    abstract String uploadBackup(CommandSender sender, String path);

    abstract String getBackupCreatorName();

    protected OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();
    }

    protected @NotNull CountingRequestBody createCountingRequestBody(CommandSender sender, RequestBody body, File file) {
        return new CountingRequestBody(body, (bytesWritten, totalBytes) -> {
            int progress = (int) ((bytesWritten * 100) / totalBytes);

            if(progress % 20 != 0)
                return;

            if(lastSentProgress == progress)
                return;

            msgConsumer.accept(sender, config.getValue(ConfigPathProvider.MESSAGES_BACKUP_UPLOAD_PROGRESS,
                                                       Component.class,
                                                       Map.of("%world%", FilenameUtils.removeExtension(file.getName()),
                                                              "%progress%", "" + progress)));
            lastSentProgress = progress;
        });
    }

    private void backupWorld(CommandSender sender, World world) {
        File worldFolder = world.getWorldFolder();
        File tempFolder = new File(tempBackupFolder + world.getName());

        try {
            copyDirectoryExcluding(worldFolder, tempFolder, "session.lock");
        } catch (IOException e) {
            msgConsumer.accept(sender, config.getValue(ConfigPathProvider.MESSAGES_ERROR_WHILE_CREATING_TEMP_COPY,
                                                       Component.class,
                                                       Map.of("%world%", world.getName(), "%reason%", e.getMessage())));
            e.printStackTrace();
            return;
        }

        String backupPath = backupFolder + "/" + world.getName() + ".zip";
        new File(backupPath).delete();

        try(ZipFile zip = new ZipFile(backupFolder + "/" + world.getName() + ".zip")) {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);

            if(config.getValue(ConfigPathProvider.SETTINGS_ENCRYPTION_ENABLED, Boolean.class)) {
                parameters.setEncryptionMethod(EncryptionMethod.AES);
                parameters.setEncryptFiles(true);
                parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
                zip.setPassword(config.getValue(ConfigPathProvider.SETTINGS_ENCRYPTION_ZIP_PASSWORD, String.class).toCharArray());
            }

            zip.addFolder(tempFolder, parameters);

            msgConsumer.accept(sender, config.getValue(ConfigPathProvider.MESSAGES_BACKUP_CREATED, Component.class, Map.of("%world%", world.getName())));
        } catch (IOException e) {
            msgConsumer.accept(sender, config.getValue(ConfigPathProvider.MESSAGES_BACKUP_PROCESS_FAILED,
                                                       Component.class,
                                                       Map.of("%world%", world.getName(), "%reason%", e.getMessage())));
        }

        try {
            FileUtils.deleteDirectory(tempFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyDirectoryExcluding(File source, File target, String excludeName) throws IOException {
        if (!source.isDirectory()) {
            throw new IllegalArgumentException("Source must be a directory");
        }

        if (!target.exists()) {
            target.mkdirs();
        }

        File[] files = source.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if(!file.exists())
                continue;

            if (file.isDirectory()) {
                File targetDir = new File(target, file.getName());
                copyDirectoryExcluding(file, targetDir, excludeName);
            } else if (!file.getName().equals(excludeName)) {
                File targetFile = new File(target, file.getName());
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
