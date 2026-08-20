package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CmdAdminSpirit implements SuperiorCommand {

    private final SpiritsModule module;

    public CmdAdminSpirit(SpiritsModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return java.util.Arrays.asList("spirit", "spirits");
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getUsage(Locale locale) {
        return "spirit [player] [type]";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Qu\u1ea3n l\u00fd ho\u1eb7c ph\u00e1t Tinh Linh (Admin).";
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean displayCommand() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                new AdminSpiritsMenu(module).open((Player) sender);
            } else {
                sender.sendMessage("\u00a7cL\u1ec7nh n\u00e0y ch\u1ec9 d\u00e0nh cho ng\u01b0\u1eddi ch\u01a1i ho\u1eb7c ph\u1ea3i ghi \u0111\u1ea7y \u0111\u1ee7 <player> <type>.");
            }
            return;
        }

        if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("\u00a7cKh\u00f4ng t\u00ecm th\u1ea5y ng\u01b0\u1eddi ch\u01a1i.");
                return;
            }

            String type = args[1].toLowerCase();
            SpiritConfigInfo info = module.getConfiguration().getSpirits().get(type);
            if (info == null) {
                sender.sendMessage("\u00a7cKh\u00f4ng t\u00ecm th\u1ea5y Tinh Linh " + type);
                return;
            }

            ItemStack item = module.getSpiritManager().createSpiritItem(type);
            if (item != null) {
                target.getInventory().addItem(item);
                sender.sendMessage("\u00a7a\u0110\u00e3 ph\u00e1t Tinh Linh cho " + target.getName());
            }
            return;
        }
        
        sender.sendMessage("\u00a7c\u0110\u00e1nh sai c\u00fa ph\u00e1p. S\u1eed d\u1ee5ng: /is admin spirit \u0111\u1ec3 m\u1edf menu ho\u1eb7c /is admin spirit <player> <type>");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return null; // players
        } else if (args.length == 2) {
            return new ArrayList<>(module.getConfiguration().getSpirits().keySet());
        }
        return java.util.Collections.emptyList();
    }
}
