package de.tomasgng.backupcreators;

import com.google.gson.JsonParser;
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
import java.util.Base64;
import java.util.Date;
import java.util.Map;

// https://pixeldrain.com/api
// Uploading a file will generate a random id which is used to download the uploaded file.
// - max. 20GB per file
// - needs API Key (sign up)
public class PixeldrainBackupCreator extends BackupCreator {

    private final ConfigDataProvider config = BackupMasterPlugin.getPlugin().getConfigDataProvider();
    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    String uploadBackup(CommandSender sender, String path) {
        final String apiKey = config.getValue(ConfigPathProvider.SETTINGS_PIXELDRAIN_API_KEY, String.class);

        if(apiKey.equalsIgnoreCase(ConfigPathProvider.SETTINGS_PIXELDRAIN_API_KEY.getStringValue())) {
            msgConsumer.accept(sender, config.getValue(ConfigPathProvider.MESSAGES_PIXELDRAIN_API_KEY_MISSING, Component.class));
            return null;
        }

        File file = new File(path);
        String uploadUrl = "https://pixeldrain.com/api/file/" + file.getName();

        OkHttpClient client = createOkHttpClient();

        RequestBody body = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        CountingRequestBody countingBody = createCountingRequestBody(sender, body, file);

        Request request = new Request.Builder()
                .url(uploadUrl)
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((":" + apiKey).getBytes()))
                .header("Accept", "application/json")
                .header("name", file.getName())
                .put(countingBody)
                .build();

        try(Response response = client.newCall(request).execute()) {
            if(response.isSuccessful()) {
                String downloadUrl = "https://pixeldrain.com/u/" + JsonParser.parseString(response.body().string()).getAsJsonObject().get("id").getAsString();
                DatabaseManager.instance.createUploadHistory(new Date(), getBackupCreatorName(), downloadUrl);
                return downloadUrl;
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
        return "Pixeldrain";
    }
}
