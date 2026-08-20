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
        return "superiorskyblock.admin.spirit";
    }

    @Override
    public String getUsage(Locale locale) {
        return "spirit <player> <type>";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Give a spirit to a player.";
    }

    @Override
    public int getMinArgs() {
        return 2;
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
