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

        OkHttpClient client = createOkHttpClient();
        RequestBody body = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        CountingRequestBody countingBody = createCountingRequestBody(sender, body, file);

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
