import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class ChatFilterPlugin extends JavaPlugin implements Listener {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final String GPT4_API_URL = "https://api.openai.com/v1/engines/davinci-codex/completions";
    private final String GPT4_API_KEY = "your-gpt-4-api-key-here";

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        // Send the message to GPT-4 API to check for offensive content
        RequestBody body = new FormBody.Builder()
                .add("prompt", "Is the following message offensive: " + message)
                .add("max_tokens", "10")
                .build();

        Request request = new Request.Builder()
                .url(GPT4_API_URL)
                .addHeader("Authorization", "Bearer " + GPT4_API_KEY)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            String result = json.getAsJsonObject("choices").getAsJsonArray("text").get(0).getAsString();

            if ("yes".equalsIgnoreCase(result.trim())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Your message was flagged as offensive.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
