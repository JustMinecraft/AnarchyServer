package net.justminecraft.anarchyserver;

import org.bukkit.Bukkit;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Discord {

    public static void send(String dataFinal) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(AnarchyServer.getAnarchyServer(), () -> send(dataFinal));
            return;
        }

        String data = dataFinal;

        AnarchyServer.getAnarchyServer().getLogger().info("Webhook sent: " + data);

        if (data.length() > 1950) {
            data = data.substring(0, 1950) + " (" + (data.length() - 1950) + " more characters...)";
        }

        try {
            data = "content=" + URLEncoder.encode(data, "UTF-8");
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(AnarchyServer.getDiscordWebhook()).openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "AnarchyServer");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(postData);
            }
            try (InputStream in = conn.getInputStream()) {

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
