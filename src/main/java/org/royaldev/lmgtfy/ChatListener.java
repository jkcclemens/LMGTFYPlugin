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
import java.util.regex.Pattern;

class ChatListener implements Listener {

    private final LMGTFYPlugin plugin;
    private final Pattern urlPattern = Pattern.compile("(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))");

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
        if (!e.getPlayer().hasPermission("lmgtfy.autoshorten")) return;
        final String message = e.getMessage();
        for (final String word : e.getMessage().split(" ")) {
            if (!this.urlPattern.matcher(word).matches()) continue;
            String shortURL;
            try {
                shortURL = this.shortenURL(word);
                if (shortURL.startsWith("Error:")) shortURL = word;
            } catch (IOException ex) {
                shortURL = word;
            }
            if (!this.plugin.getConfig().getBoolean("shorten.show_schema", true)) {
                try {
                    final URI uri = new URI(shortURL);
                    if (uri.getScheme() != null)
                        shortURL = uri.toString().substring(uri.getScheme().length() + 3); // "://" = 3
                } catch (URISyntaxException ignored) {}
            }
            final int index = message.indexOf(word);
            e.setMessage(message.substring(0, index) + shortURL + message.substring(index + word.length()));
        }
    }

}
