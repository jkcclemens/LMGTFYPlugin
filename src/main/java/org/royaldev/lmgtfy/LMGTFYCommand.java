package org.royaldev.lmgtfy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class LMGTFYCommand implements CommandExecutor {

    private final LMGTFYPlugin plugin;

    LMGTFYCommand(LMGTFYPlugin instance) {
        this.plugin = instance;
    }

    private String getLMGTFY(String query) {
        Validate.notNull(query, "query was null");
        try {
            return "http://lmgtfy.com/?q=" + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("lmgtfy")) {
            if (!cs.hasPermission("lmgtfy.lmgtfy")) {
                cs.sendMessage(ChatColor.RED + "You don't have permission for that!");
                return true;
            }
            final Player p = (cs instanceof Player) ? (Player) cs : null;
            if (args.length < 1) {
                cs.sendMessage(cmd.getDescription());
                return false;
            }
            final Player t = this.plugin.getServer().getPlayer(args[0]);
            if (t != null && args.length < 2) {
                cs.sendMessage(cmd.getDescription());
                return false;
            }
            final String query = StringUtils.join(args, ' ', ((t == null) ? 0 : 1), args.length);
            final String lmgtfy = this.getLMGTFY(query);
            if (lmgtfy == null) {
                cs.sendMessage(ChatColor.RED + "An error occurred and has been logged to the console.");
                return true;
            }
            final String message = ((t == null) ? "" : t.getName() + ": ") + lmgtfy;
            if (p == null) this.plugin.getServer().dispatchCommand(cs, "say " + message);
            else p.chat(message);
            return true;
        }
        return false;
    }

}
