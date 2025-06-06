package de.tomasgng;

import okhttp3.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class BackupUploader {

    public static BackupUploader instance = new BackupUploader();
    private static final String API_URL = "https://pixeldrain.com/api/file";

    // 0f069b54-8c6e-4958-8ef4-669891d63b8f // KEY!!
    public String upload(Player player, File backupFile) {
        player.sendMessage("§aStarting backup upload for " + backupFile.getName());
        OkHttpClient client = new OkHttpClient();

        // Datei als Multipart-Body einbinden
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", backupFile.getName(),
                                 RequestBody.create(backupFile, MediaType.parse("application/octet-stream")))
                .build();

        // Anfrage erstellen
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(":0f069b54-8c6e-4958-8ef4-669891d63b8f".getBytes()))
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // Antwort als JSON parsen
                String responseBody = response.body().string();
                if (responseBody.contains("success\":true")) {
                    String fileId = responseBody.split("\"id\":\"")[1].split("\"")[0];
                    return "https://pixeldrain.com/u/" + fileId;
                } else {
                    return "Fehler: " + responseBody;
                }
            } else {
                return "Fehler: " + response.code();
            }
        } catch (IOException e) {
            return "Fehler: " + e.getMessage();
        }
    }

}
