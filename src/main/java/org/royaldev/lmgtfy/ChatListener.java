package org.royaldev.lmgtfy;

import org.apache.commons.lang.Validate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

class ChatListener implements Listener {

    private final LMGTFYPlugin plugin;

    ChatListener(LMGTFYPlugin instance) {
        this.plugin = instance;
    }

    /**
     * Shortens a URL with is.gd.
     *
     * @param url URL to shorten
     * @return Shortened URL
     * @throws IOException          If an exception occurs encoding or shortening
     * @throws NullPointerException If any argument is null
     */
    private String shortenURL(String url) throws IOException {
        Validate.notNull(url, "url was null");
        final URL shorten = new URL("http://is.gd/create.php?format=simple&url=" + URLEncoder.encode(url, "UTF-8"));
        return this.getContent(shorten.toString());
    }

    /**
     * Gets the contents of an external URL.
     *
     * @param url URL to get contents of
     * @return Contents
     * @throws IOException
     * @throws NullPointerException If any argument is null
     */
    private String getContent(String url) throws IOException {
        Validate.notNull(url, "url was null");
        final URL u = new URL(url);
        final BufferedReader br = new BufferedReader(new InputStreamReader(u.openConnection().getInputStream()));
        final StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.substring(0, sb.length() - 1); // remove last newline
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!this.plugin.getConfig().getBoolean("shorten.enabled", true)) return;
        final String message = e.getMessage();
        for (final String word : e.getMessage().split(" ")) {
            final URI uri;
            try {
                uri = new URI(word);
            } catch (URISyntaxException ex) {
                continue;
            }
            final List<String> supportSchemas = this.plugin.getConfig().getStringList("shorten.supported_schemas");
            if (!supportSchemas.contains(uri.getScheme().toLowerCase())) continue;
            String shortURL;
            try {
                shortURL = this.shortenURL(word);
            } catch (IOException ex) {
                shortURL = word;
            }
            final int index = message.indexOf(word);
            e.setMessage(message.substring(0, index) + shortURL + message.substring(index + word.length()));
        }
    }

}