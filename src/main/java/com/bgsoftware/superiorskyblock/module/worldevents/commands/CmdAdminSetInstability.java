package com.bgsoftware.superiorskyblock.module.worldevents.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CmdAdminSetInstability implements SuperiorCommand {

    private final WorldEventsModule module;

    public CmdAdminSetInstability(WorldEventsModule module) {
        this.module = module;
    }

    @Override public List<String> getAliases() { return Arrays.asList("setinstability", "setInstability"); }
    @Override public String getPermission()     { return "superior.admin.setinstability"; }
    @Override public String getUsage(Locale l)  { return "admin setinstability <player> <0-100>"; }
    @Override public String getDescription(Locale l) { return "Set island instability for a player."; }
    @Override public int getMinArgs()           { return 4; }
    @Override public int getMaxArgs()           { return 4; }
    @Override public boolean canBeExecutedByConsole() { return true; }
    @Override public boolean displayCommand()   { return true; }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        SuperiorPlayer target = SuperiorSkyblockAPI.getPlayer(args[2]);
        if (target == null) { sender.sendMessage("§cPlayer not found."); return; }

        Island island = target.getIsland();
        if (island == null) { sender.sendMessage("§cThat player has no island."); return; }

        int value;
        try { value = Integer.parseInt(args[3]); }
        catch (NumberFormatException e) { sender.sendMessage("§cMust be 0–100."); return; }

        int set = module.getInstabilityManager().setInstability(island.getUniqueId(), value);
        sender.sendMessage("§aSet §e" + target.getName() + "§a's instability to §e" + set + "%§a.");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
