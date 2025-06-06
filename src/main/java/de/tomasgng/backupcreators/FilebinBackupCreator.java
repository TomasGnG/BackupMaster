package de.tomasgng.backupcreators;

import de.tomasgng.BackupMasterPlugin;
import de.tomasgng.backupcreators.utils.CountingRequestBody;
import de.tomasgng.config.dataproviders.ConfigDataProvider;
import de.tomasgng.config.pathproviders.ConfigPathProvider;
import de.tomasgng.orm.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import okhttp3.*;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// https://filebin.net/api
// "Bins" are created as a folder which can be accessed, if people have the url.
// No upload size limit and uploads expire after 6 days.
// Example link: "https://filebin.net/testbin"
public class FilebinBackupCreator extends BackupCreator {

    private final ConfigDataProvider config = BackupMasterPlugin.getPlugin().getConfigDataProvider();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private int lastSentProgress = -1;

    @Override
    String uploadBackup(CommandSender sender, String path) {
        File file = new File(path);
        String bin = UUID.randomUUID().toString();
        String uploadUrl = "https://filebin.net/" + bin + "/" + file.getName();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        RequestBody body = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        CountingRequestBody countingBody = new CountingRequestBody(body, (bytesWritten, totalBytes) -> {
            int progress = (int) ((bytesWritten * 100) / totalBytes);

            switch (progress) {
                case 0, 10, 20, 30,40,50,60,70,80,90,100:
                    if(lastSentProgress == progress)
                        return;

                    msgConsumer.accept(sender, mm.deserialize("<green>Uploading " + FilenameUtils.removeExtension(file.getName()) + "... " + progress + "%</green>"));
                    lastSentProgress = progress;
                    break;
            }
        });

        Request request = new Request.Builder()
                .url(uploadUrl)
                .header("Accept", "application/json")
                .post(countingBody)
                .build();

        try(Response response = client.newCall(request).execute()) {
            if(response.isSuccessful()) {
                DatabaseManager.instance.createUploadHistory(new Date(), getBackupCreatorName(), uploadUrl);
                return uploadUrl;
            }

            throw new IOException(response.body().string());
        } catch (IOException e) {
            msgConsumer.accept(sender, config.getValue(ConfigPathProvider.MESSAGES_ERROR_WHILE_UPLOADING,
                                                       Component.class,
                                                       Map.of("%world%", FilenameUtils.removeExtension(file.getName()), "%reason%", e.getMessage())));
            return null;
        }
    }

    @Override
    String getBackupCreatorName() {
        return "Filebin";
    }
}
