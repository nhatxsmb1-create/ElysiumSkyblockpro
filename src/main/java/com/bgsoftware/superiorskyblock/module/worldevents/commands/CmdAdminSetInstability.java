package com.bgsoftware.superiorskyblock.module.worldevents.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * /is admin setinstability <player> <0-100>
 */
public class CmdAdminSetInstability implements SuperiorCommand {

    private final WorldEventsModule module;

    public CmdAdminSetInstability(WorldEventsModule module) {
        this.module = module;
    }

    @Override public List<String> getAliases() { return Arrays.asList("setinstability", "setInstability"); }
    @Override public String getPermission()     { return "superior.admin.setinstability"; }
    @Override public String getUsage(Locale l)  { return "admin setinstability <player> <0-100>"; }
    @Override public String getDescription(Locale l) { return "Set island instability for a player."; }
    @Override public int getMinArgs()           { return 3; }
    @Override public boolean onlyPlayers()      { return false; }

    @Override
    public void execute(com.bgsoftware.superiorskyblock.api.SuperiorSkyblock plugin,
                        CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /" + getUsage(Locale.ENGLISH));
            return;
        }

        SuperiorPlayer target = SuperiorSkyblockAPI.getPlayer(args[2]);
        if (target == null) { sender.sendMessage("§cPlayer not found."); return; }

        Island island = target.getIsland();
        if (island == null) { sender.sendMessage("§cThat player has no island."); return; }

        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInstability must be a number between 0 and 100.");
            return;
        }

        int set = module.getInstabilityManager().setInstability(island.getUniqueId(), value);
        sender.sendMessage("§aSet §e" + target.getName() + "§a's island instability to §e" + set + "%§a.");
    }

    @Override
    public List<String> tabComplete(com.bgsoftware.superiorskyblock.api.SuperiorSkyblock plugin,
                                     CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
